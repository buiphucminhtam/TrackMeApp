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
import com.fossil.trackme.utils.checkLocationPermission
import java.util.*

class LocationService : Service(), LocationListener {
    private val binder = LocalBinder()
    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null
    private var timeString = ""
    private var isStart = false
    private var isInit = false
    private var totalTime = 0L //second
    private val handler = Handler()
    private var runnableUpdateTime: Runnable?=null

    companion object {
        const val ACTION_START = "START"
        const val ACTION_PAUSE = "PAUSE"
        const val LOCATION_DATA = "DATA"
        const val TIME_DATA = "TIME"
        const val ACTION_SENDDATA = "SEND_DATA"
        const val ACTION_UPDATE_TIME = "TIME"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("LocationService", "onStartService")
        intent?.let {
            when (it.action) {
                ACTION_START -> {
                    isStart = true
                    if (!isInit) {
                        isInit = true
                        //Init Timer Task
                        locationManager =
                            getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        requestUpdateLocation()
                        createNotification()
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
            handler.postDelayed(runnableUpdateTime,1000)
        }
        handler.postDelayed(runnableUpdateTime,1000)
    }

    private fun stopTimer() {
        runnableUpdateTime?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun requestUpdateLocation() {
        if (applicationContext.checkLocationPermission()) {
            Log.e("LocationService", "request update location")
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                10f,
                this
            )
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            saveAndSendBroadCastLocation()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): LocationService = this@LocationService
    }

    private fun createNotification() {
// Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher

        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        val channel: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel() else {
                ""
            }

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
            val notificationLayout =
                RemoteViews(packageName, R.layout.notification_running)
            notificationLayout.setOnClickPendingIntent(R.id.tvPause, contentIntent)
            Notification.Builder(applicationContext, channel)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Time: ")
                .setStyle(Notification.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setColor(resources.getColor(R.color.colorPrimaryDark))
                .setSmallIcon(R.drawable.ic_pause)
                .setWhen(System.currentTimeMillis())
                .build()
        } else {
            Notification.Builder(applicationContext)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Time: $timeString")
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
        val name = "snap map fake location "
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel =
            NotificationChannel("snap map channel", name, importance)
        mChannel.enableLights(true)
        mChannel.lightColor = Color.BLUE
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel)
        } else {
            stopSelf()
        }
        return "snap map channel"
    }

    override fun onLocationChanged(location: Location?) {
        Log.e("LocationService", "OnLocationChange")
        //On Update location
        lastLocation = location ?: lastLocation
        saveAndSendBroadCastLocation()
    }

    private fun saveAndSendBroadCastLocation() {
        //Save location and send broad cast to activity
        if (isStart)
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().apply {
                action = ACTION_SENDDATA
                putExtra(LOCATION_DATA, lastLocation)
            })
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.e("LocationService", "onStatusChanged status: $p0")
    }

    override fun onProviderEnabled(p0: String?) {
        Log.e("LocationService", "onProviderEnabled: $p0")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.e("LocationService", "onProviderDisabled: $p0")
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }
}