package com.jasoncsullivan.mymaps

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jasoncsullivan.mymaps.models.UserMap

fun dropPinEffect(marker: Marker) {
    // Handler allows us to repeat a code block after a specified delay
    val handler = Handler()
    val start = SystemClock.uptimeMillis()
    val duration: Long = 1500

    // Use the bounce interpolator
    val interpolator: Interpolator = BounceInterpolator()

    // Animate marker with a bounce updating its position every 15ms
    handler.post(object : Runnable {
        override fun run() {
            val elapsed = SystemClock.uptimeMillis() - start
            // Calculate t for bounce based on elapsed time
            val t = Math.max(
                1 - interpolator.getInterpolation(
                    elapsed.toFloat()
                            / duration
                ), 0f
            )
            // Set the anchor
            marker.setAnchor(0.5f, 1.0f + 14 * t)
            if (t > 0.0) {
                // Post this event again 15ms from now.
                handler.postDelayed(this, 15)
            } else { // done elapsing, show window
                marker.showInfoWindow()
            }
        }
    })
}

private const val TAG = "DisplayMapActivity"
class DisplayMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var userMap: UserMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_map)

        userMap = intent.getSerializableExtra(EXTRA_USER_MAP) as UserMap

        supportActionBar?.title = userMap.title
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Log.i(TAG, "User map to render: ${userMap.title}")

        val boundsBuilder = LatLngBounds.Builder()
        for (place in userMap.places) {
            val latLng = LatLng(place.latitude, place.longitude)
            boundsBuilder.include(latLng)
            val marker = mMap.addMarker(MarkerOptions().position(latLng).title(place.title).snippet(place.description))
            dropPinEffect(marker);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 1000, 1000, 0))
    }

}