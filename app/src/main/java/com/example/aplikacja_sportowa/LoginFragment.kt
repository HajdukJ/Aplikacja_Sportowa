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

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var biometricAuthenticator: BiometricAuthenticator

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
                            checkForFingerprint(email)
                        } else {
                            showToast(task.exception?.localizedMessage ?: "Login failed")
                        }
                    }
            } else {
                showToast("Empty areas are not allowed")
            }
        }

        return binding.root
    }

    private fun checkForFingerprint(email: String) {
        val sharedPrefs = requireActivity().getSharedPreferences("BiometricPrefs", 0)
        val storedEmail = sharedPrefs.getString("fingerprint_user", null)

        if (email == storedEmail) {
            biometricAuthenticator.promptBiometricAuth(
                title = "Login",
                subTitle = "Authenticate with your fingerprint",
                negativeButtonText = "Cancel",
                onSuccess = {
                    navigateToMainActivity()
                },
                onError = { _, errorString ->
                    showToast("Error with fingerprint: $errorString")
                },
                onFailed = {
                    showToast("Invalid fingerprint. Try again.")
                }
            )
        } else {
            navigateToMainActivity()
        }
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