package com.example.aplikacja_sportowa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream

/**
 * Fragment odpowiedzialny za aktywność biegania.
 * Zawiera funkcje do śledzenia lokalizacji na mapie, liczenia kroków,
 * obliczania dystansu, czasu, tempa oraz zapisania danych do Firebase.
 */
class RunActivityFragment : Fragment(), OnMapReadyCallback, SensorEventListener {

    // Deklaracja zmiennych
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var startButton: Button
    private lateinit var finishButton: Button
    private var isRunning = false
    private var startTime: Long = 0
    private var distanceTraveled: Float = 0f
    private var lastLocation: Location? = null
    private val polylineOptions = PolylineOptions().color(Color.CYAN).width(15f)
    private var polyline: Polyline? = null
    private val locList = mutableListOf<LatLng>()
    private var isCountingDown = false
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepsCount = 0
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "step_counter_channel"
    private var lastPace: String = "00:00"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 17f  // Domyślny poziom zoomu mapy
    }

    /**
     * Funkcja wywoływana przy tworzeniu widoku fragmentu.
     * Inicjalizuje mapę, przyciski i inne elementy interfejsu.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_run_activity, container, false)

        // Inicjalizacja zmiennych
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        startButton = rootView.findViewById(R.id.startButton)
        finishButton = rootView.findViewById(R.id.finishButton)

        // Obsługa kliknięć przycisków
        startButton.setOnClickListener { onStartClick() }
        finishButton.setOnClickListener { onFinishClick() }

        // Inicjalizacja menedżera sensorów
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Inicjalizacja menedżera powiadomień
        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        return rootView
    }

    /**
     * Funkcja wywoływana po załadowaniu mapy.
     * Sprawdza uprawnienia do lokalizacji i uruchamia lokalizację użytkownika.
     */
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

    /**
     * Funkcja włącza śledzenie lokalizacji użytkownika na mapie.
     */
    private fun enableUserLocation() {
        googleMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
            }
        }
    }

    /**
     * Funkcja uruchamia aktywność biegu.
     * Inicjalizuje start, odliczanie, lokalizację i liczenie kroków.
     */
    private fun onStartClick() {
        if (!isRunning) {
            resetActivity(true)
            isRunning = true
            startTime = System.currentTimeMillis()
            polyline = googleMap?.addPolyline(polylineOptions)
            startCountdown()
            startLocationUpdates()
            startStepCounting()
        }
    }

    /**
     * Funkcja kończy aktywność biegu.
     * Zatrzymuje śledzenie lokalizacji, liczenie kroków i zapisuje dane.
     */
    private fun onFinishClick() {
        if (isRunning) {
            isRunning = false
            stopLocationUpdates()
            stopStepCounting()

            Log.d("RunActivity", "Saving map snapshot...")
            saveMapSnapshot("run_map_${System.currentTimeMillis()}") { file ->
                if (file != null) {
                    Log.d("RunActivity", "Map snapshot saved: ${file.absolutePath}")
                    saveRunDataToFirebase(file.absolutePath)
                } else {
                    Log.e("RunActivity", "Failed to save map snapshot.")
                }
            }
        }
    }

    /**
     * Funkcja uruchamia odliczanie przed rozpoczęciem biegu.
     */
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

    /**
     * Funkcja zaczyna pobierać aktualną lokalizację co sekundę.
     */
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    if (isRunning && !isCountingDown) {
                        updateLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    /**
     * Funkcja aktualizuje lokalizację na mapie.
     */
    private fun updateLocation(location: Location) {
        if (lastLocation != null) {
            locList.add(LatLng(location.latitude, location.longitude))
            polyline?.points = locList
            distanceTraveled += lastLocation!!.distanceTo(location)
            val timeElapsed = (System.currentTimeMillis() - startTime) / 1000
            val (hours, minutes, seconds) = convertSecondsToHMSTime(timeElapsed)
            view?.findViewById<TextView>(R.id.timeLayout)?.text = String.format("TIME:\n%02d:%02d:%02d sec", hours, minutes, seconds)
            view?.findViewById<TextView>(R.id.distanceLayout)?.text = String.format("DISTANCE:\n%.2f km", distanceTraveled / 1000)
            if (distanceTraveled >= 100) {
                val runningPace = calculatePace(distanceTraveled, timeElapsed)
                lastPace = runningPace
                view?.findViewById<TextView>(R.id.paceLayout)?.text = String.format("PACE:\n%s min/km", runningPace)
            }
        }
        lastLocation = location
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
    }

    /**
     * Funkcja oblicza tempo biegu na podstawie przebytego dystansu i upływającego czasu.
     */
    private fun calculatePace(distance: Float, time: Long): String {
        if (distance > 0) {
            val paceInMinutes = (time.toFloat() / 60) / (distance / 1000)
            val paceMinutes = paceInMinutes.toInt()
            val paceSeconds = ((paceInMinutes - paceMinutes) * 60).toInt()
            return String.format("%02d:%02d", paceMinutes, paceSeconds)
        }
        return "00:00"
    }

    /**
     * Funkcja konwertuje sekundy na format godzin, minut, sekund.
     */
    private fun convertSecondsToHMSTime(seconds: Long): Triple<Int, Int, Int> {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val remainingSeconds = (seconds % 60).toInt()
        return Triple(hours, minutes, remainingSeconds)
    }

    /**
     * Funkcja zatrzymuje pobieranie lokalizacji.
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
    }

    /**
     * Funkcja zapisuje dane biegu do Firebase.
     */
    private fun saveRunDataToFirebase(mapImagePath: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val databaseReference = FirebaseDatabase.getInstance().getReference("users/${currentUser?.uid}/runs")
        val runData = mapOf(
            "date" to System.currentTimeMillis(),
            "distance" to distanceTraveled / 1000,
            "time" to (System.currentTimeMillis() - startTime) / 1000,
            "pace" to lastPace,
            "mapImagePath" to mapImagePath,
            "route" to locList.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
        )
        databaseReference.push().setValue(runData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Run data saved to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to save run data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Funkcja resetuje aktywność, czyszcząc mapę i przywracając domyślne wartości.
     */
    private fun resetActivity(clearMap: Boolean) {
        if (clearMap) {
            locList.clear()
            googleMap?.clear()
            polyline = googleMap?.addPolyline(polylineOptions)
        }
        distanceTraveled = 0f
        startTime = 0
        lastLocation = null
    }

    /**
     * Funkcja tworzy powiadomienie.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Counter Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for step counter notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Funkcja rozpoczyna liczenie kroków przy pomocy sensora kroków.
     */
    private fun startStepCounting() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            updateNotification(stepsCount)
        }
    }

    /**
     * Funkcja zatrzymuje liczenie kroków i usuwa powiadomienie.
     */
    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Funkcja aktualizuje powiadomienie o liczbie kroków.
     */
    private fun updateNotification(stepCount: Int) {
        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Step Counter")
            .setContentText("Steps: $stepCount")
            .setSmallIcon(R.drawable.baseline_directions_run_24)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (isRunning && it.sensor == stepSensor) {
                stepsCount = it.values[0].toInt()
                updateNotification(stepsCount)
            }
        }
    }

    /**
     * Funkcja zapisuje zrzut mapy do pliku.
     */
    private fun saveMapSnapshot(fileName: String, onSaved: (File?) -> Unit) {
        googleMap?.snapshot { bitmap ->
            if (bitmap != null) {
                try {
                    val file = File(requireContext().getExternalFilesDir(null), "$fileName.png")
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    Toast.makeText(requireContext(), "Mapa zapisana: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                    onSaved(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onSaved(null)
                }
            } else {
                onSaved(null)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}