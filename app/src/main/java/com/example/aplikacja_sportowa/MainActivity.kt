package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private var isUserLoggedIn = false // "Flaga" - Sprawdza, czy użytkownik jest już zalogowany.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ustawienie głównego widoku aplikacji.

        firebaseAuth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout) // Pobranie layout'u.
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Pobranie paska toolbar.
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false) // Wyłączenie ikony menu.

        val navigationView = findViewById<NavigationView>(R.id.nav_view) // Pobranie nawigacji.
        navigationView.setNavigationItemSelectedListener(this)

        // Sprawdza, czy użytkownik jest już zalogowany.
        if (firebaseAuth.currentUser == null) {
            // Jeśli jest niezalogowany, przenosi go do rejestracji.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .commit()

            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        } else {
            // Użytkownik jest zalogowany.
            isUserLoggedIn = true
            setupDrawerToggle(toolbar)  // Ustawienie nawigacji (toolbar) dopiero po zalogowaniu.

            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
                navigationView.setCheckedItem(R.id.nav_home)
            }
        }
    }

    private fun setupDrawerToggle(toolbar: Toolbar) {
        // Konfiguracja ActionBarDrawerToggle po zalogowaniu użytkownika.
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Obsługa kliknięć w menu nawigacji.
        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(HomeFragment())
                title = "Home"
            }
            R.id.nav_profile -> {
                replaceFragment(ProfileFragment())
                title = "Profile"
            }
            R.id.nav_activity -> {
                replaceFragment(ActivityFragment())
                title = "Activity"
            }
            R.id.nav_run -> {
                replaceFragment(RunFragment())
                title = "Run"
            }
            R.id.nav_settings -> {
                replaceFragment(SettingsFragment())
                title = "Settings"
            }
            R.id.nav_about -> {
                replaceFragment(AboutFragment())
                title = "About Us"
            }
            R.id.nav_logout -> {
                firebaseAuth.signOut() // Wylogowanie użytkownika.
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show() // Powiadomienie o wylogowaniu.
                finish() // Powrót do ekranu logowania.
                return true
            }
        }
        item.isChecked = true // Ustawienie klikniętego elementu w menu jako zaznaczonego.
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        // Metoda służąca do zamiany fragmentu.
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        // Obsługa przycisku "Wstecz".
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}