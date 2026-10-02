package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectType
import com.example.data.model.VoiceState
import com.example.ui.components.RomaAudioWaveform
import com.example.ui.components.RomaVoiceOrb
import com.example.ui.theme.*
import com.example.ui.viewmodel.RomaUiState

@Composable
fun VoiceAssistantScreen(
    state: RomaUiState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSelectSubject: (SubjectType) -> Unit,
    onSendPrompt: (String, SubjectType) -> Unit,
    onOpenSettings: () -> Unit,
    onRequestMicPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Find latest assistant answer for the hero voice card
    val latestAssistantMessage = state.messages.lastOrNull { !it.isUser }
    val latestUserMessage = state.messages.lastOrNull { it.isUser }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RomaDarkBg,
                        Color(0xFF0C1427),
                        RomaDarkBg
                    )
                )
            )
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar: Branding & Controls ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(RomaCyan, RomaViolet))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("R", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ROMA",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RomaCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VOICE AI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RomaCyan
                            )
                        }
                    }
                    Text(
                        text = "Math • Chem • Hist • Civ • Bio • Gram • Geo",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.voiceState == VoiceState.SPEAKING) {
                    IconButton(
                        onClick = onStopSpeaking,
                        modifier = Modifier
                            .testTag("stop_speech_button")
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Stop Voice Speech",
                            tint = Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .testTag("settings_button")
                        .clip(CircleShape)
                        .background(RomaDarkCard)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Voice Assistant Settings",
                        tint = TextSecondaryDark
                    )
                }
            }
        }

        // --- Subject Selector Chips ---
        Text(
            text = "ACADEMIC MASTERY FOCUS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiaryDark,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SubjectType.values()) { subject ->
                val isSelected = state.selectedSubject == subject
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectSubject(subject) },
                    label = {
                        Text(
                            text = "${subject.iconEmoji} ${subject.title}",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = subject.color.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White,
                        containerColor = RomaDarkCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) subject.color else Color.Transparent,
                        selectedBorderColor = subject.color,
                        borderWidth = 1.5.dp,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.testTag("subject_chip_${subject.name.lowercase()}")
                )
            }
        }

        // --- Central Voice Assistant Visualizer Orb ---
        Spacer(modifier = Modifier.height(10.dp))
        RomaVoiceOrb(
            voiceState = state.voiceState,
            rmsLevel = state.rmsLevel,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- Animated Audio Waveform ---
        RomaAudioWaveform(
            voiceState = state.voiceState,
            rmsLevel = state.rmsLevel,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // --- Status Pill ---
        AnimatedContent(
            targetState = state.voiceState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "StatusText"
        ) { vs ->
            val (statusText, statusColor) = when (vs) {
                VoiceState.LISTENING -> "ROMA is listening... Speak your question now" to RomaCyan
                VoiceState.THINKING -> "Synthesizing answer with Gemini..." to RomaViolet
                VoiceState.SPEAKING -> "ROMA is speaking the answer out loud" to RomaBlue
                VoiceState.IDLE -> "Tap the microphone to speak, or pick a question below" to TextSecondaryDark
            }
            Text(
                text = statusText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("voice_status_indicator")
            )
        }

        // --- Live Voice Transcription / Current Question Banner ---
        if (state.currentTranscript.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("live_transcript_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RomaDarkCardHighlight),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RomaCyan, RomaViolet)))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Hearing Voice",
                        tint = RomaCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "\"${state.currentTranscript}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // --- Hero Card: Latest Response Overview ---
        if (latestAssistantMessage != null && state.currentTranscript.isBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("latest_response_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RomaDarkCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(latestAssistantMessage.subject.color.copy(alpha = 0.6f), Color.Transparent)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = latestAssistantMessage.subject.iconEmoji,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = latestAssistantMessage.subject.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = latestAssistantMessage.subject.color
                            )
                        }

                        if (state.voiceState == VoiceState.SPEAKING) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Playing",
                                    fontSize = 11.sp,
                                    color = RomaCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Audio playing",
                                    tint = RomaCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (latestUserMessage != null) {
                        Text(
                            text = "Q: ${latestUserMessage.text}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFF1E293B)
                    )

                    Text(
                        text = latestAssistantMessage.text,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = TextPrimaryDark,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --- Quick Prompts for Selected Subject ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "SUGGESTED QUESTIONS FOR ${state.selectedSubject.title.uppercase()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiaryDark,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            state.selectedSubject.samplePrompts.take(3).forEach { prompt ->
                Surface(
                    onClick = { onSendPrompt(prompt, state.selectedSubject) },
                    shape = RoundedCornerShape(12.dp),
                    color = RomaDarkCard,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF263352), Color(0xFF162036)))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("sample_prompt_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = state.selectedSubject.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = prompt,
                            fontSize = 13.sp,
                            color = TextPrimaryDark,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Ask question",
                            tint = TextTertiaryDark,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // --- Primary Mic Voice Interaction Button ---
        Spacer(modifier = Modifier.height(10.dp))
        val isListening = state.voiceState == VoiceState.LISTENING
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Pulsing aura ring if listening
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(RomaCyan.copy(alpha = 0.25f))
                )
            }

            IconButton(
                onClick = {
                    if (isListening) {
                        onStopListening()
                    } else {
                        onStartListening()
                    }
                },
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .background(
                        Brush.linearGradient(
                            if (isListening) listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                            else listOf(RomaCyan, RomaViolet)
                        )
                    )
                    .testTag("main_mic_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Start Voice Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
