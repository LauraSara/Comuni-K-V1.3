package com.example.comunik.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunik.ui.theme.*

/**
 * Componentes de paleta personalizados para la aplicación
 */

@Composable
fun PaletteText(
    text: String,
    modifier: Modifier = Modifier,
    style: PaletteTextStyle = PaletteTextStyle.Body
) {
    val textStyle = when (style) {
        PaletteTextStyle.Title -> MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextPrimary
        )
        PaletteTextStyle.Subtitle -> MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            color = TextPrimary
        )
        PaletteTextStyle.Body -> MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            color = TextPrimary
        )
        PaletteTextStyle.Caption -> MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
    
    Text(
        text = text,
        style = textStyle,
        modifier = modifier
    )
}

enum class PaletteTextStyle {
    Title, Subtitle, Body, Caption
}

@Composable
fun PaletteButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PaletteButtonStyle = PaletteButtonStyle.Primary,
    enabled: Boolean = true
) {
    val buttonColors = when (style) {
        PaletteButtonStyle.Primary -> ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            disabledContainerColor = BorderGray,
            disabledContentColor = TextSecondary
        )
        PaletteButtonStyle.Secondary -> ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PrimaryBlue,
            disabledContainerColor = Color.White,
            disabledContentColor = TextSecondary
        )
        PaletteButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryBlue,
            disabledContentColor = TextSecondary
        )
    }
    
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = buttonColors,
        shape = RoundedCornerShape(8.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        )
    }
}

enum class PaletteButtonStyle {
    Primary, Secondary, Outlined
}

@Composable
fun PaletteWidget(
    title: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PaletteText(
                text = title,
                style = PaletteTextStyle.Subtitle,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
