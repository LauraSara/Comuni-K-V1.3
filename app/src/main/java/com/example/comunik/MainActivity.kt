package com.example.comunik

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comunik.data.AuthRepository
import com.example.comunik.data.exceptions.InvalidCredentialsException
import com.example.comunik.ui.screens.ForgotPasswordScreen
import com.example.comunik.ui.screens.HomeScreen
import com.example.comunik.ui.screens.LoginScreen
import com.example.comunik.ui.screens.RegisterScreen
import com.example.comunik.ui.theme.ComuniKTheme

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ForgotPassword : Screen("forgot_password")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComuniKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationGraph(
                        onLoginSuccess = { user ->
                            Toast.makeText(
                                this@MainActivity,
                                "¡Bienvenido ${user.name}!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onLoginError = {
                            Toast.makeText(
                                this@MainActivity,
                                "Credenciales incorrectas. Verifica tu email y contraseña.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onRegisterSuccess = {
                            Toast.makeText(
                                this@MainActivity,
                                "¡Registro exitoso! Ahora puedes iniciar sesión.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onRegisterError = { message ->
                            Toast.makeText(
                                this@MainActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController = rememberNavController(),
    onLoginSuccess: (com.example.comunik.data.User) -> Unit,
    onLoginError: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onRegisterError: (String) -> Unit
) {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf<com.example.comunik.data.User?>(null) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    // try/catch para manejar excepciones
                    try {
                        val user = AuthRepository.loginWithExceptions(email, password)
                        currentUser = user
                        onLoginSuccess(user)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } catch (e: InvalidCredentialsException) {
                        onLoginError()
                    } catch (e: Exception) {
                        onLoginError()
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = { name, email, password, disability ->
                    val success = AuthRepository.register(email, password, name)
                    if (success) {
                        onRegisterSuccess()
                        navController.popBackStack()
                    } else {
                        if (AuthRepository.userExists(email)) {
                            onRegisterError("El correo electrónico ya está registrado")
                        } else {
                            onRegisterError("No se pudo registrar. Se alcanzó el límite de usuarios (5 máximo)")
                        }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                },
                onGoogleClick = {
                    Toast.makeText(
                        context,
                        "Registro con Google pendiente",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onFacebookClick = {
                    Toast.makeText(
                        context,
                        "Registro con Facebook pendiente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onSendResetClick = { email, onResult ->
                    val userExists = AuthRepository.userExists(email)
                    if (userExists) {
                        Toast.makeText(
                            context,
                            "Se han enviado las instrucciones de recuperación a $email",
                            Toast.LENGTH_LONG
                        ).show()
                        onResult(true)
                    } else {
                        Toast.makeText(
                            context,
                            "No existe una cuenta asociada a este correo electrónico",
                            Toast.LENGTH_LONG
                        ).show()
                        onResult(false)
                    }
                },
                onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                userName = currentUser?.name ?: "Usuario",
                onStartCameraClick = {
                    Toast.makeText(
                        context,
                        "Funcionalidad en desarrollo",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onPhrasesClick = {
                    Toast.makeText(
                        context,
                        "Próximamente",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onContactsClick = {
                    Toast.makeText(
                        context,
                        "En desarrollo",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onTextToVoiceClick = {
                    Toast.makeText(
                        context,
                        "Funcionalidad pendiente",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSettingsClick = {
                    Toast.makeText(
                        context,
                        "Próximamente",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onNotificationClick = {
                    Toast.makeText(
                        context,
                        "En desarrollo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}