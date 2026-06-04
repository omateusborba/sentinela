package com.sentinela.ui.points

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.ui.components.LoadableUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PointFormState(
    val id: Long = 0,
    val name: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val radiusKm: String = "10",
)

class PointFormViewModel(
    private val pointId: Long,
    private val pointRepository: MonitoredPointRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(PointFormState())
    val form: StateFlow<PointFormState> = _form.asStateFlow()

    private val _saveState = MutableStateFlow<LoadableUiState<Unit>?>(null)
    val saveState: StateFlow<LoadableUiState<Unit>?> = _saveState.asStateFlow()

    init {
        if (pointId != 0L) {
            viewModelScope.launch {
                val existing = pointRepository.getById(pointId)
                if (existing != null) {
                    _form.value = PointFormState(
                        id = existing.id,
                        name = existing.name,
                        latitude = existing.latitude.toString(),
                        longitude = existing.longitude.toString(),
                        radiusKm = existing.radiusKm.toString(),
                    )
                }
            }
        }
    }

    fun updateName(value: String) {
        _form.value = _form.value.copy(name = value)
    }

    fun updateLatitude(value: String) {
        _form.value = _form.value.copy(latitude = value)
    }

    fun updateLongitude(value: String) {
        _form.value = _form.value.copy(longitude = value)
    }

    fun updateRadius(value: String) {
        _form.value = _form.value.copy(radiusKm = value)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _form.value
        val lat = state.latitude.toDoubleOrNull()
        val lon = state.longitude.toDoubleOrNull()
        val radius = state.radiusKm.toDoubleOrNull()

        if (state.name.isBlank() || lat == null || lon == null || radius == null || radius <= 0) {
            _saveState.value = LoadableUiState.Error("Preencha nome, coordenadas e raio válidos")
            return
        }

        viewModelScope.launch {
            _saveState.value = LoadableUiState.Loading
            try {
                val existing = if (state.id != 0L) pointRepository.getById(state.id) else null
                pointRepository.upsert(
                    MonitoredPoint(
                        id = state.id,
                        name = state.name.trim(),
                        latitude = lat,
                        longitude = lon,
                        radiusKm = radius,
                        inAlert = existing?.inAlert ?: false,
                    ),
                )
                _saveState.value = LoadableUiState.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                _saveState.value = LoadableUiState.Error(e.message ?: "Erro ao salvar")
            }
        }
    }
}
