package com.sentinela.domain.model

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)

data class FireReport(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val severity: String,
    val createdAt: String,
)
