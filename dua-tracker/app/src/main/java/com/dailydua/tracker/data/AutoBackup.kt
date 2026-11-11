package com.dailydua.tracker.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Keeps the user's setup (reminders, favourites, playlist, progress, settings) saved without any manual step.
 *
 *  1. Android's own cloud backup (allowBackup) restores it when the app is installed again.
 *  2. A copy is also written to Documents/DailyDua on the phone after every change, and read back
 *     the first time the app opens with nothing set up, so a reinstall picks up where it left off.
 */
object AutoBackup {
    private const val TAG = "AutoBackup"
    private const val NAME = "daily-dua-backup.json"
    private const val FOLDER = "Documents/DailyDua/"

    /** Call once from the main screen. Restores on a fresh install, then keeps saving. */
    @OptIn(FlowPreview::class)
    fun start(context: Context, scope: CoroutineScope) {
        val app = context.applicationContext
        val repo = DuaRepository(app)
        scope.launch(Dispatchers.IO) {
            runCatching {
                val fresh = repo.isFreshInstall()
                Log.w(TAG, "start: fresh=$fresh")
                if (!fresh) return@runCatching
                val text = read(app)
                Log.w(TAG, "start: backup file found=${text != null} size=${text?.length}")
                if (text != null) Log.w(TAG, "start: restored=${repo.importBackup(text)}")
            }.onFailure { Log.w(TAG, "start failed", it) }
            repo.markStarted()
            // Save the current state now, then again after every change (batched).
            runCatching { write(app, repo.exportBackup()); Log.w(TAG, "saved") }.onFailure { Log.w(TAG, "save failed", it) }
            repo.changes().drop(1).debounce(2000).collect {
                runCatching { write(app, repo.exportBackup()); Log.w(TAG, "saved") }.onFailure { Log.w(TAG, "save failed", it) }
            }
        }
    }

    private fun write(context: Context, text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val existing = find(context)
        val uri = existing ?: resolver.insert(
            collection,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, FOLDER)
            },
        ) ?: return
        resolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray()) }
    }

    private fun read(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val uri = find(context) ?: return null
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
    }

    private fun find(context: Context): android.net.Uri? {
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        context.contentResolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
            arrayOf(NAME, FOLDER),
            null,
        )?.use { c -> if (c.moveToFirst()) return ContentUris.withAppendedId(collection, c.getLong(0)) }
        return null
    }
}
