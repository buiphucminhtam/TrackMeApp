package com.fossil.trackme.data.services

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fossil.trackme.R
import com.fossil.trackme.base.BaseService
import com.fossil.trackme.data.models.*
import com.fossil.trackme.data.repositories.MapsRepository
import com.fossil.trackme.utils.checkLocationPermission
import com.fossil.trackme.utils.distance
import java.lang.Runnable

class LocationService : BaseService(), LocationListener {
    private val repo by lazy { MapsRepository.INSTANCE }

    private lateinit var locationManager: LocationManager
    private val listLocation = arrayListOf<LatLongEntity>()
    private var lastLocation: LatLongEntity? = null
    private var isInit = false
    private val handler = Handler()
    private var runnableUpdateTime: Runnable?=null
    private var notificationLayout:RemoteViews?=null


    companion object {
        const val TAG = "LocationService"
        const val ACTION_START = "START"
        const val ACTION_PAUSE = "PAUSE"
        const val LOCATION_DATA = "DATA"
        const val TIME_DATA = "TIME"
        const val DISTANCE_DATA = "DISTANCE"
        const val ACTION_SENDDATA = "SEND_DATA"
        const val ACTION_UPDATE_TIME = "TIME"
        const val MAX_TRACKING_LATLONG = 2
        var sessionId = 0L
        var isServiceRunning = false
        var isStart = false
        var totalDistance = 0f //Meter
        var totalTime = 0L //second
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartService")
        isServiceRunning = true
        intent?.let {
            when (it.action) {
                ACTION_START -> {
                    isStart = true
                    if (!isInit) {
                        isInit = true
                        isServiceRunning = true
                        totalDistance = 0f
                        totalTime = 0L
                        //Init Timer Task
                        locationManager =
                            getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        requestUpdateLocation()
                        createNotification()
                        insertTrackSession()
                    }
                    //Start timer
                    startTimer()
                }

                ACTION_PAUSE -> {
                    isStart = false
                    stopTimer()
                }

                else -> {}
            }
        }
        return START_STICKY
    }

    private fun startTimer() {
        runnableUpdateTime = Runnable {
            totalTime += 1
            LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(Intent().apply {
                action = ACTION_UPDATE_TIME
                putExtra(TIME_DATA,totalTime)
            })
           startDelayTask()
        }
        startDelayTask()
    }

    private fun startDelayTask(){
        runnableUpdateTime?.let {
            handler.postDelayed(it,1000)
        }
    }

    private fun stopTimer() {
        runnableUpdateTime?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun requestUpdateLocation() {
        if (applicationContext.checkLocationPermission()) {
            Log.e(TAG, "request update location")
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                10f,
                this
            )
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.toLatLongEntity()
            saveAndSendBroadCastLocation()
        }
    }


    private fun createNotification() {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher

        val channel: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel() else ""

        val showTaskIntent = Intent(
            applicationContext,
            LocationService::class.java
        )
        showTaskIntent.action = "Pause"
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val contentIntent = PendingIntent.getService(
            applicationContext,
            996,
            showTaskIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        var notification: Notification? = null
        notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationLayout =
                RemoteViews(packageName, R.layout.notification_running)
            notificationLayout?.setOnClickPendingIntent(R.id.tvPause, contentIntent)
            Notification.Builder(applicationContext, channel)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Track me is running ")
                .setStyle(Notification.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setColor(resources.getColor(R.color.colorPrimaryDark))
                .setSmallIcon(R.drawable.ic_pause)
                .setWhen(System.currentTimeMillis())
                .build()
        } else {
            Notification.Builder(applicationContext)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Track me is running ")
                .setSmallIcon(R.drawable.ic_pause)
                .setWhen(System.currentTimeMillis())
                .setColor(resources.getColor(R.color.colorPrimaryDark))
                .addAction(
                    Notification.Action(
                        R.drawable.ic_pause,
                        "OverlayIcon",
                        contentIntent
                    )
                )
                .build()
        }
        startForeground(1, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Synchronized
    private fun createChannel(): String {
        val mNotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val name = "tracking location "
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel =
            NotificationChannel("tracking channel", name, importance)
        mChannel.enableLights(true)
        mChannel.lightColor = Color.BLUE
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel)
        } else {
            stopSelf()
        }
        return "tracking channel"
    }

    override fun onLocationChanged(location: Location?) {
        Log.e(TAG, "OnLocationChange: $location")
        //On Update location
        location?.let {updateDistance(it.toLatLongEntity())}
        saveAndSendBroadCastLocation()
    }

    private fun updateDistance(newLocation: LatLongEntity) {
        lastLocation?.let {
            val distance = distance(it.lat,it.long,newLocation.lat,newLocation.long)
            totalDistance += distance
            newLocation.speed = (totalDistance / totalTime) * (18 / 5) // 18/5 is value to convert m/s to km/h
            lastLocation = newLocation
        }?:kotlin.run {
            lastLocation = newLocation
        }
    }

    private fun saveAndSendBroadCastLocation() {
        //Save location and send broad cast to activity
        if (isStart) {
            lastLocation?.let {
                listLocation.add(it)
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().apply {
                    action = ACTION_SENDDATA
                    putExtra(LOCATION_DATA, it)
                    putExtra(DISTANCE_DATA, totalDistance)
                })
            }
            if (listLocation.size == MAX_TRACKING_LATLONG) {
                //Create thread to insert list latlong to db
                insertListLatLong()
            }
        }
    }

    private fun insertTrackSession() {
        sessionId = System.currentTimeMillis()
        async {
            repo.insertTrackSession(TrackSession(sessionId,"",totalTime,0f,totalDistance))
            Log.e(TAG,"insertTrackSession")
        }
    }

    private fun insertListLatLong() {
        async {
            val convertArray = listLocation.toListLatLongFromLatLongEntity(sessionId).toTypedArray()
            Log.e(TAG,"insert arr: $convertArray")
            val result = repo.insertArrLatLong(convertArray)
            listLocation.clear()
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.e(TAG, "onStatusChanged status: $p0")
    }

    override fun onProviderEnabled(p0: String?) {
        Log.e(TAG, "onProviderEnabled: $p0")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.e(TAG, "onProviderDisabled: $p0")
    }

    override fun onDestroy() {
        insertListLatLong()
        stopTimer()
        isServiceRunning = false
        super.onDestroy()
    }
}