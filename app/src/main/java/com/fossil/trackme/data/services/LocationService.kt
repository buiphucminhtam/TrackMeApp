package com.fossil.trackme.data.services

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.fossil.trackme.R

class LocationService : Service(), LocationListener {
    private val binder = LocalBinder()
    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null
    private var timeString = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return START_STICKY
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
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        //Save location and send broad cast to activity
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.e("LocationService", "onStatusChanged")
    }

    override fun onProviderEnabled(p0: String?) {
        Log.e("LocationService", "onProviderEnabled")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.e("LocationService", "onProviderDisabled")
    }
}