package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.aplikacja_sportowa.databinding.FragmentUserDataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserDataFragment : Fragment() {

    private lateinit var binding: FragmentUserDataBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDataBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        val ageAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.age_array,
            android.R.layout.simple_spinner_item
        )
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ageSpinner.adapter = ageAdapter

        val genderAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genderSpinner.adapter = genderAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Upload your data first!", Toast.LENGTH_SHORT).show()
        }

        binding.button.setOnClickListener {
            saveUserData()
        }

        return binding.root
    }

    private fun saveUserData() {
        val username = binding.usernamebox.text.toString().trim()
        val age = binding.ageSpinner.selectedItem.toString()
        val gender = binding.genderSpinner.selectedItem.toString()
        val height = binding.heightbox.text.toString().trim()
        val weight = binding.weightbox.text.toString().trim()

        if (username.isNotEmpty() && age != "Choose your age" && gender != "Choose your gender" && height.isNotEmpty() && weight.isNotEmpty()) {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val userMap = mapOf(
                    "username" to username,
                    "age" to age,
                    "gender" to gender,
                    "height" to height,
                    "weight" to weight
                )

                database.child(userId).setValue(userMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "User data saved successfully!", Toast.LENGTH_SHORT).show()
                        navigateToLoginFragment()
                    } else {
                        Toast.makeText(requireContext(), "Error saving user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "User is not logged in", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Empty areas are not allowed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLoginFragment() {
        val fragment = LoginFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}