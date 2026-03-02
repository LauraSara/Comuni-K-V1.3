package com.example.comunik

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de instrumentación para Firebase Test Lab
 * 
 * Esta clase contiene pruebas que pueden ejecutarse en Firebase Test Lab
 * para validar el funcionamiento de la aplicación en diferentes dispositivos
 * y configuraciones.
 * 
 * Firebase Test Lab utiliza la anotación @RunWith(AndroidJUnit4::class)
 * para indicar que se utilizará AndroidJUnit4 como el corredor para ejecutar
 * las pruebas de instrumentación.
 */
@RunWith(AndroidJUnit4::class)
class FirebaseTestLabTest {
    
    /**
     * La anotación @get:Rule indica que la propiedad siguiente (composeTestRule)
     * debe ser obtenida utilizando un método get en lugar de acceder directamente
     * al campo. Esto es necesario para que JUnit pueda inicializar correctamente
     * la regla antes de ejecutar las pruebas.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    /**
     * Prueba que verifica que la aplicación inicia correctamente
     * y muestra la pantalla de login
     */
    @Test
    fun testAppLaunchesAndShowsLoginScreen() {
        // Verificar que la pantalla de login se muestra
        // La pantalla de login contiene el texto "Bienvenido"
        composeTestRule.onNodeWithText("Bienvenido", substring = true)
            .assertExists("La pantalla de login debe mostrarse al iniciar la app")
    }
    
    /**
     * Prueba que verifica que el botón de registro es clickeable
     * y navega a la pantalla de registro
     */
    @Test
    fun testRegisterButtonIsClickable() {
        // Buscar y hacer clic en el botón/link de registro
        // En LoginScreen hay un texto clickeable "Registrarse" o similar
        composeTestRule.onNodeWithText("Registrarse", substring = true)
            .assertExists("El botón de registro debe estar visible")
            .performClick()
        
        // Verificar que se navegó a la pantalla de registro
        // La pantalla de registro contiene el texto "Crea tu cuenta"
        composeTestRule.onNodeWithText("Crea tu cuenta", substring = true)
            .assertExists("Debe navegar a la pantalla de registro")
    }
    
    /**
     * Prueba que verifica que los campos de entrada de texto
     * son interactuables
     */
    @Test
    fun testLoginFieldsAreInteractable() {
        // Verificar que los campos de email y contraseña existen
        composeTestRule.onNodeWithText("Correo electrónico", substring = true)
            .assertExists("El campo de correo electrónico debe estar visible")
        
        composeTestRule.onNodeWithText("Contraseña", substring = true)
            .assertExists("El campo de contraseña debe estar visible")
    }
    
    /**
     * Prueba que verifica la navegación a la pantalla de recuperación
     * de contraseña
     */
    @Test
    fun testForgotPasswordNavigation() {
        // Buscar y hacer clic en el enlace de "¿Olvidaste tu contraseña?"
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?", substring = true)
            .assertExists("El enlace de recuperación de contraseña debe estar visible")
            .performClick()
        
        // Verificar que se navegó a la pantalla de recuperación
        composeTestRule.onNodeWithText("Recuperar contraseña", substring = true)
            .assertExists("Debe navegar a la pantalla de recuperación de contraseña")
    }
    
    /**
     * Prueba básica que verifica el contexto de la aplicación
     */
    @Test
    fun testAppContext() {
        val appContext = composeTestRule.activity.applicationContext
        assert(appContext.packageName == "com.example.comunik") {
            "El package name debe ser com.example.comunik"
        }
    }
}
