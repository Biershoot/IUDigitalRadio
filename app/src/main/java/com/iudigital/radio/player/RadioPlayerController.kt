package com.iudigital.radio.player

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.iudigital.radio.model.Station
import com.iudigital.radio.model.StationSource

/** Situación del motor de audio, expuesta a la interfaz para informar al usuario. */
enum class EngineStatus {
    /** Sin emisora preparada todavía. */
    IDLE,

    /** Conectando con el flujo de audio o abriendo el recurso local. */
    BUFFERING,

    /** Audio real sonando a través de Media3 ExoPlayer. */
    STREAMING,

    /**
     * El flujo de red no está disponible (sin datos, servidor caído o emulador
     * sin salida de audio). La aplicación continúa en modo simulado: la interfaz
     * refleja el estado de reproducción aunque no haya señal (RF-07).
     */
    SIMULATED
}

/**
 * RF-07: capa de reproducción.
 *
 * Envuelve a Media3 [ExoPlayer] y lo expone como un objeto observable por
 * Compose. La pantalla mantiene el estado de la interfaz (emisora seleccionada,
 * reproduciendo, silenciado) y este controlador se limita a ejecutar los efectos
 * secundarios correspondientes sobre el motor de audio.
 */
@Stable
class RadioPlayerController(private val context: Context) {

    /** Estado del motor observado por la interfaz. */
    var status: EngineStatus by mutableStateOf(EngineStatus.IDLE)
        private set

    /** Descripción legible del último error de reproducción, si lo hubo. */
    var lastError: String? by mutableStateOf(null)
        private set

    private var currentStationId: String? = null

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            status = when (playbackState) {
                Player.STATE_BUFFERING -> EngineStatus.BUFFERING
                Player.STATE_READY -> EngineStatus.STREAMING
                else -> status
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            // Degradación elegante: la app no se cierra, pasa a modo simulado.
            lastError = error.errorCodeName
            status = EngineStatus.SIMULATED
        }
    }

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            addListener(listener)
        }
    }

    /** Carga la emisora indicada y decide si debe empezar a sonar. */
    fun prepare(station: Station, playWhenReady: Boolean) {
        if (currentStationId == station.id) {
            player.playWhenReady = playWhenReady
            return
        }
        currentStationId = station.id
        lastError = null
        status = EngineStatus.BUFFERING

        player.setMediaItem(MediaItem.fromUri(station.uri()))
        // La pista institucional es corta: se repite en bucle como si fuera
        // una emisora continua. Los flujos en línea no necesitan repetición.
        player.repeatMode = when (station.source) {
            is StationSource.Local -> Player.REPEAT_MODE_ONE
            is StationSource.Streaming -> Player.REPEAT_MODE_OFF
        }
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    /** Alterna entre reproducción y pausa sin recargar la emisora. */
    fun setPlaying(playing: Boolean) {
        player.playWhenReady = playing
        if (!playing && status == EngineStatus.BUFFERING) {
            status = EngineStatus.IDLE
        }
    }

    /** Silencia o restituye el volumen del motor de audio. */
    fun setMuted(muted: Boolean) {
        player.volume = if (muted) 0f else 1f
    }

    /** Libera los recursos nativos del reproductor. */
    fun release() {
        player.removeListener(listener)
        player.release()
    }

    private fun Station.uri(): Uri = when (val origin = source) {
        is StationSource.Local ->
            Uri.parse("android.resource://${context.packageName}/${origin.resId}")

        is StationSource.Streaming -> Uri.parse(origin.url)
    }
}

/**
 * Crea un [RadioPlayerController] ligado al ciclo de vida de la composición y
 * garantiza la liberación de los recursos nativos cuando la pantalla desaparece.
 */
@Composable
fun rememberRadioPlayerController(): RadioPlayerController {
    val context = LocalContext.current
    val controller = remember { RadioPlayerController(context.applicationContext) }

    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    return controller
}
