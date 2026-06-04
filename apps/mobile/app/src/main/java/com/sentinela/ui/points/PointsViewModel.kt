package com.sentinela.ui.points

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.ui.components.LoadableUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PointsViewModel(
    private val pointRepository: MonitoredPointRepository,
) : ViewModel() {

    val points: StateFlow<LoadableUiState<List<MonitoredPoint>>> = pointRepository
        .observeAll()
        .map { points -> LoadableUiState.Success(points) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LoadableUiState.Loading,
        )

    fun delete(point: MonitoredPoint) {
        viewModelScope.launch {
            pointRepository.delete(point)
        }
    }
}
