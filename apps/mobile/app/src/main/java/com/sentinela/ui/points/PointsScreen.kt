package com.sentinela.ui.points

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentinela.SentinelaApplication
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.ui.components.LoadableUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: PointsViewModel = viewModel { app.container.pointsViewModel() }
    val uiState by viewModel.points.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pontos monitorados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar ponto")
            }
        },
    ) { padding ->
        when (val state = uiState) {
            is LoadableUiState.Success -> {
                if (state.data.isEmpty()) {
                    Text(
                        text = "Nenhum ponto cadastrado. Toque + para adicionar.",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(state.data, key = { it.id }) { point ->
                            PointRow(
                                point = point,
                                onEdit = { onEdit(point.id) },
                                onDelete = { viewModel.delete(point) },
                            )
                        }
                    }
                }
            }
            else -> Text(
                "Carregando…",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PointRow(
    point: MonitoredPoint,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = {
            BadgedBox(
                badge = {
                    Badge(containerColor = if (point.inAlert) Color(0xFFEF4444) else Color(0xFF22C55E)) {
                        Text(if (point.inAlert) "!" else "OK")
                    }
                },
            ) {
                Text(point.name)
            }
        },
        supportingContent = {
            Text(
                "${point.latitude}, ${point.longitude} · raio ${point.radiusKm} km",
            )
        },
        trailingContent = {
            IconButton(onClick = onEdit) {
                Text("Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir")
            }
        },
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}
