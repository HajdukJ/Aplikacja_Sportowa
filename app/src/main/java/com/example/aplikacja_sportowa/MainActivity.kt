package com.example.aplikacja_sportowa

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userProfileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        userName = headerView.findViewById(R.id.user_name)
        userEmail = headerView.findViewById(R.id.user_email)
        userProfileImage = headerView.findViewById(R.id.user_profile_image)

        navigationView.setNavigationItemSelectedListener(this)

        if (firebaseAuth.currentUser != null) {
            setupDrawerToggle(toolbar)
            loadUserData()
            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
                navigationView.setCheckedItem(R.id.nav_home)
                title = "Home"
            }
        } else {
            replaceFragment(LoginFragment())
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            supportActionBar?.hide()
        }
    }

    private fun setupDrawerToggle(toolbar: Toolbar) {
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun loadUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("username").value?.toString() ?: "Unknown User"
                    val email = currentUser.email ?: "Unknown Email"
                    val imageBase64 = snapshot.child("image").value?.toString()

                    userName.text = name
                    userEmail.text = email

                    if (!imageBase64.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(this@MainActivity)
                            .load(bitmap)
                            .circleCrop()
                            .into(userProfileImage)
                    } else {
                        userProfileImage.setImageResource(R.drawable.logo_aplikacja)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    userName.text = "Error loading data"
                    userEmail.text = "Error loading email"
                }
            })
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(HomeFragment())
                title = "Home"
                supportActionBar?.show()
            }
            R.id.nav_profile -> {
                replaceFragment(ProfileFragment())
                title = "Profile"
                supportActionBar?.show()
            }
            R.id.nav_run -> {
                replaceFragment(RunFragment())
                title = "Run"
                supportActionBar?.show()
            }
            R.id.nav_activity -> {
                replaceFragment(ActivityFragment())
                title = "Activity"
                supportActionBar?.show()
            }
            R.id.nav_settings -> {
                replaceFragment(SettingsFragment())
                title = "Settings"
                supportActionBar?.show()
            }
            R.id.nav_about -> {
                replaceFragment(AboutFragment())
                title = "About us"
                supportActionBar?.show()
            }
            R.id.nav_logout -> {
                firebaseAuth.signOut()
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                finish()
                return true
            }
        }
        item.isChecked = true
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}