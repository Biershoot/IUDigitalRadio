# IU Digital Radio

Aplicación móvil nativa para Android desarrollada en **Kotlin** con **Jetpack Compose**,
correspondiente a la Evidencia de aprendizaje 3 (taller práctico) de la Institución
Universitaria Digital de Antioquia. Modalidad **Opción B – Trabajo individual**
(rol de desarrollador Full-Stack Android).

La aplicación simula una emisora institucional: permite personalizar el perfil del
oyente con una fotografía tomada desde la cámara del dispositivo, controlar la
reproducción de audio con retroalimentación háptica y cambiar de emisora desde un
catálogo dinámico.

## Requerimientos funcionales cubiertos

| ID | Requerimiento | Dónde se implementa |
|----|---------------|---------------------|
| RF-01 | Maquetación UI declarativa (sin XML de vistas) | `ui/RadioScreen.kt`, `ui/components/` |
| RF-02 | Perfil con captura de cámara (`TakePicturePreview`) | `ui/RadioScreen.kt`, `ui/components/ProfileHeader.kt` |
| RF-03 | Permisos `CAMERA` y `VIBRATE` en tiempo de ejecución | `AndroidManifest.xml`, `ui/RadioScreen.kt` |
| RF-04 | Estado con `mutableStateOf` / `rememberSaveable` | `ui/RadioScreen.kt`, `util/BitmapSaver.kt` |
| RF-05 | Retroalimentación háptica (`VibratorManager` / `Vibrator`) | `util/Haptics.kt` |
| RF-06 | Lista dinámica de emisoras con `LazyColumn` | `ui/RadioScreen.kt`, `ui/components/StationRow.kt` |
| RF-07 | Reproducción de audio con Media3 ExoPlayer | `player/RadioPlayerController.kt` |

## Arquitectura

```
com.iudigital.radio
├── MainActivity.kt              Punto de entrada; solo llama a setContent
├── model/Station.kt             Modelo de dominio y catálogo de emisoras
├── player/                      Capa de audio (Media3 ExoPlayer)
├── ui/
│   ├── RadioScreen.kt           Pantalla con estado + versión sin estado
│   ├── components/              ProfileHeader, PlayerCard, StationRow
│   └── theme/                   Color, tipografía y esquema Material 3
└── util/                        Vibración y persistencia de la foto de perfil
```

El patrón aplicado es **elevación de estado** (*state hoisting*): `RadioScreen`
concentra el estado y los efectos secundarios, mientras que `RadioScreenContent`
y los componentes son funciones puras que reciben datos y emiten eventos. Esto
permite previsualizarlos con `@Preview` sin necesidad de emulador.

## Requisitos de compilación

- Android Studio Ladybug o superior
- JDK 17 o superior
- Android SDK 35 (compileSdk 35, minSdk 24)

## Compilación

```bash
./gradlew assembleDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Instalación en un dispositivo o emulador

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Notas de implementación

- La emisora *IU Digital Sintonía* es una pista empaquetada en `res/raw`, de modo
  que la aplicación siempre puede reproducir audio aunque no haya red.
- Las demás emisoras son flujos públicos en línea. Si un flujo no está disponible,
  el reproductor pasa a un modo de **emisión simulada** en lugar de fallar: la
  interfaz sigue reflejando el estado de reproducción (RF-07).
- La fotografía de perfil se reduce a 512 px antes de mostrarse y se conserva en
  el almacenamiento interno, de modo que sobrevive a la rotación de pantalla.
