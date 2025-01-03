package com.example.aplikacja_sportowa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.Manifest
import android.content.pm.PackageManager

/**
 * Fragment, który wyświetla statystyki aktywności użytkownika, takie jak bieganie i jazda na rowerze.
 * Pobiera dane z bazy danych Firebase i wyświetla je w interfejsie użytkownika.
 */
class StatisticsFragment : Fragment() {

    private lateinit var statsLayout: LinearLayout
    private lateinit var databaseReference: DatabaseReference

    /**
     * Tworzy widok fragmentu, inicjalizuje layout, pobiera dane aktywności i sprawdza uprawnienia.
     *
     * @param inflater Inflater do tworzenia widoku
     * @param container Kontener, w którym ma się znaleźć widok
     * @param savedInstanceState Stan zapisany w poprzedniej instancji
     * @return Widok fragmentu
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_statistics, container, false)
        statsLayout = rootView.findViewById(R.id.statsLayout)
        fetchActivityData()  // Pobranie danych aktywności
        checkPermissions()  // Sprawdzenie uprawnień
        return rootView
    }

    /**
     * Sprawdza, czy aplikacja ma uprawnienia do odczytu zewnętrznej pamięci.
     * Jeśli nie, prosi użytkownika o nadanie uprawnień.
     */
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    /**
     * Pobiera dane aktywności użytkownika z Firebase i wyświetla je w interfejsie.
     * Obsługuje dwa typy aktywności: bieganie i jazda na rowerze.
     */
    private fun fetchActivityData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Referencje do bazy danych Firebase dla biegania i jazdy na rowerze
        val runsReference = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/runs")
        val cyclingReference = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/cycling")

        // Pobieranie danych biegania
        runsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statsLayout.removeAllViews()  // Usunięcie poprzednich widoków
                for (child in snapshot.children) {
                    val runData = child.value as? Map<String, Any>
                    if (runData != null) {
                        val date = runData["date"] as? Long ?: 0L
                        val distance = runData["distance"] as? Double ?: 0.0
                        val time = runData["time"] as? Long ?: 0L
                        val pace = runData["pace"] as? String ?: "00:00"
                        val route = runData["route"] as? List<Map<String, Double>> ?: emptyList()

                        // Tworzenie widoku dla danych biegania
                        val runView = createRunView(date, distance, time, pace, route)
                        statsLayout.addView(runView)
                    } else {
                        Log.e("Firebase", "Invalid run data: $child")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
                Toast.makeText(requireContext(), "Error fetching running data", Toast.LENGTH_SHORT).show()
            }
        })

        // Pobieranie danych jazdy na rowerze
        cyclingReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val cyclingData = child.value as? Map<String, Any>
                    if (cyclingData != null) {
                        val date = cyclingData["date"] as? Long ?: 0L
                        val distance = cyclingData["distance"] as? Double ?: 0.0
                        val time = cyclingData["time"] as? Long ?: 0L
                        val speed = cyclingData["speed"] as? Double ?: 0.0
                        val route = cyclingData["route"] as? List<Map<String, Double>> ?: emptyList()

                        // Tworzenie widoku dla danych jazdy na rowerze
                        val cyclingView = createCyclingView(date, distance, time, speed, route)
                        statsLayout.addView(cyclingView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching cycling data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Formatuje datę z formatu timestamp do formatu "dd/MM/yyyy HH:mm".
     *
     * @param timestamp Czas w formacie timestamp
     * @return Sformatowana data w postaci tekstu
     */
    private fun formatDate(timestamp: Long): String {
        return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(timestamp)
    }

    /**
     * Tworzy widok dla danych biegania.
     *
     * @param date Data aktywności
     * @param distance Przebyty dystans
     * @param time Czas trwania aktywności
     * @param pace Tempo biegu
     * @param route Trasa biegu
     * @return Widok reprezentujący dane biegania
     */
    private fun createRunView(date: Long, distance: Double, time: Long, pace: String, route: List<Map<String, Double>>): View {
        val runView = LinearLayout(requireContext())
        runView.orientation = LinearLayout.HORIZONTAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8)
        runView.layoutParams = layoutParams
        runView.setPadding(16, 16, 16, 16)

        runView.setBackgroundResource(R.drawable.border_button)
        runView.isClickable = true
        runView.isFocusable = true

        val runIcon = ImageView(requireContext())
        runIcon.layoutParams = LinearLayout.LayoutParams(80, 80)
        runIcon.setImageResource(R.drawable.baseline_directions_run_24)

        val textLayout = LinearLayout(requireContext())
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textLayout.setPadding(16, 0, 0, 0)

        // Wyświetlanie daty, dystansu, czasu i tempa
        val dateTextView = TextView(requireContext())
        dateTextView.text = "Date: ${formatDate(date)}"
        dateTextView.textSize = 18f
        dateTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        dateTextView.setTextColor(resources.getColor(android.R.color.black, null))

        val distanceTextView = TextView(requireContext())
        distanceTextView.text = String.format("Distance: %.2f km", distance)
        distanceTextView.textSize = 16f
        distanceTextView.setTextColor(resources.getColor(android.R.color.black, null))

        val timeTextView = TextView(requireContext())
        timeTextView.text = "Time: $time s"
        timeTextView.textSize = 16f
        timeTextView.setTextColor(resources.getColor(android.R.color.black, null))

        val paceTextView = TextView(requireContext())
        paceTextView.text = "Pace: $pace min/km"
        paceTextView.textSize = 16f
        paceTextView.setTextColor(resources.getColor(android.R.color.black, null))

        textLayout.addView(dateTextView)
        textLayout.addView(distanceTextView)
        textLayout.addView(timeTextView)
        textLayout.addView(paceTextView)

        runView.addView(runIcon)
        runView.addView(textLayout)

        runView.setOnClickListener {
            val mapImagePath = "/path/to/your/image"

            // Ładowanie ścieżki mapy
            Log.d("MapDisplay", "Map image path: $mapImagePath")

            if (mapImagePath.isNullOrEmpty()) {
                Log.e("MapDisplay", "Invalid map image path.")
                Toast.makeText(requireContext(), "Image path is invalid.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), MapDisplayActivity::class.java)
            intent.putExtra("mapImagePath", mapImagePath)
            startActivity(intent)
        }

        return runView
    }

    /**
     * Tworzy widok dla danych jazdy na rowerze.
     *
     * @param date Data aktywności
     * @param distance Przebyty dystans
     * @param time Czas trwania aktywności
     * @param speed Prędkość jazdy
     * @param route Trasa jazdy
     * @return Widok reprezentujący dane jazdy na rowerze
     */
    private fun createCyclingView(date: Long, distance: Double, time: Long, speed: Double, route: List<Map<String, Double>>): View {
        val cyclingView = LinearLayout(requireContext())
        cyclingView.orientation = LinearLayout.HORIZONTAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8)
        cyclingView.layoutParams = layoutParams
        cyclingView.setPadding(16, 16, 16, 16)

        cyclingView.setBackgroundResource(R.drawable.border_button)
        cyclingView.isClickable = true
        cyclingView.isFocusable = true

        val cyclingIcon = ImageView(requireContext())
        cyclingIcon.layoutParams = LinearLayout.LayoutParams(80, 80)
        cyclingIcon.setImageResource(R.drawable.baseline_directions_bike_24)

        val textLayout = LinearLayout(requireContext())
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textLayout.setPadding(16, 0, 0, 0)

        // Wyświetlanie daty, dystansu, czasu i prędkości
        val dateTextView = TextView(requireContext())
        dateTextView.text = "Date: ${formatDate(date)}"
        dateTextView.textSize = 18f
        dateTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        dateTextView.setTextColor(resources.getColor(android.R.color.black, null))

        val distanceTextView = TextView(requireContext())
        distanceTextView.text = String.format("Distance: %.2f km", distance)
        distanceTextView.textSize = 16f
        distanceTextView.setTextColor(resources.getColor(android.R.color.black, null))

        val timeTextView = TextView(requireContext())
        timeTextView.text = "Time: $time s"
        timeTextView.textSize = 16f
        timeTextView.setTextColor(resources.getColor(android.R.color.black, null))

        val speedTextView = TextView(requireContext())
        speedTextView.text = String.format("Speed: %.2f km/h", speed)
        speedTextView.textSize = 16f
        speedTextView.setTextColor(resources.getColor(android.R.color.black, null))

        textLayout.addView(dateTextView)
        textLayout.addView(distanceTextView)
        textLayout.addView(timeTextView)
        textLayout.addView(speedTextView)

        cyclingView.addView(cyclingIcon)
        cyclingView.addView(textLayout)

        // Obsługuje kliknięcie w widok aktywności rowerowej
        cyclingView.setOnClickListener {
            val mapImagePath = "/path/to/your/image"

            // Ładowanie ścieżki mapy
            Log.d("MapDisplay", "Map image path: $mapImagePath")

            if (mapImagePath.isNullOrEmpty()) {
                Log.e("MapDisplay", "Invalid map image path.")
                Toast.makeText(requireContext(), "Image path is invalid.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), MapDisplayActivity::class.java)
            intent.putExtra("mapImagePath", mapImagePath)
            startActivity(intent)
        }

        return cyclingView
    }
}