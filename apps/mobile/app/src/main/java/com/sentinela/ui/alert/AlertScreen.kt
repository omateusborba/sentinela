package com.sentinela.ui.alert

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentinela.SentinelaApplication
import com.sentinela.ui.components.LoadableUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: AlertViewModel = viewModel { app.container.alertViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is LoadableUiState.Success -> {
                if (state.data.isEmpty()) {
                    Text(
                        text = "Nenhum ponto em alerta no momento.",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(state.data, key = { "${it.point.id}-${it.fire.id}" }) { alert ->
                            ListItem(
                                headlineContent = {
                                    Text("⚠️ Foco a ${alert.distanceKm.format(1)} km de ${alert.point.name}")
                                },
                                supportingContent = {
                                    Text(
                                        "Foco ${alert.fire.id} · ${alert.fire.latitude}, ${alert.fire.longitude}",
                                    )
                                },
                            )
                        }
                    }
                }
            }
            else -> Text("Carregando…", modifier = Modifier.padding(padding).padding(16.dp))
        }
    }
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
