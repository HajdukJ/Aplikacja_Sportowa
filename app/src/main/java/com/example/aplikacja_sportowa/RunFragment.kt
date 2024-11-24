
package com.example.aplikacja_sportowa

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

class RunFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_run, container, false)

        val runningButton = view.findViewById<Button>(R.id.runningButton)
        val cyclingButton = view.findViewById<Button>(R.id.cyclingButton)

        runningButton.setOnClickListener {
            Toast.makeText(context, "Current activity: Running", Toast.LENGTH_SHORT).show()
        }

        cyclingButton.setOnClickListener {
            Toast.makeText(context, "Current activity: Cycling", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}