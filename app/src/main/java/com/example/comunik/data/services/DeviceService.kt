package com.example.comunik.data.services

import com.example.comunik.data.FirebaseDatabaseService
import com.example.comunik.data.FirebaseStorageService
import com.example.comunik.data.models.DeviceEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

object DeviceService {
    suspend fun createDevice(
        userId: String,
        name: String,
        latitude: Double,
        longitude: Double,
        address: String = ""
    ): Result<DeviceEntry> = withContext(Dispatchers.IO) {
        try {
            val deviceId = FirebaseDatabaseService.generateId()
            val deviceEntry = DeviceEntry(
                id = deviceId,
                userId = userId,
                name = name,
                latitude = latitude,
                longitude = longitude,
                address = address
            )
            
            val dbResult = FirebaseDatabaseService.createWithId(
                path = "devices/$userId",
                id = deviceId,
                data = mapOf(
                    "id" to deviceId,
                    "userId" to userId,
                    "name" to name,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "address" to address,
                    "createdAt" to deviceEntry.createdAt,
                    "updatedAt" to deviceEntry.updatedAt
                )
            )
            
            if (dbResult.isFailure) {
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al guardar ubicación"))
            }
            
            Result.success(deviceEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDevice(userId: String, deviceId: String): Result<DeviceEntry> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.readMap(
                path = "devices/$userId/$deviceId"
            )
            
            if (dbResult.isFailure || dbResult.getOrNull() == null) {
                val metadataResult = FirebaseStorageService.downloadMetadata(userId, "devices", deviceId)
                if (metadataResult.isFailure) {
                    return@withContext Result.failure(Exception("Dispositivo no encontrado"))
                }
                
                val metadata = metadataResult.getOrNull() ?: ""
                val deviceEntry = DeviceEntry.fromJson(metadata)
                if (deviceEntry != null) {
                    return@withContext Result.success(deviceEntry)
                } else {
                    return@withContext Result.failure(Exception("Error al parsear metadatos"))
                }
            }
            
            val data = dbResult.getOrNull()!!
            val deviceEntry = DeviceEntry(
                id = (data["id"] as? String) ?: deviceId,
                userId = (data["userId"] as? String) ?: userId,
                name = (data["name"] as? String) ?: "",
                latitude = (data["latitude"] as? Double) ?: 0.0,
                longitude = (data["longitude"] as? Double) ?: 0.0,
                address = (data["address"] as? String) ?: "",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
            )
            
            Result.success(deviceEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listDevices(userId: String): Result<List<DeviceEntry>> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.readAllMaps(
                path = "devices/$userId"
            )
            
            if (dbResult.isFailure) {
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al listar dispositivos"))
            }
            
            val dataMap = dbResult.getOrNull() ?: emptyMap()
            val devices = mutableListOf<DeviceEntry>()
            
            dataMap.forEach { (deviceId, data) ->
                try {
                    val dataMap = data as? Map<String, Any> ?: return@forEach
                    
                    val deviceEntry = DeviceEntry(
                        id = (dataMap["id"] as? String) ?: deviceId,
                        userId = (dataMap["userId"] as? String) ?: userId,
                        name = (dataMap["name"] as? String) ?: "",
                        latitude = (dataMap["latitude"] as? Double) ?: 0.0,
                        longitude = (dataMap["longitude"] as? Double) ?: 0.0,
                        address = (dataMap["address"] as? String) ?: "",
                        createdAt = (dataMap["createdAt"] as? Long) ?: System.currentTimeMillis(),
                        updatedAt = (dataMap["updatedAt"] as? Long) ?: System.currentTimeMillis()
                    )
                    devices.add(deviceEntry)
                } catch (e: Exception) {
                }
            }
            
            if (devices.isEmpty()) {
                try {
                    val devicesRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                        .child("devices/$userId")
                    val listResult = devicesRef.listAll().await()
                    
                    listResult.items.forEach { item ->
                        if (item.name.endsWith("_metadata.json")) {
                            val deviceId = item.name.replace("_metadata.json", "")
                            getDevice(userId, deviceId).getOrNull()?.let { devices.add(it) }
                        }
                    }
                } catch (e: Exception) {
                }
            }
            
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDevice(
        userId: String,
        deviceId: String,
        name: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null
    ): Result<DeviceEntry> = withContext(Dispatchers.IO) {
        try {
            val existingResult = getDevice(userId, deviceId)
            if (existingResult.isFailure) {
                return@withContext Result.failure(existingResult.exceptionOrNull() ?: Exception("Dispositivo no encontrado"))
            }
            
            val existing = existingResult.getOrNull()!!
            
            val updates = mutableMapOf<String, Any?>(
                "updatedAt" to System.currentTimeMillis()
            )
            
            if (name != null) {
                updates["name"] = name
            }
            
            if (latitude != null) {
                updates["latitude"] = latitude
            }
            
            if (longitude != null) {
                updates["longitude"] = longitude
            }
            
            if (address != null) {
                updates["address"] = address
            }
            
            val dbResult = FirebaseDatabaseService.updateFields(
                path = "devices/$userId/$deviceId",
                updates = updates
            )
            
            if (dbResult.isFailure) {
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al actualizar en base de datos"))
            }
            
            val updated = existing.copy(
                name = name ?: existing.name,
                latitude = latitude ?: existing.latitude,
                longitude = longitude ?: existing.longitude,
                address = address ?: existing.address,
                updatedAt = System.currentTimeMillis()
            )
            
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDevice(userId: String, deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.delete("devices/$userId/$deviceId")
            
            if (dbResult.isFailure) {
                try {
                    com.google.firebase.storage.FirebaseStorage.getInstance().reference
                        .child("devices/$userId/${deviceId}_metadata.json").delete().await()
                } catch (e: Exception) {
                    return@withContext Result.failure(e)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
