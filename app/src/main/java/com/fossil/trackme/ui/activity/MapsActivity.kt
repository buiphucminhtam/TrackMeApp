package com.fossil.trackme.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fossil.trackme.R
import com.fossil.trackme.base.BaseActivity
import com.fossil.trackme.data.models.CaptureEntity
import com.fossil.trackme.data.models.LatLongEntity
import com.fossil.trackme.data.models.TrackingSessionEntity
import com.fossil.trackme.data.services.LocationService
import com.fossil.trackme.data.viewmodel.MapsViewModel
import com.fossil.trackme.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : BaseActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var lastLocation: LatLongEntity? = null
    private var isStartFollow = true //Check is start or pause to move camera
    private var totalTime = 0L //Second
    private var totalDistance = 0f //Meter
    private var listLocationToDrawPath = arrayListOf<LatLongEntity>()
    private lateinit var viewModel: MapsViewModel
    private var currentTrackingSession: TrackingSessionEntity? = null

    private val broadCastUpdateLocation = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            //receive intent from service
            Log.e(TAG, "receive data action")
            p1?.let {
                val newLocation: LatLongEntity? = it.getParcelableExtra(LocationService.LOCATION_DATA)
                totalDistance = it.getFloatExtra(LocationService.DISTANCE_DATA, 0f)
                if (newLocation != null) {
                    listLocationToDrawPath.add(newLocation)
                    if (isStartFollow) {
                        onLocationChanged()
                        updateDistance()
                        updateSpeed()
                    }
                }

            }
        }
    }

    private val broadcastUpdateTime = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.e(TAG, "receive data update time")
            p1?.let {
                totalTime = it.getLongExtra(LocationService.TIME_DATA, 0)
                updateTime()
            }
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val ZOOM_DEFAULT_VALUE = 17f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel(MapsViewModel::class.java)
        setContentView(R.layout.activity_maps)
        initMaps()

    }


    @SuppressLint("ServiceCast")
    private fun initMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun initViews() {
        btnPause.setOnClickListener {
           changeStatePause()
            startService(Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_PAUSE
            })
        }

        btnResume.setOnClickListener {
            changeStateStart()
            startService(Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_START
            })
        }

        btnStop.setOnClickListener {
            //Create image and save to database
            if(listLocationToDrawPath.size == 0 ){
                startActivity(Intent(this,HomeActivity::class.java))
                finish()
                return@setOnClickListener
            }
            onStopTrackingSession()
        }
    }

    private fun changeStatePause() {
        isStartFollow = false
        btnPause.post { btnPause.visibility = View.GONE }
        groupPaused.post { groupPaused.visibility = View.VISIBLE }
    }

    private fun changeStateStart() {
        isStartFollow = true
        btnPause.post { btnPause.visibility = View.VISIBLE }
        groupPaused.post { groupPaused.visibility = View.GONE }
    }

    override fun observeData() {
        viewModel.listLatLong.observe(this, Observer {
            it?.run {
                listLocationToDrawPath.clear()
                listLocationToDrawPath.addAll(0, this)
                initFirstDraw()
                onLocationChanged()
            }
        })

        viewModel.trackingSession.observe(this, Observer {
            it?.run {
                currentTrackingSession = this
            }
        })

        viewModel.onInsertTrackSession.observe(this, Observer {
            if (it) {
                startActivity(Intent(this,HomeActivity::class.java))
                finish()
            }
        })

        viewModel.captureEntity.observe(this, Observer {
            it?.run {
                saveTrackingSession(this)
            }
        })
    }

    private fun startService() {
        if (!LocationService.isServiceRunning) {
            startService(Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_START
            })
        } else {
            //call get list latlong from database for updating view
            viewModel.getCurrentTrackSessionAndListLatLong(LocationService.sessionId)
            //Check state for loading state
            if(!LocationService.isStart) changeStatePause()
            totalDistance = LocationService.totalDistance
            totalTime = LocationService.totalTime
            updateDistance()
            updateSpeed()
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
            with(it.uiSettings) {
                if(checkLocationPermission())
                    mMap?.isMyLocationEnabled = true
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
                isZoomGesturesEnabled = true
                isScrollGesturesEnabled = true
                isRotateGesturesEnabled = true
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Start service
                    initMaps()
                    startService()
                }
            } else {
                // permission denied,
                openAppPermissionSetting()
            }
        }
    }

    fun onLocationChanged() {
        Log.e("MapsActivity", "OnLocationChange: ${lastLocation}")
        //On Update location
        if (isStartFollow) {
            //Follow me when enable follow
            lastLocation?.let {
                while (listLocationToDrawPath.isNotEmpty()) {
                    val newLocation = listLocationToDrawPath[0]
                    val currentLatLong = LatLng(it.lat, it.long)
                    val newLatLong = LatLng(newLocation.lat, newLocation.long)
                    mMap?.run {
                        //Move camera
                        moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLong, ZOOM_DEFAULT_VALUE))
                        //Draw line on map
                        addPolyline(PolylineOptions().add(currentLatLong, newLatLong).width(5f).color(Color.RED))
                    }
                    lastLocation = newLocation
                    listLocationToDrawPath.removeAt(0)
                }
            } ?: kotlin.run {
                initFirstDraw()
            }
        }
    }

    private fun initFirstDraw() {
        if (listLocationToDrawPath.isNotEmpty()) {
            lastLocation = listLocationToDrawPath[0]
            listLocationToDrawPath.removeAt(0)
            mMap?.let {
                lastLocation?.run {
                    it.addMarker(MarkerOptions().position(LatLng(lat,long)).anchor(0.5f,1f))
                }

            }
        }
    }

    private fun updateDistance() {
        //Udate view
        tvDistance.text = "${String.format("%.2f", (totalDistance / 1000))} Km"
    }

    private fun updateSpeed() {
        tvAvgSpeed.text = "${lastLocation?.speed} Km/h"
    }

    private fun updateTime() {
        tvTime.text = secondToTimeString(totalTime)
    }

    private fun onStopTrackingSession() {
        Log.e(TAG,"onStopTrackingSession")
       doHandleCaptureMapView()
    }

    private fun doHandleCaptureMapView() {
        viewModel.handleViewCapture(LocationService.sessionId)
    }

    private fun saveTrackingSession(captureEntity: CaptureEntity) {
        isStartFollow = false
        val sessionId = LocationService.sessionId
        stopService(Intent(this, LocationService::class.java))
        mMap?.run {
            moveCamera(CameraUpdateFactory.newLatLngZoom(captureEntity.latLng, captureEntity.zoomValue.toFloat()))
            setOnCameraIdleListener {
                setOnMapLoadedCallback {
                    snapshot {
                        Log.e(TAG,"On Snapshot ready: $it")
                        it?.run {
                            val base64StringConverted = BitMapToString(it)
                            Log.e(TAG,"converted $base64StringConverted")
                            if(base64StringConverted != null) {
                                currentTrackingSession = TrackingSessionEntity(sessionId, arrayListOf(),base64StringConverted,null,totalTime,(totalDistance / totalTime) * (18 / 5), totalDistance)
                                viewModel.updateTrackSession(currentTrackingSession!!)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadCastUpdateLocation,
            IntentFilter(LocationService.ACTION_SENDDATA)
        )
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastUpdateTime, IntentFilter(LocationService.ACTION_UPDATE_TIME))

        if (checkLocationPermissionWithRequest()) {
            startService()
        }
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastUpdateLocation)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastUpdateTime)
        super.onStop()
    }
}