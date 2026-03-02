package com.example.comunik.data.services

import com.example.comunik.data.FirebaseAuthService
import com.google.firebase.auth.FirebaseUser

object UserService {
    /**
     * Obtiene el usuario actual
     */
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuthService.getCurrentUser()
    }

    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return FirebaseAuthService.getCurrentUserId()
    }

    /**
     * Obtiene el email del usuario actual
     */
    fun getCurrentUserEmail(): String? {
        return getCurrentUser()?.email
    }

    /**
     * Obtiene el nombre del usuario actual
     */
    fun getCurrentUserName(): String? {
        return getCurrentUser()?.displayName ?: "Usuario"
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuthService.isUserLoggedIn()
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun signOut() {
        FirebaseAuthService.signOut()
    }
}
