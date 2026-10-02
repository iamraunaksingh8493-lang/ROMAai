package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VoiceState
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RomaVoiceOrb(
    voiceState: VoiceState,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransition")

    // Rotation angle for outer holographic ring
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    VoiceState.THINKING -> 2000
                    VoiceState.LISTENING -> 4000
                    VoiceState.SPEAKING -> 3000
                    VoiceState.IDLE -> 8000
                },
                easing = LinearEasing
            )
        ),
        label = "OrbRotation"
    )

    // Breathing pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    VoiceState.SPEAKING -> 450
                    VoiceState.LISTENING -> 700
                    VoiceState.THINKING -> 500
                    VoiceState.IDLE -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Dynamic wave ripples expanding outward
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    VoiceState.LISTENING -> 900
                    VoiceState.SPEAKING -> 1200
                    VoiceState.THINKING -> 800
                    VoiceState.IDLE -> 2400
                },
                easing = LinearEasing
            )
        ),
        label = "RippleProgress"
    )

    // Colors mapping to active state
    val targetPrimaryColor = when (voiceState) {
        VoiceState.LISTENING -> OrbListeningPulse
        VoiceState.THINKING -> OrbThinkingPulse
        VoiceState.SPEAKING -> OrbSpeakingPulse
        VoiceState.IDLE -> OrbIdlePulse
    }

    val primaryGlow by animateColorAsState(
        targetValue = targetPrimaryColor,
        animationSpec = tween(400),
        label = "PrimaryGlow"
    )

    val secondaryGlow by animateColorAsState(
        targetValue = when (voiceState) {
            VoiceState.LISTENING -> Color(0xFF0284C7)
            VoiceState.THINKING -> Color(0xFFEC4899)
            VoiceState.SPEAKING -> Color(0xFF6366F1)
            VoiceState.IDLE -> Color(0xFF3B82F6)
        },
        animationSpec = tween(400),
        label = "SecondaryGlow"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("roma_voice_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.28f

            // Dynamic amplification from audio RMS input
            val audioBoost = (rmsLevel * 24f).coerceIn(0f, 24f)
            val currentRadius = (baseRadius * pulseScale) + audioBoost

            // 1. Expanding background acoustic shockwaves
            if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                for (i in 0..2) {
                    val progress = (rippleProgress + (i * 0.33f)) % 1f
                    val waveRadius = currentRadius + (progress * 55.dp.toPx())
                    val waveAlpha = (1f - progress).coerceIn(0f, 0.45f)

                    drawCircle(
                        color = primaryGlow.copy(alpha = waveAlpha),
                        radius = waveRadius,
                        center = center,
                        style = Stroke(width = (2.5f * (1f - progress)).dp.toPx())
                    )
                }
            }

            // 2. Outer holographic dashed / particle ring
            val outerOrbitRadius = baseRadius * 1.55f
            val segments = 24
            for (i in 0 until segments) {
                val angleRad = Math.toRadians((rotationAngle + (i * (360f / segments))).toDouble())
                val arcRadius = outerOrbitRadius + (sin(angleRad * 3).toFloat() * 4f)
                val x = center.x + (cos(angleRad) * arcRadius).toFloat()
                val y = center.y + (sin(angleRad) * arcRadius).toFloat()
                val dotAlpha = if (i % 2 == 0) 0.6f else 0.25f

                drawCircle(
                    color = secondaryGlow.copy(alpha = dotAlpha),
                    radius = if (i % 4 == 0) 3.5.dp.toPx() else 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // 3. Middle Halo Ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.8f),
                        secondaryGlow.copy(alpha = 0.3f),
                        primaryGlow.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.5f),
                        primaryGlow.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                radius = currentRadius * 1.25f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Central Glowing Radial Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryGlow,
                        secondaryGlow,
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.15f
                ),
                radius = currentRadius,
                center = center
            )

            // 5. High-intensity Core Light
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = currentRadius * 0.35f,
                center = center
            )
        }

        // Center Voice State Text or Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ROMA",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = when (voiceState) {
                    VoiceState.LISTENING -> "Listening"
                    VoiceState.THINKING -> "Thinking"
                    VoiceState.SPEAKING -> "Speaking"
                    VoiceState.IDLE -> "Ready"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryGlow
            )
        }
    }
}

/**
 * Animated waveform equalizer bars reacting to speech audio input.
 */
@Composable
fun RomaAudioWaveform(
    voiceState: VoiceState,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformTransition")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (voiceState == VoiceState.SPEAKING) 800 else 1200,
                easing = LinearEasing
            )
        ),
        label = "WaveformPhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("roma_audio_waveform")
    ) {
        val barCount = 28
        val spacing = size.width / (barCount + 1)
        val centerY = size.height / 2f
        val maxBarHeight = size.height * 0.85f

        for (i in 0 until barCount) {
            val x = spacing * (i + 1)

            val heightMultiplier = when (voiceState) {
                VoiceState.LISTENING -> {
                    val wave = (sin(phase + (i * 0.35f)) + 1f) / 2f
                    (0.2f + (rmsLevel * 0.8f * wave)).coerceIn(0.1f, 1f)
                }
                VoiceState.SPEAKING -> {
                    val wave = (sin(phase * 1.5f + (i * 0.45f)) + 1f) / 2f
                    (0.25f + (0.75f * wave)).coerceIn(0.15f, 1f)
                }
                VoiceState.THINKING -> {
                    val wave = (sin(phase * 2.0f + (i * 0.5f)) + 1f) / 2f
                    0.2f + (0.3f * wave)
                }
                VoiceState.IDLE -> {
                    val wave = (sin(phase * 0.5f + (i * 0.2f)) + 1f) / 2f
                    0.08f + (0.12f * wave)
                }
            }

            val barHeight = maxBarHeight * heightMultiplier
            val barTop = centerY - (barHeight / 2f)
            val barBottom = centerY + (barHeight / 2f)

            val barColor = when (voiceState) {
                VoiceState.LISTENING -> RomaCyan
                VoiceState.THINKING -> RomaViolet
                VoiceState.SPEAKING -> RomaBlue
                VoiceState.IDLE -> TextTertiaryDark
            }

            drawLine(
                color = barColor,
                start = Offset(x, barTop),
                end = Offset(x, barBottom),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
