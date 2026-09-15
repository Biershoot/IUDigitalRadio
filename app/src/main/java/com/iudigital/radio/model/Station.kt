package com.iudigital.radio.model

import androidx.annotation.RawRes
import com.iudigital.radio.R

/**
 * Origen del audio de una emisora.
 *
 * Se modela como interfaz sellada para que el reproductor pueda resolver la
 * fuente en tiempo de compilación y el catálogo pueda mezclar recursos locales
 * con flujos de red (RF-07).
 */
sealed interface StationSource {
    /** Pista empaquetada dentro del APK (res/raw). Siempre reproducible. */
    data class Local(@RawRes val resId: Int) : StationSource

    /** Flujo de audio en línea servido por HTTP/ICEcast. */
    data class Streaming(val url: String) : StationSource
}

/**
 * Emisora del catálogo de IU Digital Radio.
 *
 * @param id identificador estable usado para preservar la selección al rotar.
 * @param name nombre comercial de la emisora.
 * @param genre género musical o línea editorial.
 * @param dial etiqueta de dial mostrada en la insignia de la lista.
 * @param source origen del audio (local o streaming).
 */
data class Station(
    val id: String,
    val name: String,
    val genre: String,
    val dial: String,
    val source: StationSource
)

/**
 * Catálogo estático de emisoras.
 *
 * En una evolución del proyecto este objeto sería sustituido por un repositorio
 * con origen remoto (API REST) o local (Room) sin afectar a la capa de UI.
 */
object StationCatalog {

    val stations: List<Station> = listOf(
        Station(
            id = "iud-sintonia",
            name = "IU Digital Sintonía",
            genre = "Institucional · pista local",
            dial = "88.1",
            source = StationSource.Local(R.raw.iu_digital_sintonia)
        ),
        Station(
            id = "groove-salad",
            name = "Groove Salad",
            genre = "Downtempo · ambient",
            dial = "91.3",
            source = StationSource.Streaming("https://ice1.somafm.com/groovesalad-128-mp3")
        ),
        Station(
            id = "indie-pop",
            name = "Indie Pop Rocks",
            genre = "Indie · alternativo",
            dial = "94.7",
            source = StationSource.Streaming("https://ice1.somafm.com/indiepop-128-mp3")
        ),
        Station(
            id = "secret-agent",
            name = "Secret Agent",
            genre = "Lounge · spy jazz",
            dial = "97.5",
            source = StationSource.Streaming("https://ice1.somafm.com/secretagent-128-mp3")
        ),
        Station(
            id = "drone-zone",
            name = "Drone Zone",
            genre = "Ambient · concentración",
            dial = "100.2",
            source = StationSource.Streaming("https://ice1.somafm.com/dronezone-128-mp3")
        ),
        Station(
            id = "radio-paradise",
            name = "Radio Paradise Main",
            genre = "Ecléctica · rock",
            dial = "103.9",
            source = StationSource.Streaming("https://stream.radioparadise.com/mp3-128")
        ),
        Station(
            id = "rp-mellow",
            name = "Radio Paradise Mellow",
            genre = "Suave · acústica",
            dial = "105.4",
            source = StationSource.Streaming("https://stream.radioparadise.com/mellow-128")
        ),
        Station(
            id = "lush",
            name = "Lush",
            genre = "Voces femeninas · chill",
            dial = "107.8",
            source = StationSource.Streaming("https://ice1.somafm.com/lush-128-mp3")
        )
    )

    /** Emisora mostrada al abrir la aplicación por primera vez. */
    val default: Station = stations.first()

    /** Recupera una emisora por su identificador; útil al restaurar el estado. */
    fun byId(id: String): Station = stations.firstOrNull { it.id == id } ?: default
}
