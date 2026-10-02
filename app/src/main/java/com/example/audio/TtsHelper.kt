package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context, onInitSuccess: (() -> Unit)? = null) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentRate: Float = 1.0f
    private var currentPitch: Float = 1.0f

    private var onStartCallback: (() -> Unit)? = null
    private var onDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("TtsHelper", "US English not supported, falling back to default locale")
                    tts?.language = Locale.getDefault()
                }
                isInitialized = true
                tts?.setSpeechRate(currentRate)
                tts?.setPitch(currentPitch)
                onInitSuccess?.invoke()
            } else {
                Log.e("TtsHelper", "TTS Initialization failed with status: $status")
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onStartCallback?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                onDoneCallback?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onDoneCallback?.invoke()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onDoneCallback?.invoke()
            }
        })
    }

    fun speak(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit
    ) {
        if (!isInitialized) {
            Log.w("TtsHelper", "TTS not initialized yet")
            onDone()
            return
        }

        stop()
        onStartCallback = onStart
        onDoneCallback = onDone

        val cleanSpeechText = cleanTextForSpeech(text)
        if (cleanSpeechText.isBlank()) {
            onDone()
            return
        }

        val utteranceId = "roma_speech_${System.currentTimeMillis()}"
        tts?.speak(cleanSpeechText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
        onDoneCallback?.invoke()
    }

    fun setSpeechRate(rate: Float) {
        currentRate = rate.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            tts?.setSpeechRate(currentRate)
        }
    }

    fun setSpeechPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 1.5f)
        if (isInitialized) {
            tts?.setPitch(currentPitch)
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    /**
     * Prepares rich text for smooth spoken audio by stripping markdown,
     * translating math symbols, and normalizing formatting.
     */
    private fun cleanTextForSpeech(text: String): String {
        var clean = text
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), "Code section omitted for speech.")
            // Remove inline code ticks
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove markdown headers # ## ###
            .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "")
            // Remove bold/italics
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            // Remove markdown links [title](url) -> title
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            // Clean common math and chemical notations for natural speaking
            .replace("²", " squared ")
            .replace("³", " cubed ")
            .replace("√", " square root of ")
            .replace("≠", " is not equal to ")
            .replace("≈", " is approximately ")
            .replace("≤", " is less than or equal to ")
            .replace("≥", " is greater than or equal to ")
            .replace("±", " plus or minus ")
            .replace("×", " times ")
            .replace("÷", " divided by ")
            .replace("CO₂", "C O 2")
            .replace("H₂O", "H 2 O")
            .replace("O₂", "O 2")
            .replace("->", " yields ")
            .replace("→", " yields ")
            // Remove excessive bullet symbols
            .replace(Regex("^[•\\-*]\\s+", RegexOption.MULTILINE), "")
            // Normalize spaces
            .replace(Regex("\\s+"), " ")
            .trim()

        // If the response is very long, TTS gives a natural spoken delivery of up to 400 words
        val words = clean.split(" ")
        if (words.size > 250) {
            clean = words.take(250).joinToString(" ") + "... You can read the complete detailed step-by-step breakdown on your screen."
        }

        return clean
    }
}
