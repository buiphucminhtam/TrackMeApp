package com.fossil.trackme.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fossil.trackme.data.models.LatLong
import com.fossil.trackme.data.models.TrackSession
@Database(entities = [TrackSession::class, LatLong::class], version = 1, exportSchema = false)
abstract class MapsDatabase : RoomDatabase() {
    companion object {
        private val DB_NAME = "tracking_db"
        private var instance : MapsDatabase ? = null
        fun getInstance(applicationContext: Context): MapsDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(applicationContext,MapsDatabase::class.java, DB_NAME).build()
            }
            return instance as MapsDatabase
        }
    }

    abstract fun trackSessionDAO(): TrackSessionDao

    abstract fun trackSessionAndLatLongDAO(): TrackSessionAndLatLongDao

    abstract fun latLongDAO():LatLongDAO
}