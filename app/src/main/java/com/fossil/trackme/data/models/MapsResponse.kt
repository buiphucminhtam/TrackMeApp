package com.fossil.trackme.data.models

import androidx.room.*

@Entity(tableName = "latlong")
data class LatLong(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "trackSessionId")
    var trackSessionId:Long? = 0,
    @ColumnInfo(name = "lat")
    var lat: Double? = 0.0,
    @ColumnInfo(name = "long")
    var long: Double? = 0.0,
    @ColumnInfo(name = "speed")
    var currentSpeed: Float?=0F
)

@Entity(tableName = "tracksession")
data class TrackSession(
    @PrimaryKey(autoGenerate = false)
    var id: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "image")
    var imageBase64: String? = "",
    @ColumnInfo(name = "time")
    var totalTime: Long? = 0,
    @ColumnInfo(name = "speed")
    var avgSpeed: Float? = 0F,
    @ColumnInfo(name = "distance")
    var totalDistance: Float? = 0F
)