package com.example.comunik.util

import android.util.Patterns
import com.example.comunik.data.User


// función de extensión para validar el formato correo

fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Función de extensión para formatear un email
fun String.formatEmail(): String {
    return this.trim().lowercase()
}

// función de extensión para generar un texto  del usuario
fun User.getDisplayInfo(): String {
    return "Usuario: $name (${email.formatEmail()})"
}

// propiedad de extensión para verificar si un string está vacío
val String.esVacio: Boolean
    get() = this.isBlank()

// propiedad de extensión que indica si un usuario tiene datos válidos
val User.esValido: Boolean
    get() = name.isNotBlank() && 
            email.isNotBlank() && 
            email.isValidEmail() && 
            password.isNotBlank() && 
            password.length >= 6
