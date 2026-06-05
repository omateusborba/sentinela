package com.sentinela.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.sentinela.ui.components.SentinelaTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentinela.SentinelaApplication
import com.sentinela.ui.components.ErrorContent
import com.sentinela.ui.components.LoadableUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireDetailScreen(
    fireId: String,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: FireDetailViewModel = viewModel(key = fireId) {
        app.container.fireDetailViewModel(fireId)
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SentinelaTopAppBar(
                title = "Detalhe do foco",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is LoadableUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = onBack,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            is LoadableUiState.Success -> {
                val fire = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                ) {
                    DetailRow("ID", fire.id)
                    DetailRow("Coordenadas", "${fire.latitude}, ${fire.longitude}")
                    DetailRow("Detectado (UTC)", fire.acquiredAt)
                    DetailRow("Satélite", fire.satellite)
                    DetailRow("Instrumento", fire.instrument)
                    DetailRow("Confiança", fire.confidence)
                    DetailRow("FRP (MW)", fire.frp?.toString() ?: "—")
                    DetailRow("Dia/Noite", fire.dayNight)
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Text(text = label, modifier = Modifier.padding(top = 12.dp))
    Text(text = value)
}
