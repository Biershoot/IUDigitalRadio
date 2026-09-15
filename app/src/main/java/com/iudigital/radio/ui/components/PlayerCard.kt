package com.iudigital.radio.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iudigital.radio.R
import com.iudigital.radio.model.Station
import com.iudigital.radio.model.StationCatalog
import com.iudigital.radio.player.EngineStatus
import com.iudigital.radio.ui.theme.IUDigitalRadioTheme
import com.iudigital.radio.ui.theme.IuAmber
import com.iudigital.radio.ui.theme.IuGreen
import com.iudigital.radio.ui.theme.IuNavy
import com.iudigital.radio.ui.theme.IuNavyElevated
import com.iudigital.radio.ui.theme.IuNavySurface

/**
 * RF-04 y RF-05: reproductor principal.
 *
 * Tarjeta destacada con los datos de la emisora activa y los tres controles
 * exigidos (Play, Pause y Mute). Cada control notifica su pulsación hacia la
 * pantalla, que se encarga de actualizar el estado y disparar la vibración.
 */
@Composable
fun PlayerCard(
    station: Station,
    isPlaying: Boolean,
    isMuted: Boolean,
    engineStatus: EngineStatus,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(IuNavyElevated, IuNavySurface)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusLine(isPlaying = isPlaying, engineStatus = engineStatus)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = station.dial + " FM  ·  " + station.genre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Equalizer(active = isPlaying && !isMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    icon = Icons.Rounded.PlayArrow,
                    label = stringResource(R.string.player_play),
                    highlighted = !isPlaying,
                    onClick = onPlay,
                    modifier = Modifier.weight(1f)
                )
                ControlButton(
                    icon = Icons.Rounded.Pause,
                    label = stringResource(R.string.player_pause),
                    highlighted = isPlaying,
                    onClick = onPause,
                    modifier = Modifier.weight(1f)
                )
                ControlButton(
                    icon = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                    label = stringResource(
                        if (isMuted) R.string.player_unmute else R.string.player_mute
                    ),
                    highlighted = isMuted,
                    accent = IuAmber,
                    onClick = onToggleMute,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Línea superior con el indicador de emisión y el estado del motor de audio. */
@Composable
private fun StatusLine(isPlaying: Boolean, engineStatus: EngineStatus) {
    val label = when {
        !isPlaying -> stringResource(R.string.player_paused)
        engineStatus == EngineStatus.BUFFERING -> stringResource(R.string.player_connecting)
        engineStatus == EngineStatus.SIMULATED -> stringResource(R.string.player_simulated)
        else -> stringResource(R.string.player_now_playing)
    }
    val dotColor = when {
        !isPlaying -> MaterialTheme.colorScheme.onSurfaceVariant
        engineStatus == EngineStatus.SIMULATED -> IuAmber
        else -> IuGreen
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = dotColor
        )
    }
}

/** Ecualizador decorativo que se anima solo mientras hay audio activo. */
@Composable
private fun Equalizer(active: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ecualizador")
    val durations = listOf(420, 300, 520, 360, 460)

    Row(
        modifier = modifier.height(44.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        durations.forEachIndexed { index, duration ->
            val factor by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barra"
            )
            val barHeight = 44.dp * (if (active) factor else 0.18f)

            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (active) IuGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
            )
        }
    }
}

/** Botón circular de control con su etiqueta descriptiva. */
@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = IuGreen
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (highlighted) accent else MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .size(58.dp)
                .semantics { contentDescription = label }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = if (highlighted) IuNavy else accent
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1B2B)
@Composable
private fun PlayerCardPreview() {
    IUDigitalRadioTheme {
        PlayerCard(
            station = StationCatalog.default,
            isPlaying = true,
            isMuted = false,
            engineStatus = EngineStatus.STREAMING,
            onPlay = {},
            onPause = {},
            onToggleMute = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
