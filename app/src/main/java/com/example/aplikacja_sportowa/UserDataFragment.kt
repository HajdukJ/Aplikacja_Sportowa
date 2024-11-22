package com.example.aplikacja_sportowa

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.aplikacja_sportowa.databinding.FragmentUserDataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UserDataFragment : Fragment() {

    private lateinit var binding: FragmentUserDataBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var imageUri: Uri? = null
    private var imageBase64: String? = null
    private val PICK_IMAGE_REQUEST = 1

    private var selectedGender: String? = null
    private var selectedAge: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDataBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Upload your data first!", Toast.LENGTH_SHORT).show()
        }

        binding.button.setOnClickListener { saveUserData() }
        binding.profileImageView.setOnClickListener { openFileChooser() }

        setupGenderSelection()
        setupAgeSelection()

        return binding.root
    }

    private fun setupGenderSelection() {
        resetGenderButtonStyles()

        binding.maleButton.setOnClickListener {
            selectedGender = "Male"
            updateGenderButtonStyles(binding.maleButton)
        }
        binding.femaleButton.setOnClickListener {
            selectedGender = "Female"
            updateGenderButtonStyles(binding.femaleButton)
        }
    }

    private fun resetGenderButtonStyles() {
        binding.maleButton.setBackgroundResource(R.drawable.field_border_background)
        binding.femaleButton.setBackgroundResource(R.drawable.field_border_background)
    }

    private fun updateGenderButtonStyles(selectedButton: View) {
        resetGenderButtonStyles()
        selectedButton.setBackgroundResource(R.drawable.selected_border_background)
    }

    private fun setupAgeSelection() {
        resetAgeButtonStyles()

        binding.age1824.setOnClickListener {
            selectedAge = "18-24"
            updateAgeButtonStyles(binding.age1824)
        }
        binding.age2534.setOnClickListener {
            selectedAge = "25-34"
            updateAgeButtonStyles(binding.age2534)
        }
        binding.age3544.setOnClickListener {
            selectedAge = "35-44"
            updateAgeButtonStyles(binding.age3544)
        }
        binding.age45Plus.setOnClickListener {
            selectedAge = "45+"
            updateAgeButtonStyles(binding.age45Plus)
        }
    }

    private fun resetAgeButtonStyles() {
        binding.age1824.setBackgroundResource(R.drawable.field_border_background)
        binding.age2534.setBackgroundResource(R.drawable.field_border_background)
        binding.age3544.setBackgroundResource(R.drawable.field_border_background)
        binding.age45Plus.setBackgroundResource(R.drawable.field_border_background)
    }

    private fun updateAgeButtonStyles(selectedButton: View) {
        resetAgeButtonStyles()
        selectedButton.setBackgroundResource(R.drawable.selected_border_background)
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.profileImageView.setImageURI(imageUri)
            convertImageToBase64()
            loadImageIntoCircle()
        }
    }

    private fun convertImageToBase64() {
        try {
            val inputStream: InputStream? = imageUri?.let { requireContext().contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val imageBytes: ByteArray = outputStream.toByteArray()
            imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            Log.d("UserDataFragment", "Image converted successfully!")
        } catch (e: Exception) {
            Log.e("UserDataFragment", "Error converting image: ${e.message}", e)
            Toast.makeText(requireContext(), "Error converting image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImageIntoCircle() {
        if (imageUri != null) {
            Glide.with(requireContext())
                .load(imageUri)
                .circleCrop()
                .into(binding.profileImageView)
        }
    }

    private fun saveUserData() {
        val username = binding.usernamebox.text.toString().trim()
        val height = binding.heightbox.text.toString().trim()
        val weight = binding.weightbox.text.toString().trim()

        if (username.isNotEmpty() && selectedAge != null && selectedGender != null && height.isNotEmpty() && weight.isNotEmpty()) {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val userMap = mapOf(
                    "image" to imageBase64,
                    "username" to username,
                    "age" to selectedAge,
                    "gender" to selectedGender,
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