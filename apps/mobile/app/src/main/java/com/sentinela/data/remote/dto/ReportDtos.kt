package com.sentinela.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FireReportDto(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val severity: String,
    @SerialName("createdAt") val createdAt: String,
)

@Serializable
data class CreateReportRequestDto(
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val severity: String,
)
