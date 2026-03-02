package com.example.comunik.data.services

import com.example.comunik.data.FirebaseDatabaseService
import com.example.comunik.data.FirebaseStorageService
import com.example.comunik.data.models.SpeechEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.File

object SpeechService {
    suspend fun createSpeech(
        userId: String,
        text: String,
        audioFile: File? = null,
        audioBytes: ByteArray? = null,
        duration: Long = 0
    ): Result<SpeechEntry> = withContext(Dispatchers.IO) {
        try {
            val speechId = FirebaseDatabaseService.generateId()
            var audioUrl = ""
            
            if (audioFile != null) {
                val uploadResult = FirebaseStorageService.uploadAudioFile(userId, speechId, audioFile)
                if (uploadResult.isSuccess) {
                    audioUrl = uploadResult.getOrNull() ?: ""
                }
            } else if (audioBytes != null) {
                val uploadResult = FirebaseStorageService.uploadAudioBytes(userId, speechId, audioBytes)
                if (uploadResult.isSuccess) {
                    audioUrl = uploadResult.getOrNull() ?: ""
                }
            }
            
            val speechEntry = SpeechEntry(
                id = speechId,
                userId = userId,
                text = text,
                audioUrl = audioUrl,
                duration = duration
            )
            
            val dbResult = FirebaseDatabaseService.createWithId(
                path = "speeches/$userId",
                id = speechId,
                data = mapOf(
                    "id" to speechId,
                    "userId" to userId,
                    "text" to text,
                    "audioUrl" to audioUrl,
                    "duration" to duration,
                    "createdAt" to speechEntry.createdAt,
                    "updatedAt" to speechEntry.updatedAt
                )
            )
            
            if (dbResult.isFailure) {
                if (audioUrl.isNotEmpty()) {
                    FirebaseStorageService.deleteAudioFile(userId, speechId)
                }
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al guardar en base de datos"))
            }
            
            Result.success(speechEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSpeech(userId: String, speechId: String): Result<SpeechEntry> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.readMap(
                path = "speeches/$userId/$speechId"
            )
            
            if (dbResult.isFailure || dbResult.getOrNull() == null) {
                val metadataResult = FirebaseStorageService.downloadMetadata(userId, "speeches", speechId)
                if (metadataResult.isFailure) {
                    return@withContext Result.failure(Exception("Speech no encontrado"))
                }
                
                val metadata = metadataResult.getOrNull() ?: ""
                val speechEntry = SpeechEntry.fromJson(metadata)
                if (speechEntry != null) {
                    return@withContext Result.success(speechEntry)
                } else {
                    return@withContext Result.failure(Exception("Error al parsear metadatos"))
                }
            }
            
            val data = dbResult.getOrNull()!!
            val speechEntry = SpeechEntry(
                id = (data["id"] as? String) ?: speechId,
                userId = (data["userId"] as? String) ?: userId,
                text = (data["text"] as? String) ?: "",
                audioUrl = (data["audioUrl"] as? String) ?: "",
                duration = (data["duration"] as? Long) ?: 0L,
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
            )
            
            Result.success(speechEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listSpeeches(userId: String): Result<List<SpeechEntry>> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.readAllMaps(
                path = "speeches/$userId"
            )
            
            if (dbResult.isFailure) {
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al listar speeches"))
            }
            
            val dataMap = dbResult.getOrNull() ?: emptyMap()
            val speeches = mutableListOf<SpeechEntry>()
            
            dataMap.forEach { (speechId, data) ->
                try {
                    val dataMap = data as? Map<String, Any> ?: return@forEach
                    
                    val speechEntry = SpeechEntry(
                        id = (dataMap["id"] as? String) ?: speechId,
                        userId = (dataMap["userId"] as? String) ?: userId,
                        text = (dataMap["text"] as? String) ?: "",
                        audioUrl = (dataMap["audioUrl"] as? String) ?: "",
                        duration = (dataMap["duration"] as? Long) ?: 0L,
                        createdAt = (dataMap["createdAt"] as? Long) ?: System.currentTimeMillis(),
                        updatedAt = (dataMap["updatedAt"] as? Long) ?: System.currentTimeMillis()
                    )
                    speeches.add(speechEntry)
                } catch (e: Exception) {
                }
            }
            
            if (speeches.isEmpty()) {
                val fileIdsResult = FirebaseStorageService.listAudioFiles(userId)
                if (fileIdsResult.isSuccess) {
                    val fileIds = fileIdsResult.getOrNull() ?: emptyList()
                    fileIds.forEach { speechId ->
                        getSpeech(userId, speechId).getOrNull()?.let { speeches.add(it) }
                    }
                }
            }
            
            Result.success(speeches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSpeech(
        userId: String,
        speechId: String,
        text: String? = null,
        audioFile: File? = null,
        audioBytes: ByteArray? = null,
        duration: Long? = null
    ): Result<SpeechEntry> = withContext(Dispatchers.IO) {
        try {
            val existingResult = getSpeech(userId, speechId)
            if (existingResult.isFailure) {
                return@withContext Result.failure(existingResult.exceptionOrNull() ?: Exception("Speech no encontrado"))
            }
            
            val existing = existingResult.getOrNull()!!
            var audioUrl = existing.audioUrl
            
            if (audioFile != null) {
                val uploadResult = FirebaseStorageService.uploadAudioFile(userId, speechId, audioFile)
                if (uploadResult.isSuccess) {
                    audioUrl = uploadResult.getOrNull() ?: audioUrl
                }
            } else if (audioBytes != null) {
                val uploadResult = FirebaseStorageService.uploadAudioBytes(userId, speechId, audioBytes)
                if (uploadResult.isSuccess) {
                    audioUrl = uploadResult.getOrNull() ?: audioUrl
                }
            }
            
            val updates = mutableMapOf<String, Any?>(
                "updatedAt" to System.currentTimeMillis()
            )
            
            if (text != null) {
                updates["text"] = text
            }
            
            if (audioUrl.isNotEmpty() && audioUrl != existing.audioUrl) {
                updates["audioUrl"] = audioUrl
            }
            
            if (duration != null) {
                updates["duration"] = duration
            }
            
            val dbResult = FirebaseDatabaseService.updateFields(
                path = "speeches/$userId/$speechId",
                updates = updates
            )
            
            if (dbResult.isFailure) {
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al actualizar en base de datos"))
            }
            
            val updated = existing.copy(
                text = text ?: existing.text,
                audioUrl = audioUrl,
                duration = duration ?: existing.duration,
                updatedAt = System.currentTimeMillis()
            )
            
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSpeech(userId: String, speechId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.delete("speeches/$userId/$speechId")
            val storageResult = FirebaseStorageService.deleteAudioFile(userId, speechId)
            
            if (dbResult.isFailure && storageResult.isFailure) {
                return@withContext Result.failure(Exception("Error al eliminar speech"))
            }
            
            try {
                com.google.firebase.storage.FirebaseStorage.getInstance().reference
                    .child("speeches/$userId/${speechId}_metadata.json").delete().await()
            } catch (e: Exception) {
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAudio(userId: String, speechId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val result = FirebaseStorageService.downloadAudioFile(userId, speechId)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
