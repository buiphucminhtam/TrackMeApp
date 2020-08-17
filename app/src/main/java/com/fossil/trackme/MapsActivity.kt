package com.fossil.trackme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.fossil.trackme.utils.checkLocationPermission
import com.fossil.trackme.utils.openAppPermissionSetting
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private var mMap: GoogleMap? = null
    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null
    private var isStartFollow = true

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
        val ZOOM_DEFAULT_VALUE = 17.5f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestUpdateLocation()
        initMaps()
    }

    @SuppressLint("ServiceCast")
    fun initMaps() {
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
        Log.e("MapsActivity","On map ready")
        mMap = googleMap
        lastLocation?.run {
            val currentLatLong = LatLng(latitude,longitude)
            mMap?.let {
                if (checkLocationPermission()) {
                    it.isMyLocationEnabled = true
                }
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,
                    ZOOM_DEFAULT_VALUE))
                with(it.uiSettings) {
                    isZoomControlsEnabled = true
                    isCompassEnabled = true
                    isMyLocationButtonEnabled = true
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                    isRotateGesturesEnabled = true
                }
            }

        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {

                        //Request location updates:
                        requestUpdateLocation()
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    openAppPermissionSetting()
                }
                return
            }
        }
    }

    private fun requestUpdateLocation() {
        if (checkLocationPermission()) {
            Log.e("MapsActivty", "request update location")
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                this
            )
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
    }


    override fun onLocationChanged(location: Location?) {
        Log.e("MapsActivity","OnLocationChange")
        //On Update location
        lastLocation = location ?: lastLocation
        if (isStartFollow) {
            //Follow me when enable follow
            lastLocation?.let {
                val currentLatLong = LatLng(it.latitude,it.longitude)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,
                    ZOOM_DEFAULT_VALUE))
            }

        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.e("MapsActivity","onStatusChanged")
    }

    override fun onProviderEnabled(p0: String?) {
        Log.e("MapsActivity","onProviderEnabled")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.e("MapsActivity","onProviderDisabled")
    }
}