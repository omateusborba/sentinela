package com.sentinela.data.repository

import com.sentinela.data.BRAZIL_BBOX
import com.sentinela.data.remote.SentinelaApi
import com.sentinela.data.remote.toDomain
import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.RegionRisk

class FireRepository(
    private val api: SentinelaApi,
) {
    suspend fun getFires(days: Int): List<FireHotspot> {
        val response = api.getFires(bbox = BRAZIL_BBOX, days = days)
        return response.hotspots.map { it.toDomain() }
    }

    suspend fun getRisk(days: Int): RegionRisk {
        val response = api.getRisk(bbox = BRAZIL_BBOX, days = days)
        return response.risk.toDomain()
    }
}
