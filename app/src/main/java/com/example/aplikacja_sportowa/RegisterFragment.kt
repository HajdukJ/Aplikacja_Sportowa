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

/**
 * Fragment odpowiedzialny za rejestrację użytkownika.
 * Umożliwia użytkownikowi rejestrację za pomocą adresu e-mail i hasła, a także opcję zapisania odcisku palca.
 */
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var biometricAuthenticator: BiometricAuthenticator

    /**
     * Metoda wywoływana przy tworzeniu widoku fragmentu.
     * Ustawia nasłuchiwanie na przycisk rejestracji oraz inicjalizuje Firebase i BiometricAuthenticator.
     *
     * @param inflater Obiekt LayoutInflater do układu widoku.
     * @param container Kontener dla widoku.
     * @param savedInstanceState Zapisanie stanu fragmentu.
     * @return Widok fragmentu.
     */
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

            // Sprawdzanie czy pola są wypełnione oraz czy hasła się zgadzają
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

    /**
     * Wywołanie procesu uwierzytelniania biometrycznego (np. odcisk palca) przy rejestracji.
     *
     * @param email Adres e-mail użytkownika.
     * @param password Hasło użytkownika.
     */
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

    /**
     * Tworzy konto użytkownika w Firebase i zapisuje dane użytkownika.
     *
     * @param email Adres e-mail użytkownika.
     * @param password Hasło użytkownika.
     * @param useFingerprint Określa, czy użytkownik wybrał zapis odcisku palca czy też nie
     */
    private fun createUserAccount(email: String, password: String, useFingerprint: Boolean) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    val sharedPrefs = requireActivity().getSharedPreferences("BiometricPrefs", 0)

                    // Zapisanie preferencji biometrycznych, jeśli włączono odcisk palca
                    if (useFingerprint) {
                        sharedPrefs.edit().putString("fingerprint_user", email).apply()
                    }

                    // Pobieranie danych użytkownika z argumentów fragmentu
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

                        // Zapisanie danych użytkownika do bazy Firebase
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

    /**
     * Nawigacja do fragmentu logowania po pomyślnej rejestracji.
     */
    private fun navigateToLoginFragment() {
        val fragment = LoginFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     *
     * @param message Treść komunikatu do wyświetlenia.
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}