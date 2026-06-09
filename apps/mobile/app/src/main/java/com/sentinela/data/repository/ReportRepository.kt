package com.sentinela.data.repository

import com.sentinela.data.BRAZIL_BBOX
import com.sentinela.data.remote.SentinelaApi
import com.sentinela.data.remote.dto.CreateReportRequestDto
import com.sentinela.data.remote.toDomain
import com.sentinela.domain.model.FireReport

class ReportRepository(
    private val api: SentinelaApi,
) {
    suspend fun getReports(bbox: String = BRAZIL_BBOX, since: String? = null): List<FireReport> {
        val list = api.getReports(bbox = bbox, since = since)
        return list.map { it.toDomain() }
    }

    suspend fun submitReport(
        latitude: Double,
        longitude: Double,
        description: String,
        severity: String,
    ): FireReport {
        val dto = CreateReportRequestDto(
            latitude = latitude,
            longitude = longitude,
            description = description,
            severity = severity,
        )
        return api.createReport(dto).toDomain()
    }
}
