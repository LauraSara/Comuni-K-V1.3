package com.example.comunik.data.models

import java.util.Date

data class TextEntry(
    val id: String,
    val userId: String,
    val content: String,
    val title: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        return """
        {
            "id": "$id",
            "userId": "$userId",
            "content": "${content.replace("\"", "\\\"")}",
            "title": "$title",
            "createdAt": $createdAt,
            "updatedAt": $updatedAt
        }
        """.trimIndent()
    }
    
    companion object {
        fun fromJson(json: String): TextEntry? {
            return try {
                val id = extractJsonValue(json, "id")
                val userId = extractJsonValue(json, "userId")
                val content = extractJsonValue(json, "content")
                val title = extractJsonValue(json, "title")
                val createdAt = extractJsonValue(json, "createdAt").toLongOrNull() ?: System.currentTimeMillis()
                val updatedAt = extractJsonValue(json, "updatedAt").toLongOrNull() ?: System.currentTimeMillis()
                
                TextEntry(id, userId, content, title, createdAt, updatedAt)
            } catch (e: Exception) {
                null
            }
        }
        
        private fun extractJsonValue(json: String, key: String): String {
            val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val match = pattern.find(json)
            return match?.groupValues?.get(1) ?: ""
        }
    }
}
