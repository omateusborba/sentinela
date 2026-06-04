package com.sentinela.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.sentinela.ui.components.ErrorContent
import com.sentinela.ui.components.LoadableUiState
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireListScreen(
    onBack: () -> Unit,
    onFireClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: FireListViewModel = viewModel { app.container.fireListViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focos recentes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                FilterChip(
                    selected = sortMode == FireSortMode.RECENCY,
                    onClick = { viewModel.setSortMode(FireSortMode.RECENCY) },
                    label = { Text("Recência") },
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = sortMode == FireSortMode.PROXIMITY,
                    onClick = { viewModel.setSortMode(FireSortMode.PROXIMITY) },
                    label = { Text("Proximidade") },
                )
            }

            when (val state = uiState) {
                is LoadableUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = onBack,
                    modifier = Modifier.fillMaxSize(),
                )
                is LoadableUiState.Success -> LazyColumn {
                    items(state.data.items, key = { it.fire.id }) { item ->
                        ListItem(
                            headlineContent = {
                                Text(formatTime(item.fire.acquiredAt))
                            },
                            supportingContent = {
                                val frp = item.fire.frp?.let { "FRP %.1f · ".format(it) } ?: ""
                                val prox = item.nearestDistanceKm?.let { dist ->
                                    " · ${dist.format(1)} km de ${item.nearestPointName}"
                                } ?: ""
                                Text(
                                    "${frp}${item.fire.satellite} / ${item.fire.confidence}$prox",
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFireClick(item.fire.id) },
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    .withZone(ZoneOffset.UTC)

private fun formatTime(iso: String): String =
    runCatching {
        timeFormatter.format(Instant.parse(iso))
    }.getOrDefault(iso)

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
