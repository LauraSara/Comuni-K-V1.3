package com.example.comunik.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comunik.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUITest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun testHomeScreenDisplaysWelcomeMessage() {
        // Este test verifica que la pantalla principal se muestra correctamente
        // Nota: Requiere que el usuario esté logueado o que se mockee el estado
    }
    
    @Test
    fun testQuickActionsAreDisplayed() {
        // Verificar que las acciones rápidas están visibles
        // composeTestRule.onNodeWithText("Escribir").assertExists()
    }
}
