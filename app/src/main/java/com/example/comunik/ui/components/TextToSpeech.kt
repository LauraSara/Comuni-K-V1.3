package com.example.comunik.ui.components

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

interface TextToSpeechController {
    val isAvailable: Boolean
    val isSpeaking: Boolean
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH)
    fun stop()
}

@Composable
fun rememberTextToSpeech(
    onInit: (Boolean) -> Unit = {}
): TextToSpeechController {
    val context = LocalContext.current
    var isAvailable by remember { mutableStateOf(false) }
    val isSpeakingState = remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    val tts = remember {
        TextToSpeech(context, object : TextToSpeech.OnInitListener {
            override fun onInit(status: Int) {
                isInitialized = true
            }
        })
    }
    
    LaunchedEffect(tts, isInitialized) {
        if (isInitialized) {
            val result = tts.setLanguage(Locale("es", "ES"))
            isAvailable = result != TextToSpeech.LANG_MISSING_DATA && 
                         result != TextToSpeech.LANG_NOT_SUPPORTED
            onInit(isAvailable)
            
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeakingState.value = true
                }
                
                override fun onDone(utteranceId: String?) {
                    isSpeakingState.value = false
                }
                
                override fun onError(utteranceId: String?) {
                    isSpeakingState.value = false
                }
            })
        }
    }
    
    DisposableEffect(tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    
    return remember {
        object : TextToSpeechController {
            override val isAvailable: Boolean
                get() = isAvailable
            
            override val isSpeaking: Boolean
                get() = isSpeakingState.value
            
            override fun speak(text: String, queueMode: Int) {
                if (isAvailable && text.isNotBlank()) {
                    val utteranceId = System.currentTimeMillis().toString()
                    tts.speak(text, queueMode, null, utteranceId)
                }
            }
            
            override fun stop() {
                tts.stop()
                isSpeakingState.value = false
            }
        }
    }
}
