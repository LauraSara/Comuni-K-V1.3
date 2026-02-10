package com.example.comunik.data

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
                registeredUsers[i] = User(email = email, password = password, name = name)
                registeredCount++
                return true
            }
        }
        
        return false
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
        for (user in registeredUsers) {
            if (user != null && user.email.equals(email, ignoreCase = true)) {
                return user
            }
        }
        return null
    }
    
    fun getRegisteredUsersArray(): Array<User?> {
        return registeredUsers
    }
    
    fun getRegisteredCount(): Int {
        return registeredCount
    }
}

