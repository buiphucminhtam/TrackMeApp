package com.fossil.trackme

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fossil.trackme.data.services.LocationService
import com.fossil.trackme.utils.checkLocationPermission
import com.fossil.trackme.utils.distance
import com.fossil.trackme.utils.openAppPermissionSetting
import com.fossil.trackme.utils.secondToTimeString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var lastLocation: Location? = null
    private var isStartFollow = true //Check is start or pause to move camera
    private var totalTime = 0L //Second
    private var totalDistance = 0f //Meter
    private var listLocationToDrawPath = arrayListOf<Location>()
    private val broadCastUpdateLocation = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            //receive intent from service
            Log.e(TAG,"receive data action")
            p1?.let {
                val newLocation: Location? = it.getParcelableExtra(LocationService.LOCATION_DATA)
                totalDistance = it.getFloatExtra(LocationService.DISTANCE_DATA,0f)
                if (newLocation != null) {
                    listLocationToDrawPath.add(newLocation)
                    onLocationChanged()
                    updateDistance()
                    updateSpeed()
                }

            }
        }
    }

    private val broadcastUpdateTime = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.e(TAG,"receive data update time")
            p1?.let {
                totalTime = it.getLongExtra(LocationService.TIME_DATA,0)
                tvTime.text = secondToTimeString(totalTime)
            }
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val ZOOM_DEFAULT_VALUE = 17.8f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initMaps()
        initView()
        if (checkLocationPermission() && isStartFollow) {
            startService(Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_START
            })
        }
    }

    @SuppressLint("ServiceCast")
    private fun initMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initView() {
        btnPause.setOnClickListener {
            btnPause.post { btnPause.visibility = View.GONE }
            groupPaused.post { groupPaused.visibility = View.VISIBLE }
            startService(Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_PAUSE
            })
        }

        btnResume.setOnClickListener {
            btnPause.post { btnPause.visibility = View.VISIBLE }
            groupPaused.post { groupPaused.visibility = View.GONE }
            startService(Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_START
            })
        }

        btnStop.setOnClickListener {
            //Create image and save to database
        }
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
        Log.e("MapsActivity", "On map ready")
        mMap = googleMap
        mMap?.let {
            if (checkLocationPermission()) {
                it.isMyLocationEnabled = true
            }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Start service
                        startService(Intent(this, LocationService::class.java))
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

    fun onLocationChanged() {
        Log.e("MapsActivity", "OnLocationChange: ${lastLocation}")
        //On Update location
        if (isStartFollow) {
            //Follow me when enable follow
            lastLocation?.let {
                val newLocation = listLocationToDrawPath[0]
                val currentLatLong = LatLng(it.latitude, it.longitude)
                val newLatLong = LatLng(newLocation.latitude,newLocation.longitude)
                mMap?.run {
                    //Move camera
                    moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, ZOOM_DEFAULT_VALUE))

                    //Draw line on map
                    addPolyline(PolylineOptions().add(currentLatLong,newLatLong).width(5f).color(Color.RED))
                }
                lastLocation = newLocation
                listLocationToDrawPath.removeAt(0)
            }?:kotlin.run {
                if(listLocationToDrawPath.isNotEmpty())
                    lastLocation = listLocationToDrawPath[0]
                listLocationToDrawPath.removeAt(0)
            }
        }
    }

    private fun updateDistance() {
        //Udate view
        tvDistance.text = "${String.format("%.2f", (totalDistance/1000))} Km"
    }

    private fun updateSpeed() {
        tvAvgSpeed.text = "${String.format("%.2f",(totalDistance/totalTime)*(18/5))} Km/h"
    }


    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastUpdateLocation, IntentFilter(LocationService.ACTION_SENDDATA))
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastUpdateTime, IntentFilter(LocationService.ACTION_UPDATE_TIME))
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastUpdateLocation)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastUpdateTime)
        super.onStop()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}