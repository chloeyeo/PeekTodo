package com.chloeyeo.peektodo.ui.reveal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.chloeyeo.peektodo.maskTodoTitle

const val REVEAL_STAGGER_MS = 90
const val REVEAL_DURATION_MS = 420

/**
 * A to-do title that starts as a row of dots and, once [revealed] flips to
 * true, cross-fades and slides into the real text after a delay proportional
 * to [index]. Shared by the lock-screen reveal activity and the in-app
 * fallback so both play the identical animation.
 *
 * If [revealed] is already true on first composition nothing animates.
 */
@Composable
fun RevealText(
    text: String,
    index: Int,
    revealed: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
) {
    val progress by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = REVEAL_DURATION_MS,
            delayMillis = if (revealed) index * REVEAL_STAGGER_MS else 0,
            easing = FastOutSlowInEasing,
        ),
        label = "reveal",
    )
    val slidePx = with(LocalDensity.current) { 16.dp.toPx() }

    Box(modifier) {
        Text(
            text = maskTodoTitle(text),
            style = style,
            color = color,
            modifier = Modifier.graphicsLayer { alpha = 1f - progress },
        )
        Text(
            text = text,
            style = style,
            color = color,
            textDecoration = textDecoration,
            modifier = Modifier.graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * slidePx
            },
        )
    }
}
