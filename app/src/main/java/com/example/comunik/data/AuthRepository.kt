package com.example.comunik.data

import com.example.comunik.util.safeRun
import com.example.comunik.util.esValido
import com.example.comunik.data.exceptions.*

object AuthRepository {
    private val registeredUsers: Array<User?> = arrayOfNulls(5)
    
    private var registeredCount: Int = 0

    fun login(email: String, password: String): User? {
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

    fun register(email: String, password: String, name: String): Boolean {
        // Validar que el usuario tenga datos válidos usando propiedad de extensión
        val newUser = User(email = email, password = password, name = name)
        if (!newUser.esValido) {
            return false
        }
        
        for (i in registeredUsers.indices) {
            val user = registeredUsers[i]
            if (user != null && user.email.equals(email, ignoreCase = true)) {
                return false
            }
        }
        
        if (registeredCount >= 5) {
            return false
        }
        
        for (i in registeredUsers.indices) {
            if (registeredUsers[i] == null) {
                registeredUsers[i] = newUser
                registeredCount++
                return true
            }
        }
        
        return false
    }
    
    @Throws(InvalidUserDataException::class, UserAlreadyExistsException::class, UserLimitReachedException::class)
    fun registerWithExceptions(email: String, password: String, name: String) {
        val newUser = User(email = email, password = password, name = name)
        if (!newUser.esValido) {
            throw InvalidUserDataException("Los datos del usuario no son válidos")
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
    fun loginWithExceptions(email: String, password: String): User {
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
        for (user in registeredUsers) {
            if (user != null && user.email.equals(email, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun getUserByEmail(email: String): User? {
        // Uso de función inline para ejecutar la búsqueda de forma segura
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
    
    // Filtra usuarios registrados según nombre y mail.
    
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

