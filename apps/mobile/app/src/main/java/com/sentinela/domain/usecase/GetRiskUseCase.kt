package com.sentinela.domain.usecase

import com.sentinela.data.repository.FireRepository
import com.sentinela.domain.model.RegionRisk

class GetRiskUseCase(
    private val fireRepository: FireRepository,
) {
    suspend operator fun invoke(bbox: String, days: Int): RegionRisk =
        fireRepository.getRisk(bbox, days)
}
