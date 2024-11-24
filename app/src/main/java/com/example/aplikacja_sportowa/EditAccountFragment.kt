package com.example.aplikacja_sportowa

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.aplikacja_sportowa.databinding.FragmentEditAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditAccountFragment : Fragment() {

    private lateinit var binding: FragmentEditAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var imageUri: Uri? = null
    private var imageBase64: String? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        loadUserData()

        binding.saveSettingsButton.setOnClickListener { saveUserData() }
        binding.profileImageView.setOnClickListener { openFileChooser() }
        binding.cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    private fun loadUserData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.value as? Map<*, *> ?: return
                    binding.emailbox.setText(userData["email"] as? String ?: "")
                    binding.usernamebox.setText(userData["username"] as? String ?: "")
                    binding.weightbox.setText(userData["weight"] as? String ?: "")
                    val imageBase64 = userData["image"] as? String
                    if (!imageBase64.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(this@EditAccountFragment)
                            .load(bitmap)
                            .circleCrop()
                            .into(binding.profileImageView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
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
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(binding.profileImageView)
            convertImageToBase64()
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
        } catch (e: Exception) {
            Log.e("EditAccountFragment", "Error converting image: ${e.message}", e)
            Toast.makeText(requireContext(), "Error converting image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val email = binding.emailbox.text.toString().trim()
        val username = binding.usernamebox.text.toString().trim()
        val weight = binding.weightbox.text.toString().trim()
        val password = binding.passwordbox.text.toString().trim()
        val confirmPassword = binding.confirmpasswordbox.text.toString().trim()

        if (password.isNotEmpty() && password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updates = mutableMapOf<String, Any>()
                    if (email.isNotEmpty()) updates["email"] = email
                    if (username.isNotEmpty()) updates["username"] = username
                    if (weight.isNotEmpty()) updates["weight"] = weight
                    if (!imageBase64.isNullOrEmpty()) updates["image"] = imageBase64!!
                    if (password.isNotEmpty()) firebaseAuth.currentUser?.updatePassword(password)

                    database.child(userId).updateChildren(updates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, ProfileFragment())
                                    .addToBackStack(null)
                                    .commit()
                            }, 2000)
                        } else {
                            Toast.makeText(requireContext(), "Error updating profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error loading user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}