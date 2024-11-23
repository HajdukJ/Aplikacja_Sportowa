package com.example.aplikacja_sportowa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aplikacja_sportowa.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var biometricAuthenticator: BiometricAuthenticator
    private var isBiometricAuthCompleted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        biometricAuthenticator = BiometricAuthenticator(requireActivity())

        binding.textView.setOnClickListener {
            val fragment = RegisterFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.button.setOnClickListener {
            val email = binding.emailbox.text.toString().trim()
            val password = binding.passwordbox.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            handleFingerprintLogin(email)
                        } else {
                            val exception = task.exception
                            when (exception) {
                                is FirebaseAuthInvalidUserException -> {
                                    showToast("No account found with this email")
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    showToast("Incorrect password")
                                }
                                else -> {
                                    showToast(exception?.localizedMessage ?: "Login failed")
                                }
                            }
                        }
                    }
            } else {
                showToast("Empty areas are not allowed!")
            }
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        if (!isBiometricAuthCompleted) {
            firebaseAuth.signOut()
        }
    }

    private fun handleFingerprintLogin(email: String) {
        val sharedPrefs = requireActivity().getSharedPreferences("BiometricPrefs", 0)
        val storedEmail = sharedPrefs.getString("fingerprint_user", null)

        if (email == storedEmail) {
            biometricAuthenticator.promptBiometricAuth(
                title = "Login",
                subTitle = "Authenticate with your fingerprint",
                negativeButtonText = "Cancel",
                onSuccess = {
                    isBiometricAuthCompleted = true
                    navigateToMainActivity()
                },
                onError = { _, errorString ->
                    isBiometricAuthCompleted = false
                    showToast("Error with fingerprint: $errorString")
                    logoutUser()
                },
                onFailed = {
                    isBiometricAuthCompleted = false
                    showToast("Invalid fingerprint. Try again.")
                    logoutUser()
                }
            )
        } else {
            isBiometricAuthCompleted = true
            navigateToMainActivity()
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        showToast("Logged out for security reasons.")
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}