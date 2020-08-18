package com.fossil.trackme.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity: AppCompatActivity(), BaseView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initViews()
        observeData()
    }

}