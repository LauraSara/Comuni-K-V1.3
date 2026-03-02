package com.example.comunik.data.services

import com.example.comunik.data.FirebaseAuthService
import org.junit.Assert.*
import org.junit.Test

class UserServiceTest {
    
    @Test
    fun `test getCurrentUserId returns null when no user logged in`() {
        // Arrange & Act
        val userId = UserService.getCurrentUserId()
        
        // Assert
        // En un entorno de prueba sin Firebase inicializado, debería retornar null
        // Este test verifica que el servicio maneja correctamente el caso sin usuario
        assertNull("User ID should be null when no user is logged in", userId)
    }
    
    @Test
    fun `test isUserLoggedIn returns false when no user logged in`() {
        // Arrange & Act
        val isLoggedIn = UserService.isUserLoggedIn()
        
        // Assert
        assertFalse("Should return false when no user is logged in", isLoggedIn)
    }
    
    @Test
    fun `test getCurrentUser returns null when no user logged in`() {
        // Arrange & Act
        val user = UserService.getCurrentUser()
        
        // Assert
        assertNull("User should be null when no user is logged in", user)
    }
}
