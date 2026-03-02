package com.example.comunik.data.models

data class SpeechEntry(
    val id: String,
    val userId: String,
    val text: String,
    val audioUrl: String = "",
    val duration: Long = 0, // Duración en milisegundos
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        return """
        {
            "id": "$id",
            "userId": "$userId",
            "text": "${text.replace("\"", "\\\"")}",
            "audioUrl": "$audioUrl",
            "duration": $duration,
            "createdAt": $createdAt,
            "updatedAt": $updatedAt
        }
        """.trimIndent()
    }
    
    companion object {
        fun fromJson(json: String): SpeechEntry? {
            return try {
                val id = extractJsonValue(json, "id")
                val userId = extractJsonValue(json, "userId")
                val text = extractJsonValue(json, "text")
                val audioUrl = extractJsonValue(json, "audioUrl")
                val duration = extractJsonValue(json, "duration").toLongOrNull() ?: 0L
                val createdAt = extractJsonValue(json, "createdAt").toLongOrNull() ?: System.currentTimeMillis()
                val updatedAt = extractJsonValue(json, "updatedAt").toLongOrNull() ?: System.currentTimeMillis()
                
                SpeechEntry(id, userId, text, audioUrl, duration, createdAt, updatedAt)
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
