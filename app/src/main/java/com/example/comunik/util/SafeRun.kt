package com.example.comunik.util

// Función inline para ejecutar bloques de código

inline fun <T> safeRun(block: () -> T): T? {
    return try {
        block()
    } catch (e: Exception) {
        null
    }
}
