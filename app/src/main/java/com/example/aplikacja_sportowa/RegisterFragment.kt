package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.view.Gravity
import android.widget.TextView
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
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val fragment = LoginFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.button.setOnClickListener {
            val email = binding.emailbox.text.toString()
            val password = binding.passwordbox.text.toString()
            val confirmpassword = binding.confirmpasswordbox.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmpassword.isNotEmpty()) {
                if (password == confirmpassword) {
                    firebaseAuth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val result = task.result
                                val signInMethods = result?.signInMethods

                                if (signInMethods != null && signInMethods.isNotEmpty()) {
                                    showToast("Account with this email already exists!")
                                } else {
                                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val fragment = LoginFragment()
                                                requireActivity().supportFragmentManager.beginTransaction()
                                                    .replace(R.id.fragment_container, fragment)
                                                    .commit()
                                            } else {
                                                val errorMessage = task.exception?.message
                                                showToast(errorMessage ?: "Error creating account")
                                            }
                                        }
                                }
                            } else {
                                val exceptionMessage = task.exception?.message
                                showToast(exceptionMessage ?: "Error checking email")
                            }
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

    private fun showToast(message: String) {
        val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        val toastText = toast.view?.findViewById<TextView>(android.R.id.message)
        toastText?.apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        toast.show()
    }
}
