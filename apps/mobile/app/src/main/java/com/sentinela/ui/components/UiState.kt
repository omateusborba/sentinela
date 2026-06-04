package com.sentinela.ui.components

sealed interface LoadableUiState<out T> {
    data object Loading : LoadableUiState<Nothing>
    data class Success<T>(val data: T) : LoadableUiState<T>
    data class Error(val message: String) : LoadableUiState<Nothing>
}
