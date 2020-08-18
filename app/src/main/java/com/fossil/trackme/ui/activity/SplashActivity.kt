package com.fossil.trackme.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.fossil.trackme.base.BaseActivity
import com.fossil.trackme.data.services.LocationService

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LocationService.isServiceRunning) {
            startActivity(Intent(this, MapsActivity::class.java))
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun initViews() {
    }

    override fun observeData() {
    }
}