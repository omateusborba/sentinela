package com.sentinela.ui.detail

import androidx.lifecycle.ViewModel
import com.sentinela.di.FiresSessionStore
import com.sentinela.domain.model.FireHotspot
import com.sentinela.ui.components.LoadableUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FireDetailViewModel(
    private val fireId: String,
    private val firesSessionStore: FiresSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoadableUiState<FireHotspot>>(LoadableUiState.Loading)
    val uiState: StateFlow<LoadableUiState<FireHotspot>> = _uiState.asStateFlow()

    init {
        val fire = firesSessionStore.fires.find { it.id == fireId }
        _uiState.value = if (fire != null) {
            LoadableUiState.Success(fire)
        } else {
            LoadableUiState.Error("Foco não encontrado")
        }
    }
}
