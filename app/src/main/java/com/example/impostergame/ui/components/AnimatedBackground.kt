package com.example.impostergame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.impostergame.ui.theme.*

@Composable
fun AnimatedBackground(content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) DarkGray else LightGray
    
    // Dodatno pojačavamo prozirnost (alpha) za svijetli način rada kako bi boje bile još izraženije
    val blueAlpha = if (isDarkTheme) 0.2f else 0.62f
    val purpleAlpha = if (isDarkTheme) 0.2f else 0.62f
    
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x"
    )
    
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BlueGradient.copy(alpha = blueAlpha),
                        Color.Transparent
                    ),
                    center = Offset(xOffset.dp.toPx(), yOffset.dp.toPx()),
                    radius = 250.dp.toPx() // Malo povećan radijus za bolji efekt
                ),
                radius = 250.dp.toPx(),
                center = Offset(xOffset.dp.toPx(), yOffset.dp.toPx())
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PurpleGradient.copy(alpha = purpleAlpha),
                        Color.Transparent
                    ),
                    center = Offset((400 - xOffset).dp.toPx(), (800 - yOffset).dp.toPx()),
                    radius = 250.dp.toPx() // Malo povećan radijus za bolji efekt
                ),
                radius = 250.dp.toPx(),
                center = Offset((400 - xOffset).dp.toPx(), (800 - yOffset).dp.toPx())
            )
        }
        content()
    }
}
