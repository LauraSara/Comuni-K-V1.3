package com.example.comunik.ui.screens

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunik.data.models.DeviceEntry
import com.example.comunik.data.services.DeviceService
import com.example.comunik.data.services.UserService
import com.example.comunik.ui.components.rememberLocationProvider
import com.example.comunik.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FindDeviceScreen(
    onBackClick: () -> Unit = {}
) {
    var deviceName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    var devices by remember { mutableStateOf<List<DeviceEntry>>(emptyList()) }
    var isLoadingDevices by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var userId by remember { mutableStateOf("") }
    
    DisposableEffect(Unit) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        
        userId = auth.currentUser?.uid ?: ""
        
        val authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            userId = firebaseAuth.currentUser?.uid ?: ""
        }
        
        auth.addAuthStateListener(authStateListener)
        
        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }
    
    LaunchedEffect(Unit) {
        repeat(5) {
            kotlinx.coroutines.delay(200L * (it + 1))
            val currentId = UserService.getCurrentUserId() ?: ""
            if (currentId.isNotBlank()) {
                userId = currentId
            }
        }
    }
    
    fun loadDevices() {
        if (userId.isNotBlank()) {
            isLoadingDevices = true
            scope.launch {
                devices = loadDevicesFromLocal(context, userId)
                isLoadingDevices = false
            }
        }
    }
    
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            loadDevices()
        }
    }
    
    val locationProvider = rememberLocationProvider(
        onLocationUpdate = { lat, lng ->
            latitude = lat
            longitude = lng
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addressObj = addresses[0]
                    address = addressObj.getAddressLine(0) ?: ""
                }
            } catch (e: Exception) {
                address = "No se pudo obtener la dirección"
            }
        },
        onError = { error ->
            showMessage = error
        }
    )

    LaunchedEffect(Unit) {
        if (!locationProvider.hasPermission) {
            locationProvider.requestPermission()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = HeaderTextLight
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Buscar Dispositivo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = HeaderTextLight
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ubicación actual",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!locationProvider.hasPermission) {
                    Text(
                        text = "Se requieren permisos de ubicación",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { locationProvider.requestPermission() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Solicitar permisos")
                    }
                } else if (locationProvider.isEnabled && latitude != 0.0 && longitude != 0.0) {
                    Text(
                        text = "Mi ubicación actual:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Latitud: ${String.format("%.6f", latitude)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Longitud: ${String.format("%.6f", longitude)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    )
                    if (address.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Dirección:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        )
                    }
                } else if (locationProvider.hasPermission) {
                    Text(
                        text = "Obteniendo ubicación...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Nombre del dispositivo",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = TextSecondary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Ej: Mi dispositivo, Casa, etc.",
                        color = TextSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                when {
                    userId.isBlank() -> {
                        showMessage = "Error: No se pudo obtener el ID de usuario. Por favor, inicia sesión nuevamente."
                    }
                    deviceName.isBlank() -> {
                        showMessage = "Por favor, ingresa un nombre para el dispositivo"
                    }
                    latitude == 0.0 || longitude == 0.0 -> {
                        showMessage = "Por favor, espera a que se obtenga tu ubicación"
                    }
                    else -> {
                        isLoading = true
                        scope.launch {
                            try {
                                val result = DeviceService.createDevice(
                                    userId = userId,
                                    name = deviceName.trim(),
                                    latitude = latitude,
                                    longitude = longitude,
                                    address = address.ifBlank { "Dirección no disponible" }
                                )
                                isLoading = false
                                if (result.isSuccess) {
                                    result.getOrNull()?.let { savedDevice ->
                                        saveDeviceToLocal(context, savedDevice)
                                        devices = loadDevicesFromLocal(context, userId)
                                    }
                                    showMessage = "Ubicación guardada exitosamente"
                                    deviceName = ""
                                } else {
                                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                                    showMessage = "Error al guardar: $errorMsg"
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                showMessage = "Error inesperado: ${e.message}"
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                disabledContainerColor = BorderGray,
                disabledContentColor = TextSecondary
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = run {
                val currentUserId = UserService.getCurrentUserId() ?: ""
                deviceName.trim().isNotBlank() && 
                latitude != 0.0 && 
                longitude != 0.0 && 
                currentUserId.isNotBlank() && 
                !isLoading
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar Ubicación",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }
        }
        
        if (userId.isBlank() && !isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3CD) // Amarillo claro para advertencia
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No se detectó usuario autenticado. Por favor, inicia sesión nuevamente.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF856404)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dispositivos guardados",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                )
                if (isLoadingDevices) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { loadDevices() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = PrimaryBlue
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (devices.isEmpty() && !isLoadingDevices) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay dispositivos guardados",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        )
                    }
                }
            } else {
                devices.forEach { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (device.address.isNotBlank()) {
                                Text(
                                    text = device.address,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            Text(
                                text = "Lat: ${String.format("%.6f", device.latitude)}, Lng: ${String.format("%.6f", device.longitude)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            Text(
                                text = "Guardado: ${dateFormat.format(Date(device.createdAt))}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        }

        showMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                showMessage = null
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { showMessage = null }) {
                        Text("OK")
                    }
                }
            ) {
                Text(message)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindDeviceScreenPreview() {
    com.example.comunik.ui.theme.ComuniKTheme {
        FindDeviceScreen()
    }
}

private const val DEVICES_PREFS_NAME = "comunik_devices_prefs"

private fun loadDevicesFromLocal(context: Context, userId: String): List<DeviceEntry> {
    if (userId.isBlank()) return emptyList()

    return try {
        val prefs = context.getSharedPreferences(DEVICES_PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("devices_$userId", null) ?: return emptyList()
        val jsonArray = JSONArray(json)

        List(jsonArray.length()) { index ->
            val obj = jsonArray.getJSONObject(index)
            DeviceEntry(
                id = obj.optString("id"),
                userId = obj.optString("userId"),
                name = obj.optString("name"),
                latitude = obj.optDouble("latitude", 0.0),
                longitude = obj.optDouble("longitude", 0.0),
                address = obj.optString("address"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun saveDeviceToLocal(context: Context, device: DeviceEntry) {
    if (device.userId.isBlank()) return

    val prefs = context.getSharedPreferences(DEVICES_PREFS_NAME, Context.MODE_PRIVATE)
    val key = "devices_${device.userId}"

    val existing = prefs.getString(key, null)
    val jsonArray = try {
        if (existing != null) JSONArray(existing) else JSONArray()
    } catch (_: Exception) {
        JSONArray()
    }

    val obj = JSONObject().apply {
        put("id", device.id)
        put("userId", device.userId)
        put("name", device.name)
        put("latitude", device.latitude)
        put("longitude", device.longitude)
        put("address", device.address)
        put("createdAt", device.createdAt)
        put("updatedAt", device.updatedAt)
    }

    jsonArray.put(obj)

    prefs.edit().putString(key, jsonArray.toString()).apply()
}
