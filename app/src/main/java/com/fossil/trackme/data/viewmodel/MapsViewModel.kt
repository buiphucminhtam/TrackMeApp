package com.fossil.trackme.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fossil.trackme.base.BaseViewModel
import com.fossil.trackme.data.models.*
import com.fossil.trackme.data.repositories.MapsRepository
import com.fossil.trackme.utils.distance
import com.fossil.trackme.utils.midPoint

class MapsViewModel : BaseViewModel() {
    private val repo by lazy { MapsRepository.INSTANCE }

    private val _listLatLong = MutableLiveData<List<LatLongEntity>>()
    private val _trackSession = MutableLiveData<TrackingSessionEntity>()
    private val _onInsertedTrackSession = MutableLiveData<Boolean>()
    private val _captureEntity = MutableLiveData<CaptureEntity?>()


    val listLatLong: LiveData<List<LatLongEntity>>
        get() = _listLatLong

    val trackingSession: LiveData<TrackingSessionEntity>
        get() = _trackSession

    val onInsertTrackSession : LiveData<Boolean>
        get() = _onInsertedTrackSession

    val captureEntity : LiveData<CaptureEntity?>
        get() = _captureEntity


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

    fun handleViewCapture(trackSessionId : Long){
        async {
            Log.e("MapsViewModel","handleViewCapture")
            val listLatLongResponse = repo.getListLatLong(trackSessionId)

            Log.e("MapsViewModel","list LatLong: ${listLatLongResponse.toString()}")
            //Check list latlong empty or not
            if (listLatLongResponse != null && listLatLongResponse.isNotEmpty()) {
                var minLat = listLatLongResponse[0].lattitude ?: 0.0
                var maxLat = listLatLongResponse[0].lattitude ?: 0.0
                var minLong = listLatLongResponse[0].lngtitude ?: 0.0
                var maxLong = listLatLongResponse[0].lngtitude ?: 0.0

                //Find 4 point min max
                listLatLongResponse?.let {
                    for (item in it) {
                        minLat = kotlin.math.min(minLat, item.lattitude ?: 0.0)
                        maxLat = kotlin.math.max(maxLat, item.lattitude ?: 0.0)
                        minLong = kotlin.math.min(minLong, item.lngtitude ?: 0.0)
                        maxLong = kotlin.math.max(maxLong, item.lngtitude ?: 0.0)
                    }
                }

                //Find 2 middle of 2 middle point
                val middleLong = midPoint(minLat, maxLong, minLat, minLong)
                val middleLat = midPoint(maxLat, minLong, maxLat, maxLong)
                val centerPoint = midPoint(
                    middleLat.latitude,
                    middleLat.longitude,
                    middleLong.latitude,
                    middleLong.longitude
                )


                //Find zoom value from distance from center point to other point ( in 4 point, now i get minLat and max Long)
                //Find R first
                val r = distance(centerPoint.latitude, centerPoint.longitude, minLat, maxLong)
                //So we have zoom value from r distance
                val zoomValue = when {
                    r > 0 && r <= 1000 -> 15.0
                    r > 1000 && r <= 5000 -> 12.5
                    r > 5000 && r <= 10000 -> 11.0
                    r > 10000 && r <= 50000 -> 8.5
                    r > 50000 && r <= 100000 -> 6.0
                    r > 100000 && r <= 500000 -> 5.5
                    else -> 3.0
                }

                Log.e(
                    "MapsViewModel",
                    "middleLong: $middleLong middleLat: $middleLat centerPoint: $centerPoint R: $r Zoom Value: $zoomValue"
                )
                _captureEntity.postValue(CaptureEntity(centerPoint, zoomValue))
            }else _captureEntity.postValue(null)
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