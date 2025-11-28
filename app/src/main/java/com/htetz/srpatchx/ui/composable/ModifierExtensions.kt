package com.htetz.srpatchx.ui.composable

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection

object ModifierExtensions {
    @Composable
    fun Modifier.animateInfiniteRotate(): Modifier = composed {
        val transition = rememberInfiniteTransition(label = "Cookie animation")
        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Cookie animation"
        )

        graphicsLayer {
            rotationZ = angle
        }
    }

    @Composable
    private fun Modifier.compatClip(shape: Shape): Modifier {
        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        return this.drawWithContent {
            val outline = shape.createOutline(size, layoutDirection, density)

            val clipPath =
                when (outline) {
                    is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
                    is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
                    is Outline.Generic -> outline.path
                }

            clipPath(path = clipPath) {
                this@drawWithContent.drawContent()
            }
        }
    }

    @Composable
    fun Modifier.listCardContainerShape(): Modifier =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.clip(MaterialTheme.shapes.large)
        } else {
            this.compatClip(MaterialTheme.shapes.large)
        }

    @Composable
    fun Modifier.listCardItemShape(): Modifier =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.clip(MaterialTheme.shapes.extraSmall)
        } else {
            this.compatClip(MaterialTheme.shapes.extraSmall)
        }
}