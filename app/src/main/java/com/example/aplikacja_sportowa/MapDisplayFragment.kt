package com.example.aplikacja_sportowa

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MapDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_map_display)

        val mapImageView: ImageView = findViewById(R.id.mapImageView)
        val mapImagePath = intent.getStringExtra("mapImagePath")

        if (mapImagePath != null) {
            try {
                val bitmap = BitmapFactory.decodeFile(mapImagePath)
                if (bitmap != null) {
                    mapImageView.setImageBitmap(bitmap)
                } else {
                    Log.e("MapDisplayActivity", "Failed to decode bitmap from file: $mapImagePath")
                    Toast.makeText(this, "Unable to load the image.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MapDisplayActivity", "Error loading map image: ${e.message}")
                Toast.makeText(this, "Error loading map image.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("MapDisplayActivity", "Map image path is null.")
            Toast.makeText(this, "Invalid image path.", Toast.LENGTH_SHORT).show()
        }
    }
}