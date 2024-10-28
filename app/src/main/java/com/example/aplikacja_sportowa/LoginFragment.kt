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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Łączenie widoku z kodem.
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        // Inicjalizacja Firebase.
        firebaseAuth = FirebaseAuth.getInstance()
        // Przenosi do ekranu rejestracji po kliknięciu na przycisk.
        binding.textView.setOnClickListener {
            val fragment = RegisterFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        // Funkcja odpowiadająca za obsługę przycisku logowania.
        binding.button.setOnClickListener {
            val email = binding.emailbox.text.toString().trim() // Pobranie email'u.
            val password = binding.passwordbox.text.toString().trim() // Pobranie hasła.
            // Sprawdzenie, czy pola nie są puste.
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Próba logowania się email'em oraz hasłem.
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Logowanie powiodło się, przejście do MainActivity.
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            // Sprawdzenie błędów podczas logowania się przez użytkownika.
                            val errorMessage = when (task.exception) {
                                is FirebaseAuthInvalidUserException -> "No user found with this email address."
                                is FirebaseAuthInvalidCredentialsException -> "Password incorrect!"
                                else -> "Login failed: ${task.exception?.localizedMessage ?: "Unknown error"}"
                            }
                            // Wyświetla komunikat o błędzie.
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Komunikat o tym, iż pola są puste.
                Toast.makeText(requireContext(), "Empty areas are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root // Zwrócenie widoku fragmentu.
    }
}
