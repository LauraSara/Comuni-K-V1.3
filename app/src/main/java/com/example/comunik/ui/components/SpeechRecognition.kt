package com.example.comunik.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberSpeechRecognizer(
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
    onReady: () -> Unit = {},
    onBeginning: () -> Unit = {},
    onEnd: () -> Unit = {}
): SpeechRecognizerState {
    val context = LocalContext.current
    val speechRecognizer = remember { 
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }
    
    val state = remember { mutableStateOf(SpeechRecognizerState()) }
    
    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onReady()
                state.value = state.value.copy(isListening = true)
            }
            
            override fun onBeginningOfSpeech() {
                onBeginning()
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                state.value = state.value.copy(rmsdB = rmsdB)
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
            }
            
            override fun onEndOfSpeech() {
                onEnd()
                state.value = state.value.copy(isListening = false)
            }
            
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se encontró coincidencia"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout de habla"
                    else -> "Error desconocido"
                }
                onError(errorMessage)
                state.value = state.value.copy(isListening = false, error = errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.get(0) ?: ""
                if (text.isNotBlank()) {
                    onResult(text)
                }
                state.value = state.value.copy(isListening = false, lastResult = text)
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.get(0) ?: ""
                state.value = state.value.copy(partialResult = text)
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
            }
        }
        
        speechRecognizer?.setRecognitionListener(listener)
        
        onDispose {
            speechRecognizer?.destroy()
        }
    }
    
    return state.value
}

data class SpeechRecognizerState(
    val isListening: Boolean = false,
    val rmsdB: Float = 0f,
    val lastResult: String = "",
    val partialResult: String = "",
    val error: String? = null
)

fun createSpeechRecognizerIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
}
