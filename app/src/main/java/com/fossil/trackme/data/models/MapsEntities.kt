package com.fossil.trackme.data.models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LatLongEntity(
    var id: Long = System.currentTimeMillis(),
    var lat: Double = 0.0,
    var long: Double = 0.0,
    var speed: Float = 0F
) : Parcelable

@Parcelize
data class TrackingSessionEntity(
    var id: Long,
    var latLongArr: List<LatLongEntity>,
    var imageBase64: String,
    var totalTime: Long,
    var avgSpeed: Float,
    var totalDistance: Float
) : Parcelable

@Parcelize
data class CaptureEntity(
    var latLng: LatLng,
    var zoomValue: Double
) : Parcelable