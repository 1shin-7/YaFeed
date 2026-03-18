package com.w57736e.yafeed.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float
)

@Composable
fun ParticleBox(
    modifier: Modifier = Modifier,
    color: Color
) {
    val particles = remember {
        List(18) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                alpha = Random.nextFloat() * 0.4f + 0.2f,
                speed = Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "particles")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_move"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val yOffset = ((offset + particle.y) % 1f) * size.height
            drawCircle(
                color = color.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x * size.width, yOffset)
            )
        }
    }
}
