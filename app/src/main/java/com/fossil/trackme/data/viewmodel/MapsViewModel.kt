package com.fossil.trackme.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fossil.trackme.base.BaseViewModel
import com.fossil.trackme.data.models.*
import com.fossil.trackme.data.repositories.MapsRepository

class MapsViewModel : BaseViewModel() {
    private val repo by lazy { MapsRepository.INSTANCE }

    private val _listLatLong = MutableLiveData<List<LatLongEntity>>()
    private val _trackSession = MutableLiveData<TrackingSessionEntity>()
    private val _onInsertedTrackSession = MutableLiveData<Boolean>()

    val listLatLong: LiveData<List<LatLongEntity>>
        get() = _listLatLong

    val trackingSession: LiveData<TrackingSessionEntity>
        get() = _trackSession

    val onInsertTrackSession : LiveData<Boolean>
        get() = _onInsertedTrackSession

    fun getCurrentTrackSessionAndListLatLong(sessionId: Long) {
        async {
            val response = repo.getTrackSessionDB(sessionId)
            Log.e("MapsViewModel","getCurrentTrackSessionAndListLatLong: $sessionId response: $response")
            response?.let {
                val list = repo.getListLatLong(it.id)
                Log.e("MapsViewModel","getlistLatLong: $list")
                list?.run {
                    _listLatLong.postValue(toListLatLongEntity())
                }
                _trackSession.postValue(it.toTrackSessionEntity())

            }
        }
    }

    fun updateTrackSession(trackingSessionEntity: TrackingSessionEntity) {
        async {
            Log.e("MapsViewModel","insertTrackSession")
            //Get list lat long to calculate avg speed
            val listLatLong = repo.getListLatLong(trackingSessionEntity.id)
            var avgSpeed = 0F
            listLatLong?.let {
                for (item in listLatLong) {
                    avgSpeed+=item.currentSpeed?:0F
                }
                avgSpeed/listLatLong.size
                trackingSessionEntity.avgSpeed = avgSpeed
            }
            repo.updateTrackSession(trackSession = trackingSessionEntity.toTrackSession())
            _onInsertedTrackSession.postValue(true)
        }
    }
}