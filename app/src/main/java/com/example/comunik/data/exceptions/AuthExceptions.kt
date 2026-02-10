package com.example.comunik.data.exceptions

//credenciales de login son inválidas
class InvalidCredentialsException(message: String = "Credenciales inválidas") : Exception(message)

//usuario ya existe
class UserAlreadyExistsException(message: String = "El usuario ya existe") : Exception(message)

//se alcanza el límite máximo de usuarios registrados
class UserLimitReachedException(message: String = "Se ha alcanzado el límite máximo de usuarios") : Exception(message)

//datos user inválidos
class InvalidUserDataException(message: String = "Los datos del usuario no son válidos") : Exception(message)
