package com.example.comunik

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
import com.example.comunik.ui.screens.WriteScreen
import com.example.comunik.ui.screens.SpeakScreen
import com.example.comunik.ui.screens.FindDeviceScreen
import com.example.comunik.ui.screens.PhrasesScreen
import com.example.comunik.ui.theme.ComuniKTheme

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ForgotPassword : Screen("forgot_password")
    object Write : Screen("write")
    object Speak : Screen("speak")
    object FindDevice : Screen("find_device")
    object Phrases : Screen("phrases")
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
            val loginScope = rememberCoroutineScope()
            LoginScreen(
                onLoginClick = { email, password ->
                    loginScope.launch {
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
            val coroutineScope = rememberCoroutineScope()
            var showFirebaseErrorDialog by remember { mutableStateOf(false) }
            var firebaseErrorMessage by remember { mutableStateOf<String?>(null) }
            
            RegisterScreen(
                onRegisterClick = { name, email, password, disability ->
                    coroutineScope.launch {
                        val result = AuthRepository.register(email, password, name)
                        if (result.success) {
                            if (result.errorMessage != null && result.errorMessage.contains("localmente")) {
                                Toast.makeText(
                                    context,
                                    "Usuario registrado localmente. Los datos se perderán al reiniciar la app.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                onRegisterSuccess()
                            }
                            navController.popBackStack()
                            } else {
                                if (result.errorType == AuthRepository.RegisterErrorType.FIREBASE_NOT_CONFIGURED ||
                                result.errorType == AuthRepository.RegisterErrorType.FIREBASE_AUTH_DISABLED) {
                                firebaseErrorMessage = result.errorMessage
                                showFirebaseErrorDialog = true
                            } else {
                                val errorMsg = result.errorMessage ?: when (result.errorType) {
                                    AuthRepository.RegisterErrorType.USER_ALREADY_EXISTS -> "El correo electrónico ya está registrado"
                                    AuthRepository.RegisterErrorType.USER_LIMIT_REACHED -> "Se alcanzó el límite de usuarios (5 máximo)"
                                    AuthRepository.RegisterErrorType.FIREBASE_TIMEOUT -> "Timeout al conectar con Firebase. Verifica tu conexión a internet."
                                    AuthRepository.RegisterErrorType.FIREBASE_ERROR -> "Error de Firebase. Verifica la configuración."
                                    AuthRepository.RegisterErrorType.INVALID_DATA -> "Los datos ingresados no son válidos"
                                    else -> "Error desconocido al registrar usuario"
                                }
                                onRegisterError(errorMsg)
                            }
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
            
            if (showFirebaseErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showFirebaseErrorDialog = false },
                    title = {
                        Text(
                            text = "Error al registrar en Firebase.",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = firebaseErrorMessage ?: "Error desconocido de Firebase",
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showFirebaseErrorDialog = false }) {
                            Text("Entendido")
                        }
                    }
                )
            }
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
                    navController.navigate(Screen.Phrases.route)
                },
                onContactsClick = {
                    Toast.makeText(
                        context,
                        "En desarrollo",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onTextToVoiceClick = {
                    navController.navigate(Screen.Write.route)
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
                },
                onWriteClick = {
                    navController.navigate(Screen.Write.route)
                },
                onSpeakClick = {
                    navController.navigate(Screen.Speak.route)
                },
                onFindDeviceClick = {
                    navController.navigate(Screen.FindDevice.route)
                }
            )
        }
        
        composable(Screen.Write.route) {
            WriteScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Speak.route) {
            SpeakScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.FindDevice.route) {
            FindDeviceScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Phrases.route) {
            PhrasesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}