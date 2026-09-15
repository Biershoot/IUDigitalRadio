package com.iudigital.radio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.iudigital.radio.ui.RadioScreen
import com.iudigital.radio.ui.theme.IUDigitalRadioTheme

/**
 * Punto de entrada de IU Digital Radio.
 *
 * La actividad no infla ningún layout XML (RF-01): toda la interfaz se declara
 * con Jetpack Compose a partir de [RadioScreen].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IUDigitalRadioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RadioScreen(listenerName = getString(R.string.listener_default_name))
                }
            }
        }
    }
}
