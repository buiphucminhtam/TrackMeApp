package com.fossil.trackme.data.database

import androidx.room.*
import com.fossil.trackme.data.models.LatLong
import com.fossil.trackme.data.models.TrackSession
import com.fossil.trackme.data.models.TrackSessionAndLatLong

@Dao
interface TrackSessionDao {
    @Query("Select * from TrackSession")
    suspend fun getListTrackSession(): List<TrackSession>

    @Insert
    suspend fun insertTrackSession(trackSession: TrackSession)

    @Update
    suspend fun updateTrackSession(trackSession: TrackSession)

    @Delete
    suspend fun deleteTrackSession(trackSession: TrackSession)
}
@Dao
interface LatLongDAO{

    @Query("Select * from LatLong WHERE trackSessionId = :sessionId order by trackSessionId ASC")
    suspend fun getListLatLong(sessionId:Long) : List<LatLong>

    @Insert
    suspend fun insertLatLong(latLong: LatLong)

    @Insert
    suspend fun insertArrLatLong(lastLong:Array<LatLong>)

    @Delete
    suspend fun deleteLatLong(latLong: LatLong)
}

@Dao
interface TrackSessionAndLatLongDao {
    @Transaction
    @Query("SELECT * from TrackSession WHERE id = :id order by id ASC")
    suspend fun getListTrackSessionAndLatLong(id: Long): List<TrackSessionAndLatLong>
}