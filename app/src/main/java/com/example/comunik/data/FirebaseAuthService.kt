package com.example.comunik.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

object FirebaseAuthService {
    private val TAG = "FirebaseAuthService"
    
    private fun ensureFirebaseInitialized(): Boolean {
        return try {
            val defaultApp = FirebaseApp.getInstance()
            Log.d(TAG, "Firebase inicializado correctamente. App: ${defaultApp.name}")
            true
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error al verificar inicialización de Firebase: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al verificar Firebase: ${e.message}", e)
            false
        }
    }
    
    private val auth: FirebaseAuth by lazy {
        if (!ensureFirebaseInitialized()) {
            Log.w(TAG, "Firebase no está inicializado, pero se intentará usar FirebaseAuth de todas formas")
        }
        FirebaseAuth.getInstance()
    }

    suspend fun register(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            if (!ensureFirebaseInitialized()) {
                val errorMsg = "Firebase no está inicializado. Verifica que google-services.json esté en app/google-services.json y que el plugin de Google Services esté configurado."
                Log.e(TAG, errorMsg)
                return Result.failure(IllegalStateException(errorMsg))
            }
            
            Log.d(TAG, "Intentando registrar usuario: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                Log.d(TAG, "Usuario registrado exitosamente: ${user.uid}")
                Result.success(user)
            } else {
                Log.e(TAG, "No se pudo crear el usuario: result.user es null")
                Result.failure(Exception("No se pudo crear el usuario"))
            }
        } catch (e: FirebaseAuthException) {
            val errorCode = e.errorCode
            val errorMessage = e.message
            Log.e(TAG, "Error de Firebase Auth al registrar: code=$errorCode, message=$errorMessage", e)
            Result.failure(e)
        } catch (e: IllegalStateException) {
            val errorMsg = e.message ?: "Firebase no está inicializado correctamente"
            Log.e(TAG, "Error de estado de Firebase: $errorMsg", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al registrar usuario: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (!ensureFirebaseInitialized()) {
                val errorMsg = "Firebase no está inicializado. Verifica que google-services.json esté en app/google-services.json y que el plugin de Google Services esté configurado."
                Log.e(TAG, errorMsg)
                return Result.failure(IllegalStateException(errorMsg))
            }
            
            Log.d(TAG, "Intentando iniciar sesión: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Log.d(TAG, "Sesión iniciada exitosamente: ${user.uid}")
                Result.success(user)
            } else {
                Log.e(TAG, "No se pudo iniciar sesión: result.user es null")
                Result.failure(Exception("No se pudo iniciar sesión"))
            }
        } catch (e: FirebaseAuthException) {
            val errorCode = e.errorCode
            val errorMessage = e.message
            Log.e(TAG, "Error de Firebase Auth al iniciar sesión: code=$errorCode, message=$errorMessage", e)
            Result.failure(e)
        } catch (e: IllegalStateException) {
            val errorMsg = e.message ?: "Firebase no está inicializado correctamente"
            Log.e(TAG, "Error de estado de Firebase: $errorMsg", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al iniciar sesión: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (!ensureFirebaseInitialized()) {
                val errorMsg = "Firebase no está inicializado. Verifica que google-services.json esté en app/google-services.json."
                Log.e(TAG, errorMsg)
                return Result.failure(IllegalStateException(errorMsg))
            }
            
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Email de recuperación enviado a: $email")
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            val errorCode = e.errorCode
            val errorMessage = e.message
            Log.e(TAG, "Error de Firebase Auth al enviar email: code=$errorCode, message=$errorMessage", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al enviar email de recuperación: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
