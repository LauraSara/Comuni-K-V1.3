package com.example.comunik.data

import com.example.comunik.data.exceptions.InvalidCredentialsException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {
    
    @Before
    fun setUp() {
        // Deshabilitar Firebase para pruebas locales
        AuthRepository.setUseFirebaseForTesting(false)
    }
    
    @Test
    fun `test login with valid credentials`() = runBlocking {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        
        // Act
        val result = AuthRepository.register(email, password, name)
        assertTrue("Registration should succeed", result.success)
        
        val user = AuthRepository.login(email, password)
        
        // Assert
        assertNotNull("User should not be null", user)
        assertEquals("Email should match", email, user?.email)
        assertEquals("Name should match", name, user?.name)
    }
    
    @Test
    fun `test login with invalid credentials`() = runBlocking {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val wrongPassword = "wrongpassword"
        
        // Act
        AuthRepository.register(email, password, "Test User")
        val user = AuthRepository.login(email, wrongPassword)
        
        // Assert
        assertNull("User should be null for wrong password", user)
    }
    
    @Test
    fun `test loginWithExceptions throws exception for invalid credentials`() = runBlocking {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val wrongPassword = "wrongpassword"
        
        // Act
        AuthRepository.register(email, password, "Test User")
        
        // Assert
        try {
            AuthRepository.loginWithExceptions(email, wrongPassword)
            fail("Should throw InvalidCredentialsException")
        } catch (e: InvalidCredentialsException) {
            assertTrue("Exception message should contain error", e.message?.contains("incorrectos") == true)
        }
    }
    
    @Test
    fun `test register with valid data`() = runBlocking {
        // Arrange
        val email = "newuser@example.com"
        val password = "password123"
        val name = "New User"
        
        // Act
        val result = AuthRepository.register(email, password, name)
        
        // Assert
        assertTrue("Registration should succeed", result.success)
        assertTrue("User should exist", AuthRepository.userExists(email))
    }
    
    @Test
    fun `test userExists returns true for existing user`() = runBlocking {
        // Arrange
        val email = "existing@example.com"
        val password = "password123"
        
        // Act
        AuthRepository.register(email, password, "Existing User")
        val exists = AuthRepository.userExists(email)
        
        // Assert
        assertTrue("User should exist", exists)
    }
    
    @Test
    fun `test userExists returns false for non-existing user`() {
        // Arrange
        val email = "nonexisting@example.com"
        
        // Act
        val exists = AuthRepository.userExists(email)
        
        // Assert
        assertFalse("User should not exist", exists)
    }
}
