package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aplikacja_sportowa.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Łączy widok z kodem.
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        // Inicjalizacja Firebase.
        firebaseAuth = FirebaseAuth.getInstance()
        // Przenosi do ekranu logowania po kliknięciu na przycisk.
        binding.textView.setOnClickListener {
            val fragment = LoginFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        // Funkcja odpowiadająca za obsługę przycisku rejestracji.
        binding.button.setOnClickListener {
            val email = binding.emailbox.text.toString()
            val password = binding.passwordbox.text.toString()
            val confirmpassword = binding.confirmpasswordbox.text.toString()
            // Funkcja sprawdza, czy pola nie są funkcje i czy hasła są poprawne.
            if (email.isNotEmpty() && password.isNotEmpty() && confirmpassword.isNotEmpty()) {
                if (password == confirmpassword) {
                    // Utworzenie konta w bazie danych.
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Przejście do ekranu logowania.
                                val fragment = LoginFragment()
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .commit()
                            } else {
                                // Wyświetla błąd, jeśli rejestracja się nie powiodła.
                                Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Passwords is not matching!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Empty areas are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
}
