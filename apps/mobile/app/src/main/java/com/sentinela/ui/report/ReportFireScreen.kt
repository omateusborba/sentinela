package com.sentinela.ui.report

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentinela.SentinelaApplication
import com.sentinela.SentinelaDefaults
import com.sentinela.domain.model.Coordinate
import com.sentinela.ui.components.SentinelaTopAppBar
import com.sentinela.ui.theme.SentinelaColors

private val severityOptions = listOf(
    "low" to "Baixa",
    "medium" to "Média",
    "high" to "Alta",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFireScreen(
    initialCoordinate: Coordinate?,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: ReportFireViewModel = viewModel { app.container.reportFireViewModel() }
    val form by viewModel.form.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(initialCoordinate) {
        viewModel.prefill(initialCoordinate)
    }

    LaunchedEffect(form.success) {
        if (form.success != null) {
            onSubmitted()
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.useCurrentLocation(ok)
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = {
            SentinelaTopAppBar(
                title = "Reportar incêndio",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Reporte anônimo da comunidade (MVP, sem moderação).",
                style = MaterialTheme.typography.bodySmall,
                color = SentinelaColors.TextMuted,
            )
            OutlinedTextField(
                value = form.latitude,
                onValueChange = viewModel::setLatitude,
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = form.longitude,
                onValueChange = viewModel::setLongitude,
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedButton(
                onClick = {
                    if (hasLocationPermission()) {
                        viewModel.useCurrentLocation(true)
                    } else {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
                enabled = !form.locating,
            ) {
                Text(if (form.locating) "Obtendo…" else "Usar minha localização")
            }
            OutlinedTextField(
                value = form.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                supportingText = {
                    Text("${form.description.length}/${SentinelaDefaults.REPORT_DESCRIPTION_MAX}")
                },
            )
            Text(
                text = "Severidade",
                style = MaterialTheme.typography.labelLarge,
                color = SentinelaColors.TextMuted,
            )
            severityOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = form.severity == value,
                            onClick = { viewModel.setSeverity(value) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = form.severity == value,
                        onClick = { viewModel.setSeverity(value) },
                    )
                    Text(text = label, modifier = Modifier.padding(start = 8.dp))
                }
            }
            form.error?.let {
                Text(text = it, color = SentinelaColors.FlameRed)
            }
            Button(
                onClick = viewModel::submit,
                enabled = !form.loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SentinelaColors.FlameOrange),
            ) {
                Text(if (form.loading) "Enviando…" else "Enviar reporte")
            }
        }
    }
}
