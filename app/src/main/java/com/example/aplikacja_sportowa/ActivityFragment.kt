package com.example.aplikacja_sportowa

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * @class ActivityFragment
 * Fragment odpowiedzialny za wybór aktywności, takich jak bieganie i jazda na rowerze.
 */
class ActivityFragment : Fragment() {

    /**
     * Tworzy widok fragmentu i ustawia akcje dla przycisków.
     *
     * @param inflater Obiekt LayoutInflater używany do wczytania widoku fragmentu.
     * @param container Opcjonalny widok nadrzędny, do którego fragment zostanie dodany.
     * @param savedInstanceState Jeśli fragment jest ponownie tworzony, zawiera dane z poprzedniej instancji.
     * @return Widok fragmentu.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Wczytanie layoutu fragmentu
        val view = inflater.inflate(R.layout.fragment_activity, container, false)

        // Referencje do przycisków w layout'cie
        val runningButton = view.findViewById<FrameLayout>(R.id.runningButton)
        val cyclingButton = view.findViewById<FrameLayout>(R.id.cyclingButton)

        // Ustawia działanie przycisku biegania
        runningButton.setOnClickListener {
            // Wyświetla informację o bieżącej aktywności
            Toast.makeText(context, "Current activity: Running", Toast.LENGTH_SHORT).show()

            // Przełącza na fragment RunActivityFragment po 1 sekundzie
            Handler().postDelayed({
                val runActivityFragment = RunActivityFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, runActivityFragment)
                    .addToBackStack(null)
                    .commit()
            }, 1000)
        }

        // Ustawia działanie przycisku jazdy na rowerze
        cyclingButton.setOnClickListener {
            // Wyświetla informację o bieżącej aktywności
            Toast.makeText(context, "Current Activity: Cycling", Toast.LENGTH_SHORT).show()

            // Przełącza na fragment BikeActivityFragment po 1 sekundzie
            Handler().postDelayed({
                val bikeActivityFragment = BikeActivityFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, bikeActivityFragment)
                    .addToBackStack(null)
                    .commit()
            }, 1000)
        }

        // Zwraca widok fragmentu
        return view
    }
}