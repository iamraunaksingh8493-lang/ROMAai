package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageStatus
import com.example.data.model.SubjectType
import com.example.data.model.VoiceState
import com.example.ui.theme.*
import com.example.ui.viewmodel.RomaUiState
import kotlinx.coroutines.launch

@Composable
fun ChatStudyScreen(
    state: RomaUiState,
    onSendMessage: (String, SubjectType) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onStartVoice: () -> Unit,
    onSelectSubject: (SubjectType) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    // Auto-scroll to latest message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RomaDarkBg)
            .statusBarsPadding()
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ROMA Study Notes & Chat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${state.messages.size} interactions • Tap speaker to listen",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            Row {
                if (state.voiceState == VoiceState.SPEAKING) {
                    IconButton(
                        onClick = onStopSpeaking,
                        modifier = Modifier
                            .testTag("chat_stop_speech_btn")
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Stop Speech",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }

                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.testTag("clear_history_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear Chat History",
                        tint = TextTertiaryDark
                    )
                }
            }
        }

        // --- Subject Selector Strip ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
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
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = subject.color.copy(alpha = 0.2f),
                        selectedLabelColor = Color.White,
                        containerColor = RomaDarkCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) subject.color else Color.Transparent,
                        selectedBorderColor = subject.color,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.testTag("chat_subject_chip_${subject.name.lowercase()}")
                )
            }
        }

        // --- Message History List ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    isCurrentlySpeaking = state.activeSpeakingMessageId == message.id && state.voiceState == VoiceState.SPEAKING,
                    onSpeak = { onSpeakMessage(message.id) },
                    onStopSpeak = onStopSpeaking,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ROMA Study Note", message.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // --- Input Bar ---
        Surface(
            color = RomaDarkSurface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Mic Button
                IconButton(
                    onClick = onStartVoice,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.voiceState == VoiceState.LISTENING) Color(0xFFEF4444)
                            else RomaCyan.copy(alpha = 0.15f)
                        )
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (state.voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (state.voiceState == VoiceState.LISTENING) Color.White else RomaCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask ROMA in ${state.selectedSubject.title}...",
                            fontSize = 14.sp,
                            color = TextTertiaryDark
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RomaDarkCard,
                        unfocusedContainerColor = RomaDarkCard,
                        focusedBorderColor = RomaCyan,
                        unfocusedBorderColor = Color(0xFF263352),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText, state.selectedSubject)
                                inputText = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText, state.selectedSubject)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) RomaCyan else Color(0xFF1E293B)
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Question",
                        tint = if (inputText.isNotBlank()) Color(0xFF041E28) else TextTertiaryDark
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isCurrentlySpeaking: Boolean,
    onSpeak: () -> Unit,
    onStopSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_message_card" else "assistant_message_card"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Tag / Sender Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(RomaCyan, RomaViolet))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("R", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ROMA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RomaCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(message.subject.color.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${message.subject.iconEmoji} ${message.subject.title}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = message.subject.color
                    )
                }
            } else {
                Text(
                    text = "You",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondaryDark
                )
            }
        }

        // Bubble Card
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) RomaCyan.copy(alpha = 0.15f) else RomaDarkCard
            ),
            border = if (isUser) {
                CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RomaCyan.copy(alpha = 0.5f), RomaCyan.copy(alpha = 0.2f))))
            } else {
                CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF263352), Color(0xFF1E293B))))
            },
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (message.status == MessageStatus.SENDING) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = RomaCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = message.text,
                            fontSize = 13.sp,
                            color = TextSecondaryDark,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                } else {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = TextPrimaryDark
                    )

                    // Assistant Message Action Footer: Speak Aloud, Copy, Timestamp
                    if (!isUser) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                            color = Color(0xFF263352)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Speak / Listen Button
                                IconButton(
                                    onClick = {
                                        if (isCurrentlySpeaking) onStopSpeak() else onSpeak()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentlySpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                        contentDescription = if (isCurrentlySpeaking) "Stop voice playback" else "Listen to ROMA voice",
                                        tint = if (isCurrentlySpeaking) RomaCyan else TextSecondaryDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (isCurrentlySpeaking) {
                                    Text(
                                        text = "Speaking...",
                                        fontSize = 11.sp,
                                        color = RomaCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Copy Button
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy text",
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
