package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    speechRate: Float,
    speechPitch: Float,
    isAutoSpeakEnabled: Boolean,
    onSaveSettings: (rate: Float, pitch: Float, autoSpeak: Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var rate by remember { mutableStateOf(speechRate) }
    var pitch by remember { mutableStateOf(speechPitch) }
    var autoSpeak by remember { mutableStateOf(isAutoSpeakEnabled) }

    val hasCustomApiKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    ModalBottomSheet(
        onDismissRequest = {
            onSaveSettings(rate, pitch, autoSpeak)
            onDismiss()
        },
        containerColor = RomaDarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) },
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Voice Assistant Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Auto-speak Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Speak Answers",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "ROMA speaks responses aloud automatically",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
                Switch(
                    checked = autoSpeak,
                    onCheckedChange = { autoSpeak = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF041E28),
                        checkedTrackColor = RomaCyan
                    ),
                    modifier = Modifier.testTag("auto_speak_switch")
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF263352)
            )

            // Speech Rate Slider
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Speech Rate (Speed)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "${((rate * 10).roundToInt() / 10.0)}x",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RomaCyan
                    )
                }
                Slider(
                    value = rate,
                    onValueChange = { rate = it },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = RomaCyan,
                        activeTrackColor = RomaCyan,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("speech_rate_slider")
                )
            }

            // Speech Pitch Slider
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Voice Pitch",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "${((pitch * 10).roundToInt() / 10.0)}x",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RomaViolet
                    )
                }
                Slider(
                    value = pitch,
                    onValueChange = { pitch = it },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = RomaViolet,
                        activeTrackColor = RomaViolet,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("speech_pitch_slider")
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF263352)
            )

            // Intelligence Engine Status
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomaDarkCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasCustomApiKey) Icons.Default.CheckCircle else Icons.Default.Key,
                        contentDescription = null,
                        tint = if (hasCustomApiKey) Color(0xFF10B981) else RomaCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (hasCustomApiKey) "Gemini Cloud Connected" else "Dual-Engine AI Active",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (hasCustomApiKey) "Live Gemini 3.5 Flash online" else "Online Gemini API + Built-in Academic Engine",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clear Chat History Button
            OutlinedButton(
                onClick = {
                    onClearHistory()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clear_history_action_btn")
            ) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Clear Conversation History")
            }
        }
    }
}
