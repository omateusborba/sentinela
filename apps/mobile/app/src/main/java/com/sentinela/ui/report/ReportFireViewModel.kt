package com.sentinela.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.SentinelaDefaults
import com.sentinela.data.repository.ReportRepository
import com.sentinela.domain.model.Coordinate
import com.sentinela.domain.model.FireReport
import com.sentinela.location.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportFormState(
    val latitude: String = "",
    val longitude: String = "",
    val description: String = "",
    val severity: String = "medium",
    val loading: Boolean = false,
    val locating: Boolean = false,
    val error: String? = null,
    val success: FireReport? = null,
)

class ReportFireViewModel(
    private val reportRepository: ReportRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _form = MutableStateFlow(ReportFormState())
    val form: StateFlow<ReportFormState> = _form.asStateFlow()

    fun setLatitude(value: String) {
        _form.value = _form.value.copy(latitude = value, error = null)
    }

    fun setLongitude(value: String) {
        _form.value = _form.value.copy(longitude = value, error = null)
    }

    fun setDescription(value: String) {
        if (value.length <= SentinelaDefaults.REPORT_DESCRIPTION_MAX) {
            _form.value = _form.value.copy(description = value, error = null)
        }
    }

    fun setSeverity(value: String) {
        _form.value = _form.value.copy(severity = value, error = null)
    }

    fun prefill(coordinate: Coordinate?) {
        if (coordinate == null) return
        _form.value = _form.value.copy(
            latitude = coordinate.latitude.toString(),
            longitude = coordinate.longitude.toString(),
        )
    }

    fun useCurrentLocation(hasPermission: Boolean) {
        if (!hasPermission) {
            _form.value = _form.value.copy(error = "Permissão de localização necessária.")
            return
        }
        viewModelScope.launch {
            _form.value = _form.value.copy(locating = true, error = null)
            val coord = locationProvider.getCurrentLocation()
            _form.value = if (coord != null) {
                _form.value.copy(
                    latitude = coord.latitude.toString(),
                    longitude = coord.longitude.toString(),
                    locating = false,
                )
            } else {
                _form.value.copy(
                    locating = false,
                    error = "Não foi possível obter localização.",
                )
            }
        }
    }

    fun submit() {
        val current = _form.value
        val lat = current.latitude.toDoubleOrNull()
        val lon = current.longitude.toDoubleOrNull()
        val desc = current.description.trim()

        if (lat == null || lon == null) {
            _form.value = current.copy(error = "Coordenadas inválidas.")
            return
        }
        if (desc.isEmpty()) {
            _form.value = current.copy(error = "Descrição obrigatória.")
            return
        }

        viewModelScope.launch {
            _form.value = current.copy(loading = true, error = null)
            try {
                val report = reportRepository.submitReport(
                    latitude = lat,
                    longitude = lon,
                    description = desc,
                    severity = current.severity,
                )
                _form.value = ReportFormState(success = report)
            } catch (e: Exception) {
                _form.value = current.copy(
                    loading = false,
                    error = e.message ?: "Falha ao enviar reporte",
                )
            }
        }
    }
}
