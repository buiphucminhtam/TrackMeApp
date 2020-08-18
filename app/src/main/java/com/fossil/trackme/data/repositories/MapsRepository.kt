package com.fossil.trackme.data.repositories

import com.fossil.trackme.AppApplication
import com.fossil.trackme.data.database.MapsDatabase
import com.fossil.trackme.data.models.LatLong
import com.fossil.trackme.data.models.TrackSession


class MapsRepository {

    companion object {
        val INSTANCE = MapsRepository()
    }

    suspend fun getListTrackSession() : List<TrackSession>? {
        return MapsDatabase.getInstance(AppApplication.INSTANCE.applicationContext).trackSessionDAO().getListTrackSession()
    }

    suspend fun getTrackSessionDB(id:Long): TrackSession? {
       return MapsDatabase.getInstance(AppApplication.INSTANCE.applicationContext).trackSessionDAO().getTrackSessionById(id)
    }

    suspend fun getListLatLong(sessionId:Long) : List<LatLong>? {
        return MapsDatabase.getInstance(AppApplication.INSTANCE.applicationContext).latLongDAO().getListLatLong(sessionId)
    }

    suspend fun insertTrackSession(trackSession: TrackSession) {
        MapsDatabase.getInstance(AppApplication.INSTANCE.applicationContext).trackSessionDAO().insertTrackSession(trackSession)
    }

    suspend fun updateTrackSession(trackSession: TrackSession) {
        MapsDatabase.getInstance(AppApplication.INSTANCE.applicationContext).trackSessionDAO().updateTrackSession(trackSession)
    }

    suspend fun insertArrLatLong(array: Array<LatLong>) {
        MapsDatabase.getInstance(AppApplication.INSTANCE.applicationContext).latLongDAO().insertArrLatLong(array)
    }

}