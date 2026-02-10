package com.example.comunik.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunik.ui.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BackgroundWhite)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                HandIcon(
                    modifier = Modifier.size(32.dp),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )
            )

            Text(
                text = "Inicia sesión para comenzar a comunicarte",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = TextSecondary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Correo electrónico",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "usuario@ejemplo.com",
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = IconGray
                        )
                    },
                    isError = emailError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderGray,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Contraseña",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "••••••••",
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = IconGray
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = IconGray
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderGray,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = PrimaryBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onForgotPasswordClick() },
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    var hasError = false
                    if (email.isBlank()) {
                        emailError = "El correo electrónico es requerido"
                        hasError = true
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Correo electrónico inválido"
                        hasError = true
                    }
                    
                    if (password.isBlank()) {
                        passwordError = "La contraseña es requerida"
                        hasError = true
                    } else if (password.length < 6) {
                        passwordError = "La contraseña debe tener al menos 6 caracteres"
                        hasError = true
                    }

                    if (!hasError) {
                        onLoginClick(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = BorderGray,
                    thickness = 1.dp
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BorderGray)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = BorderGray,
                    thickness = 1.dp
                )
            }

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Text(
                    text = "Registrarse",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun HandIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val scaleX = width / 24f
        val scaleY = height / 24f
        val strokeWidth = 2f * minOf(scaleX, scaleY)
        
        val path1 = Path().apply {
            moveTo(18f * scaleX, 12.5f * scaleY)
            lineTo(18f * scaleX, 10f * scaleY)
            cubicTo(
                18f * scaleX, 8.89617f * scaleY,
                17.1038f * scaleX, 8f * scaleY,
                16f * scaleX, 8f * scaleY
            )
            cubicTo(
                14.8962f * scaleX, 8f * scaleY,
                14f * scaleX, 8.89617f * scaleY,
                14f * scaleX, 10f * scaleY
            )
            lineTo(14f * scaleX, 11.4f * scaleY)
            
            moveTo(14f * scaleX, 11f * scaleY)
            lineTo(14f * scaleX, 9f * scaleY)
            cubicTo(
                14f * scaleX, 7.89617f * scaleY,
                13.1038f * scaleX, 7f * scaleY,
                12f * scaleX, 7f * scaleY
            )
            cubicTo(
                10.8962f * scaleX, 7f * scaleY,
                10f * scaleX, 7.89617f * scaleY,
                10f * scaleX, 9f * scaleY
            )
            lineTo(10f * scaleX, 11f * scaleY)
            
            moveTo(10f * scaleX, 10.5f * scaleY)
            lineTo(10f * scaleX, 5f * scaleY)
            cubicTo(
                10f * scaleX, 3.89617f * scaleY,
                9.10383f * scaleX, 3f * scaleY,
                8f * scaleX, 3f * scaleY
            )
            cubicTo(
                6.89617f * scaleX, 3f * scaleY,
                6f * scaleX, 3.89617f * scaleY,
                6f * scaleX, 5f * scaleY
            )
            lineTo(6f * scaleX, 14f * scaleY)
        }
        
        val path2 = Path().apply {
            moveTo(6.99998f * scaleX, 15f * scaleY)
            lineTo(5.23998f * scaleX, 13.24f * scaleY)
            cubicTo(
                4.45107f * scaleX, 12.5226f * scaleY,
                3.23805f * scaleX, 12.5503f * scaleY,
                2.4827f * scaleX, 13.303f * scaleY
            )
            cubicTo(
                1.72736f * scaleX, 14.0556f * scaleY,
                1.69537f * scaleX, 15.2686f * scaleY,
                2.40998f * scaleX, 16.06f * scaleY
            )
            lineTo(6.00998f * scaleX, 19.66f * scaleY)
            cubicTo(
                7.49998f * scaleX, 21.14f * scaleY,
                9.19998f * scaleX, 22f * scaleY,
                12f * scaleX, 22f * scaleY
            )
            lineTo(14f * scaleX, 22f * scaleY)
            cubicTo(
                18.4183f * scaleX, 22f * scaleY,
                22f * scaleX, 18.4183f * scaleY,
                22f * scaleX, 14f * scaleY
            )
            lineTo(22f * scaleX, 7f * scaleY)
            cubicTo(
                22f * scaleX, 5.89617f * scaleY,
                21.1038f * scaleX, 5f * scaleY,
                20f * scaleX, 5f * scaleY
            )
            cubicTo(
                18.8962f * scaleX, 5f * scaleY,
                18f * scaleX, 5.89617f * scaleY,
                18f * scaleX, 7f * scaleY
            )
            lineTo(18f * scaleX, 12f * scaleY)
        }
        
        drawPath(
            path = path1,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = path2,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ComuniKTheme {
        LoginScreen()
    }
}

