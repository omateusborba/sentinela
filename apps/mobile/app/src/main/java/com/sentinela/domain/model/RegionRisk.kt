package com.sentinela.domain.model

data class RegionRisk(
    val level: RiskLevel,
    val score: Double,
    val totalFires: Int,
    val trend: String,
)
