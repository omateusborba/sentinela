package com.sentinela.ui.points

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.sentinela.ui.components.LoadableUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointFormScreen(
    pointId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: PointFormViewModel = viewModel(key = "form-$pointId") {
        app.container.pointFormViewModel(pointId)
    }
    val form by viewModel.form.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    Scaffold(
        topBar = {
            SentinelaTopAppBar(
                title = if (pointId == 0L) "Novo ponto" else "Editar ponto",
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
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = form.name,
                onValueChange = viewModel::updateName,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.latitude,
                onValueChange = viewModel::updateLatitude,
                label = { Text("Latitude") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            OutlinedTextField(
                value = form.longitude,
                onValueChange = viewModel::updateLongitude,
                label = { Text("Longitude") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            OutlinedTextField(
                value = form.radiusKm,
                onValueChange = viewModel::updateRadius,
                label = { Text("Raio (km)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            Text(
                text = "Ex.: Amazônia -3.1, -60.0 · raio 50 km",
                modifier = Modifier.padding(top = 8.dp),
            )
            when (val saved = saveState) {
                is LoadableUiState.Error -> Text(
                    text = saved.message,
                    modifier = Modifier.padding(top = 8.dp),
                )
                else -> Unit
            }
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = saveState !is LoadableUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Text("Salvar")
            }
        }
    }
}
