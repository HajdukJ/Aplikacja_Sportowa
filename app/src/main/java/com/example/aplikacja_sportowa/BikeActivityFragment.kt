package com.example.aplikacja_sportowa

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.graphics.Color

class BikeActivityFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var startButton: Button
    private lateinit var finishButton: Button
    private var isRiding = false
    private var startTime: Long = 0
    private var distanceTraveled: Float = 0f
    private var lastLocation: Location? = null
    private val polylineOptions = PolylineOptions().color(Color.CYAN).width(15f)
    private var polyline: Polyline? = null
    private val locList = mutableListOf<LatLng>()
    private var isCountingDown = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 17f
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_bike_activity, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        startButton = rootView.findViewById(R.id.startButton)
        finishButton = rootView.findViewById(R.id.finishButton)

        startButton.setOnClickListener {
            onStartClick()
        }

        finishButton.setOnClickListener {
            onFinishClick()
        }

        return rootView
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun enableUserLocation() {
        googleMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
            }
        }
    }

    private fun onStartClick() {
        if (!isRiding) {
            resetActivity(true)
            isRiding = true
            startTime = System.currentTimeMillis()
            polyline = googleMap?.addPolyline(polylineOptions)
            startCountdown()
            startLocationUpdates()
        } else {
            resetActivity(true)
            isRiding = true
            startTime = System.currentTimeMillis()
            polyline = googleMap?.addPolyline(polylineOptions)
            startCountdown()
            startLocationUpdates()
        }
    }

    private fun onFinishClick() {
        if (isRiding) {
            isRiding = false
            stopLocationUpdates()
            saveCyclingDataToFirebase()
        }
    }

    private fun startCountdown() {
        isCountingDown = true
        val countdown = arrayOf(3, 2, 1, "START!")
        var index = 0
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (index < countdown.size) {
                    val text = countdown[index].toString()
                    val countdownText = view?.findViewById<TextView>(R.id.titleText)
                    countdownText?.text = text
                    index++
                    handler.postDelayed(this, 1000)
                } else {
                    isCountingDown = false
                }
            }
        })
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    if (isRiding && !isCountingDown) {
                        updateLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun updateLocation(location: Location) {
        if (lastLocation != null) {
            locList.add(LatLng(location.latitude, location.longitude))
            polyline?.points = locList
            distanceTraveled += lastLocation!!.distanceTo(location)
            val timeElapsed = (System.currentTimeMillis() - startTime) / 1000
            view?.findViewById<TextView>(R.id.timeLayout)?.text = String.format(
                "TIME:\n%02d:%02d:%02d sec",
                timeElapsed / 3600,
                (timeElapsed % 3600) / 60,
                timeElapsed % 60
            )
            view?.findViewById<TextView>(R.id.distanceLayout)?.text = String.format(
                "DISTANCE:\n%.2f km", distanceTraveled / 1000
            )
            val speed = location.speed * 3.6f
            view?.findViewById<TextView>(R.id.speedLayout)?.text = String.format(
                "SPEED:\n%.2f km/h", speed
            )
        }
        lastLocation = location
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
    }

    private fun saveCyclingDataToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val databaseReference = FirebaseDatabase.getInstance().getReference("users/${currentUser?.uid}/cycling")
        val cyclingData = mapOf(
            "date" to System.currentTimeMillis(),
            "distance" to distanceTraveled / 1000,
            "time" to (System.currentTimeMillis() - startTime) / 1000,
            "speed" to distanceTraveled / (System.currentTimeMillis() - startTime) * 3600f,
            "route" to locList.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
        )
        databaseReference.push().setValue(cyclingData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cycling data saved to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to save cycling data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetActivity(clearPolyline: Boolean = false) {
        distanceTraveled = 0f
        lastLocation = null
        locList.clear()
        view?.findViewById<TextView>(R.id.speedLayout)?.text = "SPEED:\n0.00 km/h"
        view?.findViewById<TextView>(R.id.distanceLayout)?.text = "DISTANCE:\n0.00 km"
        view?.findViewById<TextView>(R.id.timeLayout)?.text = "TIME:\n00:00:00 sec"

        if (clearPolyline) {
            polyline?.remove()
            polyline = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        }
    }
}