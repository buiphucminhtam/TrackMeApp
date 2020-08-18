package com.fossil.trackme.data.models

import android.location.Location

fun TrackSessionAndLatLong.toTrackSessionEntity():TrackSessionEntity{
    return TrackSessionEntity(trackSession.id?:0,listLatLong?.toListLatLongEntity()?: arrayListOf(),trackSession.imageBase64?:"",trackSession.totalTime?:0,trackSession.avgSpeed?:0f,trackSession.totalDistance?:0f)
}

fun TrackSessionEntity.toTrackSession():TrackSession {
    return TrackSession(id,imageBase64,totalTime,avgSpeed,totalDistance)
}

fun TrackSession.toTrackSessionEntity(): TrackSessionEntity {
    return TrackSessionEntity(id?:0, arrayListOf(),imageBase64?:"",totalTime?:0,avgSpeed?:0f,totalDistance?:0f)
}
fun List<TrackSession>.toListTrackSessionEntity(): List<TrackSessionEntity> {
    val list = arrayListOf<TrackSessionEntity>()
    for (item in this) {
        list.add(item.toTrackSessionEntity())
    }
    return list
}

fun TrackSessionEntity.toListLatLong():List<LatLong>{
    val listLatLong = arrayListOf<LatLong>()
    for (item in latLongArr) {
        listLatLong.add(item.toLatLong(id))
    }
    return listLatLong
}

fun LatLongEntity.toLatLong(trackSessionId:Long):LatLong{
    return LatLong(id,trackSessionId,lat,long)
}

fun LatLong.toLatLongEntity(): LatLongEntity {
    return LatLongEntity(id?:0,lat?:0.0,long?:0.0)
}

fun List<LatLong>.toListLatLongEntity() : List<LatLongEntity> {
    val list = arrayListOf<LatLongEntity>()
    for (item in this) {
        list.add(item.toLatLongEntity())
    }
    return list
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