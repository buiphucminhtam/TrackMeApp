package com.fossil.trackme.data.models

import android.location.Location

fun TrackingSessionEntity.toTrackSession():TrackSession {
    return TrackSession(id,imageBase64,totalTime,avgSpeed,totalDistance)
}

fun TrackSession.toTrackSessionEntity(): TrackingSessionEntity {
    return TrackingSessionEntity(id?:0, arrayListOf(),imageBase64?:"",totalTime?:0,avgSpeed?:0f,totalDistance?:0f)
}
fun List<TrackSession>.toListTrackSessionEntity(): List<TrackingSessionEntity> {
    val list = arrayListOf<TrackingSessionEntity>()
    for (item in this) {
        list.add(item.toTrackSessionEntity())
    }
    return list
}

fun TrackingSessionEntity.toListLatLong():List<LatLong>{
    val listLatLong = arrayListOf<LatLong>()
    for (item in latLongArr) {
        listLatLong.add(item.toLatLong(id))
    }
    return listLatLong
}

fun LatLongEntity.toLatLong(trackSessionId:Long):LatLong{
    return LatLong(id,trackSessionId,lat,long,speed)
}

fun List<LatLongEntity>.toListLatLongFromLatLongEntity(trackSessionId:Long):List<LatLong>{
    val list = arrayListOf<LatLong>()
    for (item in this) {
        list.add(item.toLatLong(trackSessionId))
    }
    return list
}

fun LatLong.toLatLongEntity(): LatLongEntity {
    return LatLongEntity(id?:0,lat?:0.0,long?:0.0,currentSpeed?:0F)
}

fun List<LatLong>.toListLatLongEntity() : List<LatLongEntity> {
    val list = arrayListOf<LatLongEntity>()
    for (item in this) {
        list.add(item.toLatLongEntity())
    }
    return list
}

fun Location.toLatLongEntity():LatLongEntity{
    return LatLongEntity(System.currentTimeMillis(),latitude,longitude)
}

fun Location.toLatLong(trackSessionId: Long):LatLong{
    return LatLong(System.currentTimeMillis(),trackSessionId,latitude,longitude)
}

fun List<Location>.toListLatLong(trackSessionId: Long) : List<LatLong>{
    val listLatLong = arrayListOf<LatLong>()
    for (item in this) {
        listLatLong.add(item.toLatLong(trackSessionId))
    }
    return listLatLong
}
