package com.example.comunik.data

import android.util.Log
import com.example.comunik.util.safeRun
import com.example.comunik.util.esValido
import com.example.comunik.data.exceptions.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.TimeoutCancellationException

object AuthRepository {
    private val TAG = "AuthRepository"
    private val registeredUsers: Array<User?> = arrayOfNulls(5)
    
    private var registeredCount: Int = 0
    
    private var _useFirebase: Boolean = true
    var useFirebase: Boolean
        get() = _useFirebase
        private set(value) { _useFirebase = value }
    
    fun setUseFirebaseForTesting(enabled: Boolean) {
        _useFirebase = enabled
    }
    
    private fun checkFirebaseConfiguration(): String? {
        return try {
            val defaultApp = FirebaseApp.getInstance()
            val appName = defaultApp.name
            val options = defaultApp.options
            val projectId = options.projectId
            
            Log.d(TAG, "Firebase configurado correctamente - App: $appName, ProjectId: $projectId")
            null // No hay error
        } catch (e: IllegalStateException) {
            val errorMsg = "Firebase no está inicializado: ${e.message}. Verifica que el archivo google-services.json esté en app/google-services.json y que hayas sincronizado el proyecto."
            Log.e(TAG, errorMsg, e)
            errorMsg
        } catch (e: Exception) {
            val errorMsg = "Error al verificar Firebase: ${e.javaClass.simpleName} - ${e.message}"
            Log.e(TAG, errorMsg, e)
            errorMsg
        }
    }

    fun login(email: String, password: String): User? {
        if (useFirebase) {
            return runBlocking {
                val result = FirebaseAuthService.login(email, password)
                result.getOrNull()?.let { firebaseUser ->
                    User(
                        email = firebaseUser.email ?: email,
                        password = "", // No almacenamos contraseña
                        name = firebaseUser.displayName ?: "Usuario"
                    )
                }
            }
        }
        
        for (i in registeredUsers.indices) {
            val user = registeredUsers[i]
            if (user != null && 
                user.email.equals(email, ignoreCase = true) && 
                user.password == password) {
                return user
            }
        }
        return null
    }

    data class RegisterResult(
        val success: Boolean,
        val errorMessage: String? = null,
        val errorType: RegisterErrorType? = null
    )
    
    enum class RegisterErrorType {
        USER_ALREADY_EXISTS,
        USER_LIMIT_REACHED,
        FIREBASE_NOT_CONFIGURED,
        FIREBASE_TIMEOUT,
        FIREBASE_ERROR,
        FIREBASE_AUTH_DISABLED,
        INVALID_DATA,
        UNKNOWN_ERROR
    }
    
    suspend fun register(email: String, password: String, name: String): RegisterResult {
        val newUser = User(email = email, password = password, name = name)
        if (!newUser.esValido) {
            return RegisterResult(
                success = false,
                errorMessage = "Los datos del usuario no son válidos",
                errorType = RegisterErrorType.INVALID_DATA
            )
        }
        
        if (useFirebase) {
            try {
                val firebaseResult = withTimeoutOrNull(15000) {
                    FirebaseAuthService.register(email, password, name)
                }
                
                if (firebaseResult != null && firebaseResult.isSuccess) {
                    var existsLocally = false
                    for (i in registeredUsers.indices) {
                        val user = registeredUsers[i]
                        if (user != null && user.email.equals(email, ignoreCase = true)) {
                            existsLocally = true
                            break
                        }
                    }
                    
                    if (!existsLocally && registeredCount < 5) {
                        for (i in registeredUsers.indices) {
                            if (registeredUsers[i] == null) {
                                registeredUsers[i] = newUser
                                registeredCount++
                                break
                            }
                        }
                    }
                    
                    return RegisterResult(success = true)
                } else {
                    val exception = firebaseResult?.exceptionOrNull()
                    val errorMessage = exception?.message ?: ""
                    val errorCode = exception?.javaClass?.simpleName ?: ""
                    val fullErrorDetails = buildString {
                        append("Error: $errorCode\n")
                        append("Mensaje: $errorMessage\n")
                        if (exception != null) {
                            append("Tipo: ${exception.javaClass.name}\n")
                            exception.cause?.let {
                                append("Causa: ${it.javaClass.simpleName} - ${it.message}\n")
                            }
                        }
                    }
                    
                    Log.e(TAG, "Error al registrar en Firebase:\n$fullErrorDetails", exception)
                    
                    val isConfigurationError = errorMessage.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
                        errorMessage.contains("configuration not found", ignoreCase = true) ||
                        errorMessage.contains("google-services.json", ignoreCase = true) ||
                        errorMessage.contains("Firebase no está inicializado", ignoreCase = true) ||
                        errorMessage.contains("FirebaseApp", ignoreCase = true) ||
                        errorCode.contains("IllegalStateException", ignoreCase = true) ||
                        exception is IllegalStateException
                    
                    if (isConfigurationError) {
                        var savedLocally = false
                        var localError: String? = null
                        
                        var existsLocally = false
                        for (i in registeredUsers.indices) {
                            val user = registeredUsers[i]
                            if (user != null && user.email.equals(email, ignoreCase = true)) {
                                existsLocally = true
                                break
                            }
                        }
                        
                        if (!existsLocally) {
                            if (registeredCount < 5) {
                                for (i in registeredUsers.indices) {
                                    if (registeredUsers[i] == null) {
                                        registeredUsers[i] = newUser
                                        registeredCount++
                                        savedLocally = true
                                        break
                                    }
                                }
                            } else {
                                localError = "Se alcanzó el límite de usuarios (5 máximo)"
                            }
                        } else {
                            localError = "El correo electrónico ya está registrado localmente"
                        }
                        
                        val firebaseStatus = checkFirebaseConfiguration()
                        val detailedMessage = buildString {
                            append("Firebase no está configurado correctamente.\n\n")
                            if (firebaseStatus != null) {
                                append("Detalles: $firebaseStatus\n\n")
                            }
                            append("El archivo google-services.json no se encontró o no está configurado correctamente.\n\n")
                            append("Para solucionarlo:\n")
                            append("1. Ve a Firebase Console (console.firebase.google.com)\n")
                            append("2. Descarga el archivo google-services.json\n")
                            append("3. Colócalo en: app/google-services.json\n")
                            append("4. Verifica que el package name coincida con: com.example.comunik\n")
                            append("5. Sincroniza el proyecto (Sync Project with Gradle Files)\n")
                            append("6. Limpia y reconstruye el proyecto (Build > Clean Project)\n")
                            append("7. Reinicia la aplicación\n\n")
                            if (savedLocally) {
                                append("El usuario se guardó localmente pero se perderá al reiniciar la app.")
                            } else {
                                append("No se pudo guardar localmente: ${localError ?: "Error desconocido"}")
                            }
                        }
                        
                        return RegisterResult(
                            success = savedLocally,
                            errorMessage = detailedMessage,
                            errorType = RegisterErrorType.FIREBASE_NOT_CONFIGURED
                        )
                    }
                    
                    val isAuthProviderDisabled = (exception is FirebaseAuthException && 
                        exception.errorCode == "ERROR_OPERATION_NOT_ALLOWED") ||
                        errorMessage.contains("operation is not allowed", ignoreCase = true) ||
                        errorMessage.contains("sign-in provider is disabled", ignoreCase = true) ||
                        errorCode.contains("ERROR_OPERATION_NOT_ALLOWED", ignoreCase = true)
                    
                    if (isAuthProviderDisabled) {
                        var savedLocally = false
                        var localError: String? = null
                        
                        var existsLocally = false
                        for (i in registeredUsers.indices) {
                            val user = registeredUsers[i]
                            if (user != null && user.email.equals(email, ignoreCase = true)) {
                                existsLocally = true
                                break
                            }
                        }
                        
                        if (!existsLocally) {
                            if (registeredCount < 5) {
                                for (i in registeredUsers.indices) {
                                    if (registeredUsers[i] == null) {
                                        registeredUsers[i] = newUser
                                        registeredCount++
                                        savedLocally = true
                                        break
                                    }
                                }
                            } else {
                                localError = "Se alcanzó el límite de usuarios (5 máximo)"
                            }
                        } else {
                            localError = "El correo electrónico ya está registrado localmente"
                        }
                        
                        val detailedMessage = buildString {
                            append("Error al registrar en Firebase.\n\n")
                            append("Código de error: ERROR_OPERATION_NOT_ALLOWED\n")
                            append("Mensaje: Esta operación no está permitida. El proveedor de autenticación de Email/Contraseña está deshabilitado en Firebase.\n\n")
                            append("Para solucionarlo:\n")
                            append("1. Ve a Firebase Console (console.firebase.google.com)\n")
                            append("2. Selecciona tu proyecto\n")
                            append("3. En el menú lateral, ve a \"Authentication\" (Autenticación)\n")
                            append("4. Haz clic en la pestaña \"Sign-in method\" (Método de inicio de sesión)\n")
                            append("5. Busca \"Correo electrónico/Contraseña\" o \"Email/Password\" en la lista de proveedores\n")
                            append("6. Si no está en la lista, haz clic en \"Agregar proveedor nuevo\" y selecciona \"Correo electrónico/Contraseña\"\n")
                            append("7. Haz clic en \"Correo electrónico/Contraseña\" para abrir su configuración\n")
                            append("8. Activa el toggle \"Habilitar\" (Enable)\n")
                            append("9. Haz clic en \"Guardar\" (Save)\n")
                            append("10. Vuelve a intentar registrar el usuario en la app\n\n")
                            if (savedLocally) {
                                append("El usuario se guardó localmente pero se perderá al reiniciar la app.")
                            } else {
                                append("No se pudo guardar localmente: ${localError ?: "Error desconocido"}")
                            }
                        }
                        
                        return RegisterResult(
                            success = savedLocally,
                            errorMessage = detailedMessage,
                            errorType = RegisterErrorType.FIREBASE_AUTH_DISABLED
                        )
                    }
                    
                    val isUserExistsError = (exception is FirebaseAuthException && 
                        (exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE" || 
                         exception.errorCode == "ERROR_WEAK_PASSWORD")) ||
                        errorMessage.contains("already exists", ignoreCase = true) ||
                        errorMessage.contains("already in use", ignoreCase = true) ||
                        errorMessage.contains("ya existe", ignoreCase = true) ||
                        errorMessage.contains("email address is already", ignoreCase = true) ||
                        errorMessage.contains("The email address is already", ignoreCase = true)
                    
                    if (isUserExistsError) {
                        return RegisterResult(
                            success = false,
                            errorMessage = "El correo electrónico ya está registrado en Firebase",
                            errorType = RegisterErrorType.USER_ALREADY_EXISTS
                        )
                    }
                    
                    val isNetworkError = errorMessage.contains("network", ignoreCase = true) ||
                        errorMessage.contains("connection", ignoreCase = true) ||
                        errorMessage.contains("timeout", ignoreCase = true) ||
                        errorMessage.contains("unreachable", ignoreCase = true) ||
                        (exception is FirebaseAuthException && exception.errorCode == "ERROR_NETWORK_REQUEST_FAILED")
                    
                    if (isNetworkError) {
                        return RegisterResult(
                            success = false,
                            errorMessage = "Error de conexión con Firebase. Verifica tu conexión a internet y la configuración de Firebase.",
                            errorType = RegisterErrorType.FIREBASE_ERROR
                        )
                    }
                    
                    var savedLocally = false
                    var localError: String? = null
                    
                    var existsLocally = false
                    for (i in registeredUsers.indices) {
                        val user = registeredUsers[i]
                        if (user != null && user.email.equals(email, ignoreCase = true)) {
                            existsLocally = true
                            break
                        }
                    }
                    
                    if (!existsLocally) {
                        if (registeredCount < 5) {
                            for (i in registeredUsers.indices) {
                                if (registeredUsers[i] == null) {
                                    registeredUsers[i] = newUser
                                    registeredCount++
                                    savedLocally = true
                                    break
                                }
                            }
                        } else {
                            localError = "Se alcanzó el límite de usuarios (5 máximo)"
                        }
                    } else {
                        localError = "El correo electrónico ya está registrado localmente"
                    }
                    
                    val detailedMessage = buildString {
                        append("Error al registrar en Firebase.\n\n")
                        if (exception is FirebaseAuthException) {
                            append("Código de error: ${exception.errorCode}\n")
                            append("Mensaje: ${exception.message}\n\n")
                        } else if (errorMessage.isNotBlank()) {
                            append("Detalles: $errorMessage\n\n")
                        } else {
                            append("Error desconocido.\n\n")
                        }
                        if (savedLocally) {
                            append("El usuario se guardó localmente pero se perderá al reiniciar la app.")
                        } else {
                            append("No se pudo guardar localmente: ${localError ?: "Error desconocido"}")
                        }
                    }
                    
                    return RegisterResult(
                        success = savedLocally,
                        errorMessage = detailedMessage,
                        errorType = RegisterErrorType.FIREBASE_ERROR
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Timeout al conectar con Firebase", e)
                
                var savedLocally = false
                var localError: String? = null
                
                var existsLocally = false
                for (i in registeredUsers.indices) {
                    val user = registeredUsers[i]
                    if (user != null && user.email.equals(email, ignoreCase = true)) {
                        existsLocally = true
                        break
                    }
                }
                
                if (!existsLocally) {
                    if (registeredCount < 5) {
                        for (i in registeredUsers.indices) {
                            if (registeredUsers[i] == null) {
                                registeredUsers[i] = newUser
                                registeredCount++
                                savedLocally = true
                                break
                            }
                        }
                    } else {
                        localError = "Se alcanzó el límite de usuarios (5 máximo)"
                    }
                } else {
                    localError = "El correo electrónico ya está registrado localmente"
                }
                
                val errorMsg = buildString {
                    append("Timeout al conectar con Firebase. Verifica tu conexión a internet.\n\n")
                    if (savedLocally) {
                        append("El usuario se guardó localmente pero se perderá al reiniciar la app.")
                    } else {
                        append("No se pudo guardar localmente: ${localError ?: "Error desconocido"}")
                    }
                }
                
                return RegisterResult(
                    success = savedLocally,
                    errorMessage = errorMsg,
                    errorType = RegisterErrorType.FIREBASE_TIMEOUT
                )
            } catch (e: IllegalStateException) {
                val firebaseStatus = checkFirebaseConfiguration()
                
                var savedLocally = false
                var localError: String? = null
                
                var existsLocally = false
                for (i in registeredUsers.indices) {
                    val user = registeredUsers[i]
                    if (user != null && user.email.equals(email, ignoreCase = true)) {
                        existsLocally = true
                        break
                    }
                }
                
                if (!existsLocally) {
                    if (registeredCount < 5) {
                        for (i in registeredUsers.indices) {
                            if (registeredUsers[i] == null) {
                                registeredUsers[i] = newUser
                                registeredCount++
                                savedLocally = true
                                break
                            }
                        }
                    } else {
                        localError = "Se alcanzó el límite de usuarios (5 máximo)"
                    }
                } else {
                    localError = "El correo electrónico ya está registrado localmente"
                }
                
                val errorMsg = buildString {
                    append("Firebase no está inicializado correctamente.\n\n")
                    if (firebaseStatus != null) {
                        append("Detalles: $firebaseStatus\n\n")
                    }
                    append("Verifica que:\n")
                    append("1. El archivo google-services.json esté en app/google-services.json\n")
                    append("2. El package name coincida con: com.example.comunik\n")
                    append("3. El plugin de Google Services esté configurado en build.gradle.kts\n")
                    append("4. Sincronices el proyecto (Sync Project with Gradle Files)\n\n")
                    if (savedLocally) {
                        append("El usuario se guardó localmente pero se perderá al reiniciar la app.")
                    } else {
                        append("No se pudo guardar localmente: ${localError ?: "Error desconocido"}")
                    }
                }
                Log.e(TAG, "Error de estado de Firebase: ${e.message}", e)
                return RegisterResult(
                    success = savedLocally,
                    errorMessage = errorMsg,
                    errorType = RegisterErrorType.FIREBASE_NOT_CONFIGURED
                )
            } catch (e: Exception) {
                var savedLocally = false
                var localError: String? = null
                
                var existsLocally = false
                for (i in registeredUsers.indices) {
                    val user = registeredUsers[i]
                    if (user != null && user.email.equals(email, ignoreCase = true)) {
                        existsLocally = true
                        break
                    }
                }
                
                if (!existsLocally) {
                    if (registeredCount < 5) {
                        for (i in registeredUsers.indices) {
                            if (registeredUsers[i] == null) {
                                registeredUsers[i] = newUser
                                registeredCount++
                                savedLocally = true
                                break
                            }
                        }
                    } else {
                        localError = "Se alcanzó el límite de usuarios (5 máximo)"
                    }
                } else {
                    localError = "El correo electrónico ya está registrado localmente"
                }
                
                val errorDetails = buildString {
                    append("Error de Firebase: ${e.javaClass.simpleName}\n")
                    append("Mensaje: ${e.message ?: "Error desconocido"}\n\n")
                    e.cause?.let {
                        append("Causa: ${it.javaClass.simpleName} - ${it.message}\n\n")
                    }
                    append("Verifica que Firebase esté configurado correctamente.\n\n")
                    if (savedLocally) {
                        append("El usuario se guardó localmente pero se perderá al reiniciar la app.")
                    } else {
                        append("No se pudo guardar localmente: ${localError ?: "Error desconocido"}")
                    }
                }
                Log.e(TAG, "Error inesperado de Firebase", e)
                return RegisterResult(
                    success = savedLocally,
                    errorMessage = errorDetails,
                    errorType = RegisterErrorType.FIREBASE_ERROR
                )
            }
        }
        
        if (!useFirebase) {
            return RegisterResult(
                success = false,
                errorMessage = "Firebase no está habilitado. El usuario se guardará localmente pero se perderá al reiniciar la app. Se recomienda configurar Firebase para un almacenamiento persistente.",
                errorType = RegisterErrorType.FIREBASE_NOT_CONFIGURED
            )
        }
        
        for (i in registeredUsers.indices) {
            val user = registeredUsers[i]
            if (user != null && user.email.equals(email, ignoreCase = true)) {
                return RegisterResult(
                    success = false,
                    errorMessage = "El correo electrónico ya está registrado localmente",
                    errorType = RegisterErrorType.USER_ALREADY_EXISTS
                )
            }
        }
        
        if (registeredCount >= 5) {
            return RegisterResult(
                success = false,
                errorMessage = "Se alcanzó el límite de usuarios (5 máximo) en almacenamiento local",
                errorType = RegisterErrorType.USER_LIMIT_REACHED
            )
        }
        
        for (i in registeredUsers.indices) {
            if (registeredUsers[i] == null) {
                registeredUsers[i] = newUser
                registeredCount++
                return RegisterResult(
                    success = true,
                    errorMessage = "Usuario registrado localmente. Nota: Los datos se perderán al reiniciar la app. Se recomienda configurar Firebase."
                )
            }
        }
        
        return RegisterResult(
            success = false,
            errorMessage = "Error desconocido al registrar usuario",
            errorType = RegisterErrorType.UNKNOWN_ERROR
        )
    }
    
    @Throws(InvalidUserDataException::class, UserAlreadyExistsException::class, UserLimitReachedException::class)
    suspend fun registerWithExceptions(email: String, password: String, name: String) {
        val newUser = User(email = email, password = password, name = name)
        if (!newUser.esValido) {
            throw InvalidUserDataException("Los datos del usuario no son válidos")
        }
        
        if (useFirebase) {
            try {
                val result = FirebaseAuthService.register(email, password, name)
                if (result.isSuccess) {
                    var existsLocally = false
                    for (i in registeredUsers.indices) {
                        val user = registeredUsers[i]
                        if (user != null && user.email.equals(email, ignoreCase = true)) {
                            existsLocally = true
                            break
                        }
                    }
                    
                    if (!existsLocally && registeredCount < 5) {
                        for (i in registeredUsers.indices) {
                            if (registeredUsers[i] == null) {
                                registeredUsers[i] = newUser
                                registeredCount++
                                break
                            }
                        }
                    }
                    return
                } else {
                    val exception = result.exceptionOrNull()
                    if (exception?.message?.contains("already exists", ignoreCase = true) == true ||
                        exception?.message?.contains("already in use", ignoreCase = true) == true ||
                        exception?.message?.contains("ya existe", ignoreCase = true) == true) {
                        throw UserAlreadyExistsException("El usuario con email $email ya existe")
                    }
                    throw Exception("Error al registrar en Firebase: ${exception?.message ?: "Error desconocido"}")
                }
            } catch (e: UserAlreadyExistsException) {
                throw e
            } catch (e: Exception) {
            }
        }
        
        for (user in registeredUsers) {
            if (user != null && user.email.equals(email, ignoreCase = true)) {
                throw UserAlreadyExistsException("El usuario con email $email ya existe")
            }
        }
        
        if (registeredCount >= 5) {
            throw UserLimitReachedException("Se ha alcanzado el límite máximo de 5 usuarios")
        }
        
        for (i in registeredUsers.indices) {
            if (registeredUsers[i] == null) {
                registeredUsers[i] = newUser
                registeredCount++
                return
            }
        }
    }
    

    @Throws(InvalidCredentialsException::class)
    suspend fun loginWithExceptions(email: String, password: String): User {
        if (useFirebase) {
            val result = withTimeoutOrNull(10000) {
                try {
                    FirebaseAuthService.login(email, password)
                } catch (e: Exception) {
                    null
                }
            }
            
            result?.getOrNull()?.let { firebaseUser ->
                return User(
                    email = firebaseUser.email ?: email,
                    password = "", // No almacenamos contraseña
                    name = firebaseUser.displayName ?: "Usuario"
                )
            }
        }
        
        for (i in registeredUsers.indices) {
            val user = registeredUsers[i]
            if (user != null && 
                user.email.equals(email, ignoreCase = true) && 
                user.password == password) {
                return user
            }
        }
        throw InvalidCredentialsException("Email o contraseña incorrectos")
    }

    fun userExists(email: String): Boolean {
        if (useFirebase) {
            val currentUser = FirebaseAuthService.getCurrentUser()
            if (currentUser?.email?.equals(email, ignoreCase = true) == true) {
                return true
            }
            return false
        }
        
        for (user in registeredUsers) {
            if (user != null && user.email.equals(email, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun getUserByEmail(email: String): User? {
        return safeRun {
            for (user in registeredUsers) {
                if (user != null && user.email.equals(email, ignoreCase = true)) {
                    return@safeRun user
                }
            }
            null
        }
    }
    
    fun getRegisteredUsersArray(): Array<User?> {
        return registeredUsers
    }
    
    fun getRegisteredCount(): Int {
        return registeredCount
    }
    
    fun filterUsers(nameFilter: String? = null, emailFilter: String? = null): List<User> {
        return registeredUsers
            .filterNotNull()
            .filter filterLambda@{ user ->
                // Lambda con etiqueta para controlar el flujo
                if (nameFilter.isNullOrBlank() && emailFilter.isNullOrBlank()) {
                    return@filterLambda true
                }
                
                val matchesName = nameFilter.isNullOrBlank() || 
                    user.name.contains(nameFilter, ignoreCase = true)
                val matchesEmail = emailFilter.isNullOrBlank() || 
                    user.email.contains(emailFilter, ignoreCase = true)
                
                return@filterLambda matchesName && matchesEmail
            }
    }
}

