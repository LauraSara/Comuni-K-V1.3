package com.example.comunik.data.services

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Phrase(
    val id: String,
    val text: String,
    val createdAt: Long
)

object PhrasesService {
    private const val PHRASES_PREFS_NAME = "comunik_phrases_prefs"

    fun savePhrase(context: Context, text: String): Boolean {
        if (text.isBlank()) return false

        return try {
            val prefs = context.getSharedPreferences(PHRASES_PREFS_NAME, Context.MODE_PRIVATE)
            val key = "phrases"

            val existing = prefs.getString(key, null)
            val jsonArray = try {
                if (existing != null) JSONArray(existing) else JSONArray()
            } catch (_: Exception) {
                JSONArray()
            }

            val phrase = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("text", text)
                put("createdAt", System.currentTimeMillis())
            }

            jsonArray.put(phrase)

            prefs.edit().putString(key, jsonArray.toString()).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAllPhrases(context: Context): List<Phrase> {
        return try {
            val prefs = context.getSharedPreferences(PHRASES_PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString("phrases", null) ?: return emptyList()
            val jsonArray = JSONArray(json)

            List(jsonArray.length()) { index ->
                val obj = jsonArray.getJSONObject(index)
                Phrase(
                    id = obj.optString("id"),
                    text = obj.optString("text"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }.reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deletePhrase(context: Context, phraseId: String): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PHRASES_PREFS_NAME, Context.MODE_PRIVATE)
            val key = "phrases"
            val json = prefs.getString(key, null) ?: return false
            val jsonArray = JSONArray(json)

            val newArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.optString("id") != phraseId) {
                    newArray.put(obj)
                }
            }

            prefs.edit().putString(key, newArray.toString()).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
}
