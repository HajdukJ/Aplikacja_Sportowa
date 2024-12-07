package com.example.aplikacja_sportowa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        fetchRunData()
        return rootView
    }

    private fun fetchRunData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/runs")
        databaseReference.addValueEventListener(object : ValueEventListener {
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
                Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createRunView(date: Long, distance: Double, time: Long, pace: String, route: List<Map<String, Double>>): View {
        val textView = TextView(requireContext())
        val dateFormatted = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(date)
        textView.text = "Date: $dateFormatted\nDistance: ${"%.2f".format(distance)} km\nTime: ${time}s\nPace: $pace min/km"
        textView.textSize = 16f
        return textView
    }
}