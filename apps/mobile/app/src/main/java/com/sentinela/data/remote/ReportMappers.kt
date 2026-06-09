package com.sentinela.data.remote

import com.sentinela.data.remote.dto.CreateReportRequestDto
import com.sentinela.data.remote.dto.FireReportDto
import com.sentinela.domain.model.FireReport

fun FireReportDto.toDomain(): FireReport = FireReport(
    id = id,
    latitude = latitude,
    longitude = longitude,
    description = description,
    severity = severity,
    createdAt = createdAt,
)

fun FireReport.toCreateDto(): CreateReportRequestDto = CreateReportRequestDto(
    latitude = latitude,
    longitude = longitude,
    description = description,
    severity = severity,
)
