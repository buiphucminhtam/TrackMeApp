package com.fossil.trackme.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

data class LatLongEntity(
    val id: Long,
    val lat: Double,
    val long: Double
)

data class TrackSessionEntity(
    val id: Long,
    val latLongArr: List<LatLongEntity>,
    val imageBase64: String,
    val totalTime: Long,
    val avgSpeed: Float,
    val totalDistance: Float
)
