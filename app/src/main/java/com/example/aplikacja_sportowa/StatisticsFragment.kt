package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StatisticsFragment : Fragment() {

    private lateinit var statsLayout: LinearLayout
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_statistics, container, false)
        statsLayout = rootView.findViewById(R.id.statsLayout)
        fetchActivityData()
        return rootView
    }

    private fun fetchActivityData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val runsReference = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/runs")
        val cyclingReference = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/cycling")

        runsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statsLayout.removeAllViews()
                for (child in snapshot.children) {
                    val runData = child.value as? Map<String, Any>
                    if (runData != null) {
                        val date = runData["date"] as? Long ?: 0L
                        val distance = runData["distance"] as? Double ?: 0.0
                        val time = runData["time"] as? Long ?: 0L
                        val pace = runData["pace"] as? String ?: "00:00"
                        val route = runData["route"] as? List<Map<String, Double>> ?: emptyList()

                        val runView = createRunView(date, distance, time, pace, route)
                        statsLayout.addView(runView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching running data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

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

    private fun createRunView(date: Long, distance: Double, time: Long, pace: String, route: List<Map<String, Double>>): View {
        val runView = LinearLayout(requireContext())
        runView.orientation = LinearLayout.HORIZONTAL
        runView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        runView.setPadding(0, 0, 0, 16)

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

        val dateTextView = TextView(requireContext())
        val dateFormatted = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(date)
        dateTextView.text = "Date: $dateFormatted"
        dateTextView.textSize = 18f
        dateTextView.setTextColor(resources.getColor(android.R.color.black))
        dateTextView.setTypeface(null, android.graphics.Typeface.BOLD)

        val distanceTextView = TextView(requireContext())
        distanceTextView.text = String.format("Distance: %.2f km", distance)
        distanceTextView.textSize = 16f
        distanceTextView.setTextColor(resources.getColor(android.R.color.black))

        val timeTextView = TextView(requireContext())
        timeTextView.text = "Time: $time s"
        timeTextView.textSize = 16f
        timeTextView.setTextColor(resources.getColor(android.R.color.black))

        val paceTextView = TextView(requireContext())
        paceTextView.text = "Pace: $pace min/km"
        paceTextView.textSize = 16f
        paceTextView.setTextColor(resources.getColor(android.R.color.black))

        textLayout.addView(dateTextView)
        textLayout.addView(distanceTextView)
        textLayout.addView(timeTextView)
        textLayout.addView(paceTextView)

        runView.addView(runIcon)
        runView.addView(textLayout)

        return runView
    }

    private fun createCyclingView(date: Long, distance: Double, time: Long, speed: Double, route: List<Map<String, Double>>): View {
        val cyclingView = LinearLayout(requireContext())
        cyclingView.orientation = LinearLayout.HORIZONTAL
        cyclingView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cyclingView.setPadding(0, 0, 0, 16)

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

        val dateTextView = TextView(requireContext())
        val dateFormatted = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(date)
        dateTextView.text = "Date: $dateFormatted"
        dateTextView.textSize = 18f
        dateTextView.setTextColor(resources.getColor(android.R.color.black))
        dateTextView.setTypeface(null, android.graphics.Typeface.BOLD)

        val distanceTextView = TextView(requireContext())
        distanceTextView.text = String.format("Distance: %.2f km", distance)
        distanceTextView.textSize = 16f
        distanceTextView.setTextColor(resources.getColor(android.R.color.black))

        val timeTextView = TextView(requireContext())
        timeTextView.text = "Time: $time s"
        timeTextView.textSize = 16f
        timeTextView.setTextColor(resources.getColor(android.R.color.black))

        val speedTextView = TextView(requireContext())
        speedTextView.text = String.format("Speed: %.2f km/h", speed)
        speedTextView.textSize = 16f
        speedTextView.setTextColor(resources.getColor(android.R.color.black))

        textLayout.addView(dateTextView)
        textLayout.addView(distanceTextView)
        textLayout.addView(timeTextView)
        textLayout.addView(speedTextView)

        cyclingView.addView(cyclingIcon)
        cyclingView.addView(textLayout)

        return cyclingView
    }
}