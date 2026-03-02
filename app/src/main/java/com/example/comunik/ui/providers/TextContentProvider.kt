package com.example.comunik.ui.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Content Provider para compartir textos de la aplicación
 */
class TextContentProvider : ContentProvider() {
    
    companion object {
        const val AUTHORITY = "com.example.comunik.provider.text"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/texts")
    }
    
    override fun onCreate(): Boolean {
        return true
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }
    
    override fun getType(uri: Uri): String? {
        return "vnd.android.cursor.dir/vnd.com.example.comunik.text"
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}
