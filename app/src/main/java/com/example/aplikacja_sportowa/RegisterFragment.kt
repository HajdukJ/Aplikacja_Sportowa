package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.biometric.BiometricPrompt
import com.example.aplikacja_sportowa.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        biometricAuthenticator = BiometricAuthenticator(requireActivity())

        binding.textView.setOnClickListener {
            val fragment = LoginFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.button.setOnClickListener {
            val email = binding.emailbox.text.toString().trim()
            val password = binding.passwordbox.text.toString().trim()
            val confirmPassword = binding.confirmpasswordbox.text.toString().trim()
            val useFingerprint = binding.fingerprintCheckbox.isChecked

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    if (useFingerprint) {
                        promptForFingerprint(email, password)
                    } else {
                        createUserAccount(email, password)
                    }
                } else {
                    showToast("Passwords do not match!")
                }
            } else {
                showToast("Empty areas are not allowed!")
            }
        }

        return binding.root
    }

    private fun promptForFingerprint(email: String, password: String) {
        biometricAuthenticator.promptBiometricAuth(
            title = "Register Fingerprint",
            subTitle = "Save your fingerprint for future logins",
            negativeButtonText = "Cancel",
            fragmentActivity = requireActivity(),
            onSuccess = {
                createUserAccount(email, password)
            },
            onError = { errorCode, errorString ->
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    showToast("Error saving fingerprint: $errorString")
                }
            },
            onFailed = {
                showToast("Fingerprint registration failed!")
            }
        )
    }

    private fun createUserAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Account created successfully!")
                    navigateToUserDataFragment()
                } else {
                    showToast(task.exception?.message ?: "Error creating account!")
                }
            }
    }

    private fun navigateToUserDataFragment() {
        val fragment = UserDataFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}