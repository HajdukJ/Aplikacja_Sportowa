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

/**
 * Fragment odpowiedzialny za logowanie użytkownika. Umożliwia logowanie za pomocą adresu e-mail i hasła,
 * a także obsługę uwierzytelniania biometrycznego.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var biometricAuthenticator: BiometricAuthenticator
    private var isBiometricAuthCompleted = false

    /**
     * Metoda wywoływana przy tworzeniu widoku fragmentu. Inicjalizuje instancje FirebaseAuth i BiometricAuthenticator
     * oraz ustawia nasłuchiwanie na przyciski w interfejsie użytkownika.
     *
     * @param inflater Obiekt LayoutInflater do układu widoku.
     * @param container Kontener dla widoku.
     * @param savedInstanceState Zapisany stan fragmentu.
     * @return Widok fragmentu.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        biometricAuthenticator = BiometricAuthenticator(requireActivity())

        // Kliknięcie na tekstowy widok przenosi użytkownika do fragmentu rejestracji
        binding.textView.setOnClickListener {
            val fragment = UserDataFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Kliknięcie na przycisk logowania próbuje zalogować użytkownika za pomocą e-maila i hasła
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

    /**
     * Metoda wywoływana, gdy fragment przechodzi w stan pauzy. Jeśli biometryczne logowanie nie zostało ukończone,
     * użytkownik jest wylogowywany.
     */
    override fun onPause() {
        super.onPause()
        if (!isBiometricAuthCompleted) {
            firebaseAuth.signOut()
        }
    }

    /**
     * Obsługuje logowanie biometryczne po pomyślnym zalogowaniu się za pomocą e-maila i hasła.
     * Jeśli e-mail jest zapisany w preferencjach biometrycznych, użytkownik jest proszony o uwierzytelnienie
     * za pomocą odcisku palca.
     *
     * @param email Adres e-mail użytkownika, który próbował się zalogować.
     */
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

    /**
     * Wylogowuje użytkownika i przenosi go do fragmentu logowania, jeśli wystąpił błąd podczas biometrycznego logowania.
     */
    private fun logoutUser() {
        firebaseAuth.signOut()
        showToast("Logged out for security reasons.")
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    /**
     * Nawigacja do głównej aktywności po pomyślnym zalogowaniu.
     */
    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Wyświetla krótki komunikat Toast na ekranie.
     *
     * @param message Treść komunikatu, który ma zostać wyświetlony.
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}