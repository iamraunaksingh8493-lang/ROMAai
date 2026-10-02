package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ChatStudyScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.screens.SubjectHubScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantTab
import com.example.ui.viewmodel.RomaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RomaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RomaTheme(darkTheme = true) {
                RomaAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RomaAppContent(viewModel: RomaViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Audio Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val handleVoiceTrigger = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Android System Back Navigation handling
    BackHandler(enabled = uiState.activeTab != AssistantTab.VOICE_MODE) {
        viewModel.setActiveTab(AssistantTab.VOICE_MODE)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(RomaDarkBg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = RomaDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roma_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == AssistantTab.VOICE_MODE,
                    onClick = { viewModel.setActiveTab(AssistantTab.VOICE_MODE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AssistantTab.VOICE_MODE) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq,
                            contentDescription = "Voice Assistant Mode"
                        )
                    },
                    label = {
                        Text(
                            text = "ROMA Voice",
                            fontWeight = if (uiState.activeTab == AssistantTab.VOICE_MODE) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF041E28),
                        selectedTextColor = RomaCyan,
                        indicatorColor = RomaCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextTertiaryDark
                    ),
                    modifier = Modifier.testTag("nav_voice_tab")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == AssistantTab.CHAT_MODE,
                    onClick = { viewModel.setActiveTab(AssistantTab.CHAT_MODE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AssistantTab.CHAT_MODE) Icons.Filled.Forum else Icons.Outlined.Forum,
                            contentDescription = "Chat and Study Mode"
                        )
                    },
                    label = {
                        Text(
                            text = "Study & Chat",
                            fontWeight = if (uiState.activeTab == AssistantTab.CHAT_MODE) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF041E28),
                        selectedTextColor = RomaCyan,
                        indicatorColor = RomaCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextTertiaryDark
                    ),
                    modifier = Modifier.testTag("nav_chat_tab")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == AssistantTab.SUBJECT_HUB,
                    onClick = { viewModel.setActiveTab(AssistantTab.SUBJECT_HUB) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AssistantTab.SUBJECT_HUB) Icons.Filled.School else Icons.Outlined.School,
                            contentDescription = "Subject Hub"
                        )
                    },
                    label = {
                        Text(
                            text = "Subjects",
                            fontWeight = if (uiState.activeTab == AssistantTab.SUBJECT_HUB) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF041E28),
                        selectedTextColor = RomaCyan,
                        indicatorColor = RomaCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextTertiaryDark
                    ),
                    modifier = Modifier.testTag("nav_subjects_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (uiState.activeTab) {
                AssistantTab.VOICE_MODE -> {
                    VoiceAssistantScreen(
                        state = uiState,
                        onStartListening = handleVoiceTrigger,
                        onStopListening = { viewModel.stopListening() },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onSelectSubject = { viewModel.selectSubject(it) },
                        onSendPrompt = { prompt, subject ->
                            viewModel.sendQuestion(prompt, subject)
                        },
                        onOpenSettings = { showSettingsSheet = true },
                        onRequestMicPermission = {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }

                AssistantTab.CHAT_MODE -> {
                    ChatStudyScreen(
                        state = uiState,
                        onSendMessage = { text, subject ->
                            viewModel.sendQuestion(text, subject)
                        },
                        onSpeakMessage = { messageId ->
                            viewModel.speakMessage(messageId)
                        },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onStartVoice = handleVoiceTrigger,
                        onSelectSubject = { viewModel.selectSubject(it) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }

                AssistantTab.SUBJECT_HUB -> {
                    SubjectHubScreen(
                        onSelectAndAskPrompt = { prompt, subject ->
                            viewModel.selectSubject(subject)
                            viewModel.sendQuestion(prompt, subject)
                            viewModel.setActiveTab(AssistantTab.VOICE_MODE)
                        }
                    )
                }
            }
        }

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            SettingsBottomSheet(
                speechRate = uiState.speechRate,
                speechPitch = uiState.speechPitch,
                isAutoSpeakEnabled = uiState.isAutoSpeakEnabled,
                onSaveSettings = { rate, pitch, autoSpeak ->
                    viewModel.updateSettings(rate, pitch, autoSpeak)
                },
                onClearHistory = { viewModel.clearHistory() },
                onDismiss = { showSettingsSheet = false }
            )
        }
    }
}
