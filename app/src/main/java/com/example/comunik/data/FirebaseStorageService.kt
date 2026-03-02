package com.example.comunik.data

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

object FirebaseStorageService {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    suspend fun uploadTextFile(
        userId: String,
        textId: String,
        content: String
    ): Result<String> {
        return try {
            val textRef = storageRef.child("texts/$userId/$textId.txt")
            val bytes = content.toByteArray()
            val uploadTask = textRef.putBytes(bytes).await()
            val downloadUrl = textRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadTextFile(userId: String, textId: String): Result<String> {
        return try {
            val textRef = storageRef.child("texts/$userId/$textId.txt")
            val maxDownloadSize = 1024 * 1024L // 1MB
            val bytes = textRef.getBytes(maxDownloadSize).await()
            val content = String(bytes)
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listTextFiles(userId: String): Result<List<String>> {
        return try {
            val textsRef = storageRef.child("texts/$userId")
            val listResult = textsRef.listAll().await()
            val fileNames = listResult.items.map { it.name.replace(".txt", "") }
            Result.success(fileNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTextFile(userId: String, textId: String): Result<Unit> {
        return try {
            val textRef = storageRef.child("texts/$userId/$textId.txt")
            textRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAudioFile(
        userId: String,
        speechId: String,
        audioFile: File
    ): Result<String> {
        return try {
            val audioRef = storageRef.child("speeches/$userId/$speechId.mp3")
            val uploadTask = audioRef.putFile(android.net.Uri.fromFile(audioFile)).await()
            val downloadUrl = audioRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAudioBytes(
        userId: String,
        speechId: String,
        audioBytes: ByteArray
    ): Result<String> {
        return try {
            val audioRef = storageRef.child("speeches/$userId/$speechId.mp3")
            val uploadTask = audioRef.putBytes(audioBytes).await()
            val downloadUrl = audioRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAudioFile(userId: String, speechId: String): Result<ByteArray> {
        return try {
            val audioRef = storageRef.child("speeches/$userId/$speechId.mp3")
            val maxDownloadSize = 10 * 1024 * 1024L // 10MB
            val bytes = audioRef.getBytes(maxDownloadSize).await()
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listAudioFiles(userId: String): Result<List<String>> {
        return try {
            val speechesRef = storageRef.child("speeches/$userId")
            val listResult = speechesRef.listAll().await()
            val fileNames = listResult.items.map { it.name.replace(".mp3", "") }
            Result.success(fileNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAudioFile(userId: String, speechId: String): Result<Unit> {
        return try {
            val audioRef = storageRef.child("speeches/$userId/$speechId.mp3")
            audioRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMetadata(
        userId: String,
        folder: String,
        fileId: String,
        metadata: String
    ): Result<String> {
        return try {
            val metadataRef = storageRef.child("$folder/$userId/${fileId}_metadata.json")
            val bytes = metadata.toByteArray()
            val uploadTask = metadataRef.putBytes(bytes).await()
            val downloadUrl = metadataRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadMetadata(
        userId: String,
        folder: String,
        fileId: String
    ): Result<String> {
        return try {
            val metadataRef = storageRef.child("$folder/$userId/${fileId}_metadata.json")
            val maxDownloadSize = 1024 * 1024L // 1MB
            val bytes = metadataRef.getBytes(maxDownloadSize).await()
            val content = String(bytes)
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateFileId(): String {
        return UUID.randomUUID().toString()
    }
}
