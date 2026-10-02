package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class SpeechRecognizerHelper(
    private val context: Context,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    var isListening: Boolean = false
        private set

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
            } catch (e: Exception) {
                Log.e("SpeechRecognizerHelper", "Failed to init speech recognizer", e)
                speechRecognizer = null
            }
        } else {
            Log.w("SpeechRecognizerHelper", "Speech recognition not available on this device")
        }
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        if (speechRecognizer == null) {
            onError("Speech recognition service is not available on this device. You can type your question directly.")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            onListeningStateChanged(true)
        } catch (e: Exception) {
            Log.e("SpeechRecognizerHelper", "Error starting listening", e)
            isListening = false
            onListeningStateChanged(false)
            onError("Could not access microphone: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("SpeechRecognizerHelper", "Error stopping listening", e)
        } finally {
            isListening = false
            onListeningStateChanged(false)
            onRmsChanged(0f)
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e("SpeechRecognizerHelper", "Error canceling listening", e)
        } finally {
            isListening = false
            onListeningStateChanged(false)
            onRmsChanged(0f)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("SpeechRecognizerHelper", "Error destroying speech recognizer", e)
        } finally {
            speechRecognizer = null
            isListening = false
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onListeningStateChanged(true)
            }

            override fun onBeginningOfSpeech() {
                // Speech started
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Typical values range from -2dB to 10dB
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                onRmsChanged(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                onListeningStateChanged(false)
                onRmsChanged(0f)
            }

            override fun onError(error: Int) {
                isListening = false
                onListeningStateChanged(false)
                onRmsChanged(0f)

                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "Client-side recognition error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap the mic and try again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Please try again."
                    SpeechRecognizer.ERROR_SERVER -> "Recognition server error."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap to speak again."
                    else -> "Speech recognition error code: $error"
                }
                Log.w("SpeechRecognizerHelper", errorMessage)
                // Don't toast for normal no match/timeout, just notify cleanly
                if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    onError(errorMessage)
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onListeningStateChanged(false)
                onRmsChanged(0f)

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()
                if (!recognizedText.isNullOrBlank()) {
                    onFinalResult(recognizedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim()
                if (!text.isNullOrBlank()) {
                    onPartialResult(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
