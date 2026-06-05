package com.sentinela.domain.model

data class RegionRiskCard(
    val regionId: String,
    val regionName: String,
    val risk: RegionRisk? = null,
    val error: String? = null,
)
