package com.sentinela.domain.usecase

import com.sentinela.data.repository.FireRepository
import com.sentinela.domain.model.FireHotspot

class GetFiresUseCase(
    private val fireRepository: FireRepository,
) {
    suspend operator fun invoke(days: Int): List<FireHotspot> =
        fireRepository.getFires(days)
}
