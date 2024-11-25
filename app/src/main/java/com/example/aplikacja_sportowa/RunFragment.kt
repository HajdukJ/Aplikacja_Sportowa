package com.example.aplikacja_sportowa

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class RunFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_run, container, false)

        val runningButton = view.findViewById<FrameLayout>(R.id.runningButton)
        val cyclingButton = view.findViewById<FrameLayout>(R.id.cyclingButton)

        runningButton.setOnClickListener {
            Toast.makeText(context, "Current activity: Running", Toast.LENGTH_SHORT).show()

            Handler().postDelayed({
                val runActivityFragment = RunActivityFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, runActivityFragment)
                    .addToBackStack(null)
                    .commit()
            }, 1000)
        }

        cyclingButton.setOnClickListener {
            Toast.makeText(context, "Current Activty: Cycling", Toast.LENGTH_SHORT).show()

            Handler().postDelayed({
                val bikeActivityFragment = BikeActivityFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, bikeActivityFragment)
                    .addToBackStack(null)
                    .commit()
            }, 1000)
        }

        return view
    }
}