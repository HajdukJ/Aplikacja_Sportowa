package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aplikacja_sportowa.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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
                        createUserAccount(email, password, false)
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
            onSuccess = {
                createUserAccount(email, password, true)
            },
            onError = { _, errorString ->
                showToast("Error saving fingerprint: $errorString")
            },
            onFailed = {
                showToast("Fingerprint registration failed!")
            }
        )
    }

    private fun createUserAccount(email: String, password: String, useFingerprint: Boolean) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    val sharedPrefs = requireActivity().getSharedPreferences("BiometricPrefs", 0)

                    if (useFingerprint) {
                        sharedPrefs.edit().putString("fingerprint_user", email).apply()
                    }

                    val username = arguments?.getString("username") ?: "Unknown"
                    val age = arguments?.getString("age") ?: "Unknown"
                    val gender = arguments?.getString("gender") ?: "Unknown"
                    val height = arguments?.getString("height") ?: "Unknown"
                    val weight = arguments?.getString("weight") ?: "Unknown"
                    val image = arguments?.getString("image") ?: ""

                    userId?.let {
                        val userData = hashMapOf(
                            "email" to email,
                            "password" to password,
                            "fingerprint" to if (useFingerprint) "enabled" else "disabled",
                            "username" to username,
                            "age" to age,
                            "gender" to gender,
                            "height" to height,
                            "weight" to weight,
                            "image" to image
                        )

                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(it)
                            .setValue(userData)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    showToast("Account created successfully!")
                                    navigateToLoginFragment()
                                } else {
                                    showToast("Error saving user data: ${dbTask.exception?.message}")
                                }
                            }
                    }
                } else {
                    showToast(task.exception?.message ?: "Error creating account!")
                }
            }
    }

    private fun navigateToLoginFragment() {
        val fragment = LoginFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}