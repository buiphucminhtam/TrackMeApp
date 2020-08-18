package com.fossil.trackme.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fossil.trackme.data.base.BaseViewModel
import com.fossil.trackme.data.models.TrackSessionEntity
import com.fossil.trackme.data.models.toListTrackSessionEntity
import com.fossil.trackme.data.repositories.MapsRepository

class HomeViewModel : BaseViewModel() {
    private val repo by lazy { MapsRepository.INSTANCE }
    private val _listTrackSession = MutableLiveData<List<TrackSessionEntity>>()

    val listTrackSession : LiveData<List<TrackSessionEntity>>
    get() = _listTrackSession

    fun getListTrackSessionEntity() {
        async {
            //Currently just get list session to view (now lis latlong in tracksession is empty) so if need to go to detail -> need to create one function to get list latlong from id of session
            val listTrackSession = repo.getListTrackSession()
            _listTrackSession.postValue(listTrackSession.toListTrackSessionEntity())
        }
    }

}