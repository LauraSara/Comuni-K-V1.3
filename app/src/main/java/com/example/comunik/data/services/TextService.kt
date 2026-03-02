package com.example.comunik.data.services

import com.example.comunik.data.FirebaseDatabaseService
import com.example.comunik.data.FirebaseStorageService
import com.example.comunik.data.models.TextEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

object TextService {
    suspend fun createText(
        userId: String,
        content: String,
        title: String = ""
    ): Result<TextEntry> = withContext(Dispatchers.IO) {
        try {
            val textId = FirebaseDatabaseService.generateId()
            
            val uploadResult = FirebaseStorageService.uploadTextFile(userId, textId, content)
            if (uploadResult.isFailure) {
                return@withContext Result.failure(uploadResult.exceptionOrNull() ?: Exception("Error al subir texto"))
            }
            
            val storageUrl = uploadResult.getOrNull() ?: ""
            
            val textEntry = TextEntry(
                id = textId,
                userId = userId,
                content = content,
                title = title
            )
            
            val dbResult = FirebaseDatabaseService.createWithId(
                path = "texts/$userId",
                id = textId,
                data = mapOf(
                    "id" to textId,
                    "userId" to userId,
                    "title" to title,
                    "content" to content,
                    "storageUrl" to storageUrl,
                    "createdAt" to textEntry.createdAt,
                    "updatedAt" to textEntry.updatedAt
                )
            )
            
            if (dbResult.isFailure) {
                FirebaseStorageService.deleteTextFile(userId, textId)
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al guardar en base de datos"))
            }
            
            Result.success(textEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getText(userId: String, textId: String): Result<TextEntry> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.readMap(
                path = "texts/$userId/$textId"
            )
            
            if (dbResult.isFailure || dbResult.getOrNull() == null) {
                val contentResult = FirebaseStorageService.downloadTextFile(userId, textId)
                if (contentResult.isFailure) {
                    return@withContext Result.failure(Exception("Texto no encontrado"))
                }
                
                val textEntry = TextEntry(
                    id = textId,
                    userId = userId,
                    content = contentResult.getOrNull() ?: ""
                )
                return@withContext Result.success(textEntry)
            }
            
            val data = dbResult.getOrNull()!!
            val content = (data["content"] as? String) ?: run {
                val contentResult = FirebaseStorageService.downloadTextFile(userId, textId)
                contentResult.getOrNull() ?: ""
            }
            
            val textEntry = TextEntry(
                id = (data["id"] as? String) ?: textId,
                userId = (data["userId"] as? String) ?: userId,
                content = content,
                title = (data["title"] as? String) ?: "",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
            )
            
            Result.success(textEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listTexts(userId: String): Result<List<TextEntry>> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.readAllMaps(
                path = "texts/$userId"
            )
            
            if (dbResult.isFailure) {
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al listar textos"))
            }
            
            val dataMap = dbResult.getOrNull() ?: emptyMap()
            val texts = mutableListOf<TextEntry>()
            
            dataMap.forEach { (textId, data) ->
                try {
                    val dataMap = data as? Map<String, Any> ?: return@forEach
                    val content = (dataMap["content"] as? String) ?: ""
                    
                    val textEntry = TextEntry(
                        id = (dataMap["id"] as? String) ?: textId,
                        userId = (dataMap["userId"] as? String) ?: userId,
                        content = content,
                        title = (dataMap["title"] as? String) ?: "",
                        createdAt = (dataMap["createdAt"] as? Long) ?: System.currentTimeMillis(),
                        updatedAt = (dataMap["updatedAt"] as? Long) ?: System.currentTimeMillis()
                    )
                    texts.add(textEntry)
                } catch (e: Exception) {
                }
            }
            
            if (texts.isEmpty()) {
                val fileIdsResult = FirebaseStorageService.listTextFiles(userId)
                if (fileIdsResult.isSuccess) {
                    val fileIds = fileIdsResult.getOrNull() ?: emptyList()
                    fileIds.forEach { textId ->
                        getText(userId, textId).getOrNull()?.let { texts.add(it) }
                    }
                }
            }
            
            Result.success(texts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateText(
        userId: String,
        textId: String,
        content: String? = null,
        title: String? = null
    ): Result<TextEntry> = withContext(Dispatchers.IO) {
        try {
            val existingResult = getText(userId, textId)
            if (existingResult.isFailure) {
                return@withContext Result.failure(existingResult.exceptionOrNull() ?: Exception("Texto no encontrado"))
            }
            
            val existing = existingResult.getOrNull()!!
            val newContent = content ?: existing.content
            val newTitle = title ?: existing.title
            
            var storageUrl = ""
            if (content != null) {
                val uploadResult = FirebaseStorageService.uploadTextFile(userId, textId, newContent)
                if (uploadResult.isSuccess) {
                    storageUrl = uploadResult.getOrNull() ?: ""
                }
            }
            
            val updates = mutableMapOf<String, Any?>(
                "updatedAt" to System.currentTimeMillis()
            )
            
            if (content != null) {
                updates["content"] = newContent
                if (storageUrl.isNotEmpty()) {
                    updates["storageUrl"] = storageUrl
                }
            }
            
            if (title != null) {
                updates["title"] = newTitle
            }
            
            val dbResult = FirebaseDatabaseService.updateFields(
                path = "texts/$userId/$textId",
                updates = updates
            )
            
            if (dbResult.isFailure) {
                return@withContext Result.failure(dbResult.exceptionOrNull() ?: Exception("Error al actualizar en base de datos"))
            }
            
            val updated = existing.copy(
                content = newContent,
                title = newTitle,
                updatedAt = System.currentTimeMillis()
            )
            
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteText(userId: String, textId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbResult = FirebaseDatabaseService.delete("texts/$userId/$textId")
            val storageResult = FirebaseStorageService.deleteTextFile(userId, textId)
            
            if (dbResult.isFailure && storageResult.isFailure) {
                return@withContext Result.failure(Exception("Error al eliminar texto"))
            }
            
            try {
                com.google.firebase.storage.FirebaseStorage.getInstance().reference
                    .child("texts/$userId/${textId}_metadata.json").delete().await()
            } catch (e: Exception) {
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
