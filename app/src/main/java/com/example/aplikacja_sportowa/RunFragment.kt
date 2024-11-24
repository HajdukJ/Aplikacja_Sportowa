package com.example.aplikacja_sportowa

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class RunFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_run, container, false)

        val runningButton = view.findViewById<Button>(R.id.runningButton)
        val cyclingButton = view.findViewById<Button>(R.id.cyclingButton)

        runningButton.setOnClickListener {
            Toast.makeText(context, "You chose Running!", Toast.LENGTH_SHORT).show()

            Handler().postDelayed({
                val runActivityFragment = RunActivityFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, runActivityFragment)
                    .addToBackStack(null)
                    .commit()
            }, 1000)
        }

        cyclingButton.setOnClickListener {
            Toast.makeText(context, "You chose Cycling!", Toast.LENGTH_SHORT).show()

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