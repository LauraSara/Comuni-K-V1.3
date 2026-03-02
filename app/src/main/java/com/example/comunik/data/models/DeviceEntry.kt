package com.example.comunik.data.models

data class DeviceEntry(
    val id: String,
    val userId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        return """
        {
            "id": "$id",
            "userId": "$userId",
            "name": "$name",
            "latitude": $latitude,
            "longitude": $longitude,
            "address": "$address",
            "createdAt": $createdAt,
            "updatedAt": $updatedAt
        }
        """.trimIndent()
    }
    
    companion object {
        fun fromJson(json: String): DeviceEntry? {
            return try {
                val id = extractJsonValue(json, "id")
                val userId = extractJsonValue(json, "userId")
                val name = extractJsonValue(json, "name")
                val latitude = extractJsonValue(json, "latitude").toDoubleOrNull() ?: 0.0
                val longitude = extractJsonValue(json, "longitude").toDoubleOrNull() ?: 0.0
                val address = extractJsonValue(json, "address")
                val createdAt = extractJsonValue(json, "createdAt").toLongOrNull() ?: System.currentTimeMillis()
                val updatedAt = extractJsonValue(json, "updatedAt").toLongOrNull() ?: System.currentTimeMillis()
                
                DeviceEntry(id, userId, name, latitude, longitude, address, createdAt, updatedAt)
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
