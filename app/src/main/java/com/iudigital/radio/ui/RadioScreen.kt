package com.iudigital.radio.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.iudigital.radio.R
import com.iudigital.radio.model.Station
import com.iudigital.radio.model.StationCatalog
import com.iudigital.radio.player.EngineStatus
import com.iudigital.radio.player.rememberRadioPlayerController
import com.iudigital.radio.ui.components.PlayerCard
import com.iudigital.radio.ui.components.ProfileHeader
import com.iudigital.radio.ui.components.StationRow
import com.iudigital.radio.ui.theme.IUDigitalRadioTheme
import com.iudigital.radio.ui.theme.IuGreen
import com.iudigital.radio.util.Haptics
import com.iudigital.radio.util.rememberProfilePhotoState
import com.iudigital.radio.util.scaleForAvatar
import kotlinx.coroutines.launch

/**
 * Pantalla principal con estado (stateful).
 *
 * Concentra las tres responsabilidades transversales de la aplicación:
 *  - Estado de la interfaz preservado con rememberSaveable (RF-04).
 *  - Integración con el hardware: cámara y vibración (RF-02, RF-03, RF-05).
 *  - Sincronización del estado con el motor de audio (RF-07).
 *
 * El dibujo de la interfaz se delega en [RadioScreenContent], que no guarda
 * estado propio y por tanto puede previsualizarse y probarse de forma aislada.
 */
@Composable
fun RadioScreen(listenerName: String = "Oyente IU Digital") {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Estado de la interfaz: sobrevive a la rotación de pantalla ---
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var selectedStationId by rememberSaveable { mutableStateOf(StationCatalog.default.id) }
    var profilePhoto by rememberProfilePhotoState()

    val selectedStation: Station = remember(selectedStationId) {
        StationCatalog.byId(selectedStationId)
    }

    // --- Motor de audio ---
    val player = rememberRadioPlayerController()

    LaunchedEffect(selectedStation, isPlaying) {
        player.prepare(selectedStation, playWhenReady = isPlaying)
        player.setPlaying(isPlaying)
    }
    LaunchedEffect(isMuted) {
        player.setMuted(isMuted)
    }

    // --- Cámara y permisos (RF-02 y RF-03) ---
    val permissionDeniedMessage = stringResource(R.string.permission_denied)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Se reduce antes de mostrarla: evita texturas de varios megapixeles
            profilePhoto = bitmap.scaleForAvatar()
            scope.launch { snackbarHostState.showMessage("Foto de perfil actualizada") }
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            takePictureLauncher.launch(null)
        } else {
            scope.launch { snackbarHostState.showMessage(permissionDeniedMessage) }
        }
    }

    val onTakePhoto: () -> Unit = {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            takePictureLauncher.launch(null)
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    RadioScreenContent(
        listenerName = listenerName,
        profilePhoto = profilePhoto,
        stations = StationCatalog.stations,
        selectedStation = selectedStation,
        isPlaying = isPlaying,
        isMuted = isMuted,
        engineStatus = player.status,
        snackbarHostState = snackbarHostState,
        onTakePhoto = onTakePhoto,
        onPlay = {
            Haptics.tick(context)
            isPlaying = true
        },
        onPause = {
            Haptics.tick(context)
            isPlaying = false
        },
        onToggleMute = {
            Haptics.doubleTick(context)
            isMuted = !isMuted
        },
        onStationSelected = { station ->
            Haptics.stationChange(context)
            selectedStationId = station.id
            isPlaying = true
        }
    )
}

/**
 * Pantalla principal sin estado (stateless).
 *
 * Recibe todo lo que debe mostrar y emite eventos hacia arriba, siguiendo el
 * patrón de elevación de estado recomendado por Jetpack Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreenContent(
    listenerName: String,
    profilePhoto: Bitmap?,
    stations: List<Station>,
    selectedStation: Station,
    isPlaying: Boolean,
    isMuted: Boolean,
    engineStatus: EngineStatus,
    snackbarHostState: SnackbarHostState,
    onTakePhoto: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onToggleMute: () -> Unit,
    onStationSelected: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Radio,
                            contentDescription = null,
                            tint = IuGreen
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        // RF-01 y RF-06: toda la pantalla se compone con LazyColumn, de modo que
        // el contenido se recicla y funciona igual en vertical y en horizontal.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sección superior: perfil con captura de cámara
            item(key = "perfil") {
                ProfileHeader(
                    photo = profilePhoto,
                    listenerName = listenerName,
                    onTakePhotoClick = onTakePhoto
                )
            }

            // Sección central: reproductor principal
            item(key = "reproductor") {
                PlayerCard(
                    station = selectedStation,
                    isPlaying = isPlaying,
                    isMuted = isMuted,
                    engineStatus = engineStatus,
                    onPlay = onPlay,
                    onPause = onPause,
                    onToggleMute = onToggleMute
                )
            }

            // Sección inferior: catálogo de emisoras
            item(key = "titulo_catalogo") {
                CatalogTitle(total = stations.size)
            }

            items(items = stations, key = { it.id }) { station ->
                StationRow(
                    station = station,
                    isSelected = station.id == selectedStation.id,
                    isPlaying = isPlaying,
                    onClick = { onStationSelected(station) }
                )
            }
        }
    }
}

/** Encabezado de la sección de catálogo. */
@Composable
private fun CatalogTitle(total: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.catalog_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.catalog_subtitle, total),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Atajo para mostrar mensajes breves sin repetir la configuración. */
private suspend fun SnackbarHostState.showMessage(message: String) {
    currentSnackbarData?.dismiss()
    showSnackbar(message)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RadioScreenPreview() {
    IUDigitalRadioTheme {
        RadioScreenContent(
            listenerName = "Oyente IU Digital",
            profilePhoto = null,
            stations = StationCatalog.stations,
            selectedStation = StationCatalog.default,
            isPlaying = true,
            isMuted = false,
            engineStatus = EngineStatus.STREAMING,
            snackbarHostState = SnackbarHostState(),
            onTakePhoto = {},
            onPlay = {},
            onPause = {},
            onToggleMute = {},
            onStationSelected = {}
        )
    }
}
