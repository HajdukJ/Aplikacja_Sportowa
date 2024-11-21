package com.example.aplikacja_sportowa

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.graphics.BitmapFactory

class ProfileFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var heightTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var profileImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        ageTextView = view.findViewById(R.id.ageTextView)
        genderTextView = view.findViewById(R.id.genderTextView)
        heightTextView = view.findViewById(R.id.heightTextView)
        weightTextView = view.findViewById(R.id.weightTextView)
        profileImageView = view.findViewById(R.id.profileImageView)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        loadUserData()

        return view
    }

    private fun loadUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usernameTextView.text = "Username: ${snapshot.child("username").value.toString() ?: "N/A"}"
                    emailTextView.text = "Email: ${currentUser.email ?: "N/A"}"
                    ageTextView.text = "Age: ${snapshot.child("age").value.toString() ?: "N/A"}"
                    genderTextView.text = "Gender: ${snapshot.child("gender").value.toString() ?: "N/A"}"
                    heightTextView.text = "Height: ${snapshot.child("height").value.toString() ?: "N/A"} cm"
                    weightTextView.text = "Weight: ${snapshot.child("weight").value.toString() ?: "N/A"} kg"

                    val profileImageBase64 = snapshot.child("image").value?.toString()
                    if (!profileImageBase64.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(requireContext())
                            .load(bitmap)
                            .circleCrop()
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.logo_aplikacja)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    usernameTextView.text = "Error loading data"
                }
            })
        } else {
            usernameTextView.text = "User not logged in"
        }
    }
}