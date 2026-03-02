package com.example.comunik.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseDatabaseService {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef: DatabaseReference = database.reference

    /**
     * Crea una nueva entrada en la base de datos
     */
    suspend fun <T> create(
        path: String,
        data: T
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val ref = databaseRef.child(path)
            ref.setValue(data).await()
            Result.success(ref.key ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea una nueva entrada con un ID específico
     */
    suspend fun <T> createWithId(
        path: String,
        id: String,
        data: T
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val ref = databaseRef.child("$path/$id")
            ref.setValue(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lee un valor de la base de datos
     */
    suspend fun <T> read(
        path: String,
        clazz: Class<T>
    ): Result<T?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = databaseRef.child(path).get().await()
            if (snapshot.exists()) {
                val value = snapshot.getValue(clazz)
                Result.success(value)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lee un valor como Map desde la base de datos
     */
    suspend fun readMap(
        path: String
    ): Result<Map<String, Any>?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = databaseRef.child(path).get().await()
            if (snapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                val value = snapshot.value as? Map<String, Any>
                Result.success(value)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lee todos los valores de un path
     */
    suspend fun <T> readAll(
        path: String,
        clazz: Class<T>
    ): Result<Map<String, T>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = databaseRef.child(path).get().await()
            if (snapshot.exists()) {
                val map = mutableMapOf<String, T>()
                snapshot.children.forEach { child ->
                    val value = child.getValue(clazz)
                    if (value != null) {
                        map[child.key ?: ""] = value
                    }
                }
                Result.success(map)
            } else {
                Result.success(emptyMap())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lee todos los valores de un path como Maps
     */
    suspend fun readAllMaps(
        path: String
    ): Result<Map<String, Map<String, Any>>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = databaseRef.child(path).get().await()
            if (snapshot.exists()) {
                val map = mutableMapOf<String, Map<String, Any>>()
                snapshot.children.forEach { child ->
                    @Suppress("UNCHECKED_CAST")
                    val value = child.value as? Map<String, Any>
                    if (value != null) {
                        map[child.key ?: ""] = value
                    }
                }
                Result.success(map)
            } else {
                Result.success(emptyMap())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un valor en la base de datos
     */
    suspend fun <T> update(
        path: String,
        data: T
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            databaseRef.child(path).setValue(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza campos específicos sin sobrescribir todo el objeto
     */
    suspend fun updateFields(
        path: String,
        updates: Map<String, Any?>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            databaseRef.child(path).updateChildren(updates.filterValues { it != null } as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un valor de la base de datos
     */
    suspend fun delete(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            databaseRef.child(path).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Escucha cambios en tiempo real en un path específico
     */
    fun <T> listen(
        path: String,
        clazz: Class<T>
    ): Flow<T?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val value = snapshot.getValue(clazz)
                    trySend(value)
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = databaseRef.child(path)
        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Escucha cambios en tiempo real en una lista de elementos
     */
    fun <T> listenAll(
        path: String,
        clazz: Class<T>
    ): Flow<Map<String, T>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, T>()
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        val value = child.getValue(clazz)
                        if (value != null) {
                            map[child.key ?: ""] = value
                        }
                    }
                }
                trySend(map)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = databaseRef.child(path)
        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene una referencia a un path específico
     */
    fun getReference(path: String): DatabaseReference {
        return databaseRef.child(path)
    }

    /**
     * Genera un nuevo ID único usando push()
     */
    fun generateId(): String {
        return databaseRef.push().key ?: ""
    }
}
