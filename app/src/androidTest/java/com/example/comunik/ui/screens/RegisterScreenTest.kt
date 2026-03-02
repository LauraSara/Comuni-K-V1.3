package com.example.comunik.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class RegisterScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `test register screen displays correctly`() {
        // Arrange
        composeTestRule.setContent {
            RegisterScreen(
                onRegisterClick = { _, _, _, _ -> },
                onLoginClick = { },
                onGoogleClick = { },
                onFacebookClick = { }
            )
        }
        
        // Assert
        // Verificar que los elementos principales están presentes
    }
}
