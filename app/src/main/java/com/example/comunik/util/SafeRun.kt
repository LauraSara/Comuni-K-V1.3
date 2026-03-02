package com.example.comunik.util

// funcion inline para ejcutar bloques de código

inline fun <T> safeRun(block: () -> T): T? {
    return try {
        block()
    } catch (e: Exception) {
        null
    }
}
