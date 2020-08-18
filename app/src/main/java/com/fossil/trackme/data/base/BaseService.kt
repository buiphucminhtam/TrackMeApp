package com.fossil.trackme.data.base

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.fossil.trackme.data.services.LocationService
import kotlinx.coroutines.*

abstract class BaseService: Service() {
    private val binder = LocalBinder()
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Default + viewModelJob)

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }


    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BaseService = this@BaseService
    }

    protected fun async(
        error: (throwable: Throwable) -> Unit = {},
        call: suspend () -> Unit = {}
    ): Deferred<*> {
        return viewModelScope.async {
            runCatching {
                call()
            }.onFailure {
                it.printStackTrace()
                error(it)
            }
        }
    }
}