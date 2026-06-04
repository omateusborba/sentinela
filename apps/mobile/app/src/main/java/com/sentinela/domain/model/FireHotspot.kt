package com.sentinela.domain.model

data class FireHotspot(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val acquiredAt: String,
    val satellite: String,
    val instrument: String,
    val confidence: String,
    val frp: Double?,
    val dayNight: String,
)
