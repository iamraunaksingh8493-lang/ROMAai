package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognizerHelper
import com.example.audio.TtsHelper
import com.example.data.model.ChatMessage
import com.example.data.model.MessageStatus
import com.example.data.model.SubjectType
import com.example.data.model.VoiceState
import com.example.data.repository.RomaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AssistantTab {
    VOICE_MODE,
    CHAT_MODE,
    SUBJECT_HUB
}

data class RomaUiState(
    val messages: List<ChatMessage> = emptyList(),
    val voiceState: VoiceState = VoiceState.IDLE,
    val selectedSubject: SubjectType = SubjectType.GENERAL,
    val currentTranscript: String = "",
    val rmsLevel: Float = 0f,
    val isAutoSpeakEnabled: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val activeTab: AssistantTab = AssistantTab.VOICE_MODE,
    val errorMessage: String? = null,
    val activeSpeakingMessageId: String? = null,
    val isMicAvailable: Boolean = true
)

class RomaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RomaRepository()
    private val _uiState = MutableStateFlow(RomaUiState())
    val uiState: StateFlow<RomaUiState> = _uiState.asStateFlow()

    private var ttsHelper: TtsHelper? = null
    private var speechHelper: SpeechRecognizerHelper? = null

    init {
        initTts(application)
        initSpeechRecognizer(application)
        setupInitialWelcomeMessage()
    }

    private fun initTts(context: Application) {
        ttsHelper = TtsHelper(context) {
            // TTS initialized successfully
        }
    }

    private fun initSpeechRecognizer(context: Application) {
        speechHelper = SpeechRecognizerHelper(
            context = context,
            onListeningStateChanged = { listening ->
                _uiState.update { current ->
                    current.copy(
                        voiceState = if (listening) VoiceState.LISTENING else if (current.voiceState == VoiceState.LISTENING) VoiceState.IDLE else current.voiceState
                    )
                }
            },
            onRmsChanged = { rms ->
                _uiState.update { it.copy(rmsLevel = rms) }
            },
            onPartialResult = { partial ->
                _uiState.update { it.copy(currentTranscript = partial) }
            },
            onFinalResult = { finalResult ->
                _uiState.update { it.copy(currentTranscript = finalResult) }
                processUserQuestion(finalResult, _uiState.value.selectedSubject)
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        voiceState = VoiceState.IDLE,
                        errorMessage = error
                    )
                }
            }
        )
        _uiState.update { it.copy(isMicAvailable = speechHelper?.isAvailable() == true) }
    }

    private fun setupInitialWelcomeMessage() {
        val welcome = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = "Greetings! I am ROMA, your AI voice assistant. I am equipped to guide and solve questions in Mathematics, Chemistry, History, Civics, Biology, Grammar, and Geography. Tap the microphone or select a subject below to begin!",
            spokenSummary = "Greetings! I am ROMA, your AI voice assistant. Ask me anything in Maths, Chemistry, History, Civics, Biology, Grammar, or Geography.",
            isUser = false,
            subject = SubjectType.GENERAL
        )
        _uiState.update { it.copy(messages = listOf(welcome)) }
    }

    fun startListening() {
        // Stop any active TTS speaking
        stopSpeaking()
        _uiState.update {
            it.copy(
                errorMessage = null,
                currentTranscript = "",
                voiceState = VoiceState.LISTENING
            )
        }
        speechHelper?.startListening()
    }

    fun stopListening() {
        speechHelper?.stopListening()
        _uiState.update {
            if (it.voiceState == VoiceState.LISTENING) it.copy(voiceState = VoiceState.IDLE) else it
        }
    }

    fun toggleListening() {
        if (_uiState.value.voiceState == VoiceState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun sendQuestion(text: String, subject: SubjectType = _uiState.value.selectedSubject) {
        if (text.isBlank()) return
        stopSpeaking()
        processUserQuestion(text.trim(), subject)
    }

    private fun processUserQuestion(question: String, subject: SubjectType) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = question,
            isUser = true,
            subject = subject
        )

        val thinkingMsgId = UUID.randomUUID().toString()
        val thinkingMsg = ChatMessage(
            id = thinkingMsgId,
            text = "ROMA is formulating your answer...",
            isUser = false,
            subject = subject,
            status = MessageStatus.SENDING
        )

        _uiState.update { current ->
            current.copy(
                messages = current.messages + userMsg + thinkingMsg,
                voiceState = VoiceState.THINKING,
                currentTranscript = ""
            )
        }

        viewModelScope.launch {
            val result = repository.askRoma(
                question = question,
                subject = subject,
                chatHistory = _uiState.value.messages
            )

            result.onSuccess { answerText ->
                val finishedMsg = ChatMessage(
                    id = thinkingMsgId,
                    text = answerText,
                    spokenSummary = answerText,
                    isUser = false,
                    subject = subject,
                    status = MessageStatus.SUCCESS
                )

                _uiState.update { current ->
                    current.copy(
                        messages = current.messages.map { if (it.id == thinkingMsgId) finishedMsg else it },
                        voiceState = VoiceState.IDLE
                    )
                }

                if (_uiState.value.isAutoSpeakEnabled) {
                    speakMessage(thinkingMsgId, answerText)
                }
            }.onFailure { err ->
                val errorMsg = ChatMessage(
                    id = thinkingMsgId,
                    text = "I encountered an issue processing that question. Please try again.",
                    isUser = false,
                    subject = subject,
                    status = MessageStatus.ERROR
                )
                _uiState.update { current ->
                    current.copy(
                        messages = current.messages.map { if (it.id == thinkingMsgId) errorMsg else it },
                        voiceState = VoiceState.IDLE,
                        errorMessage = err.localizedMessage
                    )
                }
            }
        }
    }

    fun speakMessage(messageId: String, textToSpeak: String? = null) {
        val msg = _uiState.value.messages.firstOrNull { it.id == messageId }
        val content = textToSpeak ?: msg?.text ?: return

        stopSpeaking()

        _uiState.update {
            it.copy(
                activeSpeakingMessageId = messageId,
                voiceState = VoiceState.SPEAKING
            )
        }

        ttsHelper?.speak(
            text = content,
            onStart = {
                _uiState.update {
                    it.copy(
                        activeSpeakingMessageId = messageId,
                        voiceState = VoiceState.SPEAKING
                    )
                }
            },
            onDone = {
                _uiState.update {
                    it.copy(
                        activeSpeakingMessageId = null,
                        voiceState = if (it.voiceState == VoiceState.SPEAKING) VoiceState.IDLE else it.voiceState
                    )
                }
            }
        )
    }

    fun stopSpeaking() {
        ttsHelper?.stop()
        _uiState.update {
            it.copy(
                activeSpeakingMessageId = null,
                voiceState = if (it.voiceState == VoiceState.SPEAKING) VoiceState.IDLE else it.voiceState
            )
        }
    }

    fun selectSubject(subject: SubjectType) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun setActiveTab(tab: AssistantTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun updateSettings(speechRate: Float, speechPitch: Float, autoSpeak: Boolean) {
        ttsHelper?.setSpeechRate(speechRate)
        ttsHelper?.setSpeechPitch(speechPitch)
        _uiState.update {
            it.copy(
                speechRate = speechRate,
                speechPitch = speechPitch,
                isAutoSpeakEnabled = autoSpeak
            )
        }
    }

    fun clearHistory() {
        stopSpeaking()
        stopListening()
        setupInitialWelcomeMessage()
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper?.destroy()
        ttsHelper?.shutdown()
    }
}
