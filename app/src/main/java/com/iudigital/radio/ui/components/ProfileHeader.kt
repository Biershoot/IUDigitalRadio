package com.iudigital.radio.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iudigital.radio.R
import com.iudigital.radio.ui.theme.IUDigitalRadioTheme
import com.iudigital.radio.ui.theme.IuGreen
import com.iudigital.radio.ui.theme.IuGreenSoft

/**
 * RF-02: sección superior de perfil.
 *
 * Muestra la fotografía capturada con la cámara nativa dentro de un contenedor
 * circular y ofrece el botón de disparo. Es un componente sin estado: recibe la
 * imagen y notifica la intención de tomar la foto hacia arriba.
 */
@Composable
fun ProfileHeader(
    photo: Bitmap?,
    listenerName: String,
    onTakePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileAvatar(photo = photo)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = listenerName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.profile_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onTakePhotoClick,
                    modifier = Modifier.padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IuGreen)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = IuGreen
                    )
                    Text(
                        text = stringResource(
                            if (photo == null) R.string.profile_take_photo
                            else R.string.profile_retake_photo
                        ),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = IuGreen
                    )
                }
            }
        }
    }
}

/** Contenedor circular de la fotografía de perfil o del avatar por defecto. */
@Composable
private fun ProfileAvatar(photo: Bitmap?, modifier: Modifier = Modifier) {
    val ring = Brush.linearGradient(listOf(IuGreen, IuGreenSoft))

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(width = 3.dp, brush = ring, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = stringResource(R.string.profile_photo_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.PersonOutline,
                contentDescription = stringResource(R.string.profile_placeholder_desc),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1B2B)
@Composable
private fun ProfileHeaderPreview() {
    IUDigitalRadioTheme {
        ProfileHeader(
            photo = null,
            listenerName = "Estudiante IU Digital",
            onTakePhotoClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
