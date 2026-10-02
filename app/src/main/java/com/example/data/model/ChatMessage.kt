package com.example.data.model

import java.util.UUID

enum class MessageStatus {
    SENDING,
    SUCCESS,
    ERROR
}

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val spokenSummary: String = text,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val subject: SubjectType = SubjectType.GENERAL,
    val isAudioPlaying: Boolean = false,
    val status: MessageStatus = MessageStatus.SUCCESS
)
