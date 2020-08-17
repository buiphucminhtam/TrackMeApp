package com.fossil.trackme.data.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData

import com.fossil.trackme.data.base.BaseViewModel
import com.fossil.trackme.data.repositories.HomeRepository

class HomeViewModel : BaseViewModel() {
    private val repo by lazy { HomeRepository.INSTANCE }


    init {

    }

}