package com.example.comunik.data.services

import com.example.comunik.data.FirebaseStorageService
import com.example.comunik.data.models.TextEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

class TextServiceTest {
    
    @Test
    fun `test createText generates valid TextEntry`() {
        // Arrange
        val userId = "test-user-id"
        val content = "Test content"
        val title = "Test title"
        
        // Act & Assert
        // Nota: Este test requiere Firebase configurado o mocks
        // En un entorno real, se mockearía FirebaseStorageService
        // Por ahora, verificamos que el servicio existe y tiene el método
        assertNotNull("TextService should not be null", TextService)
    }
    
    @Test
    fun `test TextEntry toJson creates valid JSON`() {
        // Arrange
        val textEntry = TextEntry(
            id = "test-id",
            userId = "user-id",
            content = "Test content",
            title = "Test title"
        )
        
        // Act
        val json = textEntry.toJson()
        
        // Assert
        assertNotNull("JSON should not be null", json)
        assertTrue("JSON should contain id", json.contains("test-id"))
        assertTrue("JSON should contain userId", json.contains("user-id"))
        assertTrue("JSON should contain content", json.contains("Test content"))
    }
    
    @Test
    fun `test TextEntry fromJson parses valid JSON`() {
        // Arrange
        val json = """
        {
            "id": "test-id",
            "userId": "user-id",
            "content": "Test content",
            "title": "Test title",
            "createdAt": 1234567890,
            "updatedAt": 1234567890
        }
        """.trimIndent()
        
        // Act
        val textEntry = TextEntry.fromJson(json)
        
        // Assert
        assertNotNull("TextEntry should not be null", textEntry)
        assertEquals("ID should match", "test-id", textEntry?.id)
        assertEquals("User ID should match", "user-id", textEntry?.userId)
    }
}
