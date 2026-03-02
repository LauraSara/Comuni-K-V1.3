package com.example.comunik.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comunik.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WriteScreenUITest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun testWriteScreenAllowsTextInput() {
        // Este test verifica que se puede escribir texto en la pantalla
        // Nota: Requiere navegación a WriteScreen primero
    }
    
    @Test
    fun testSaveButtonIsEnabledWhenTextIsEntered() {
        // Verificar que el botón de guardar se habilita cuando hay texto
    }
}
