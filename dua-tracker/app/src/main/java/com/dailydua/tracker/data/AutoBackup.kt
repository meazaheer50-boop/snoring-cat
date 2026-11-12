package com.dailydua.tracker.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * Keeps the user's setup (reminders, favourites, playlist, progress, every setting) saved without a manual step.
 *
 * Restoring after a reinstall happens with no prompt through Android's own cloud backup
 * (the saved data is included in the backup rules, and a refresh is requested after every
 * change). Android 11+ stops a reinstalled app from reading the file its previous install
 * wrote, so that file is a manual fallback only: Settings has "Restore from a backup file",
 * which uses [pickerIntent] and [restoreFrom].
 *
 *  - Every change is also copied to Documents/DailyDua/daily-dua-backup.json.
 */
object AutoBackup {
    private const val TAG = "AutoBackup"
    private const val NAME = "daily-dua-backup.json"
    private const val FOLDER = "Documents/DailyDua/"
    private const val PREFS = "backup_target"

    /** True on a reinstall, until the user restores or starts fresh. The app shows a prompt while it is true. */
    var needsRestore by mutableStateOf(false)
        private set

    private val resolved = CompletableDeferred<Unit>()
    private lateinit var app: Context
    private lateinit var repo: DuaRepository

    @OptIn(FlowPreview::class)
    fun start(context: Context, scope: CoroutineScope) {
        app = context.applicationContext
        repo = DuaRepository(app)
        scope.launch(Dispatchers.IO) {
            runCatching {
                val fresh = repo.isFreshInstall()
                Log.w(TAG, "start: fresh=$fresh")
                if (fresh) {
                    val own = readOwn()
                    if (own != null) {
                        Log.w(TAG, "start: restored from own file=${repo.importBackup(own)}")
                    } else {
                        // Nothing to restore silently. Android's own cloud backup, if it has a copy, has
                        // already restored the data before this point, so this is a genuinely new start.
                        Log.w(TAG, "start: nothing to restore")
                    }
                }
            }.onFailure { Log.w(TAG, "start failed", it) }
            repo.markStarted()
            save()
            repo.changes().drop(1).debounce(2000).collect { save() }
        }
    }

    /** The intent that opens the system file picker at the backup folder, with permission to read and keep writing the file. */
    fun pickerIntent(): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "application/octet-stream", "text/plain"))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        runCatching {
            putExtra(
                "android.provider.extra.INITIAL_URI",
                Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADocuments%2FDailyDua"),
            )
        }
    }

    /** Restores from the file the user picked. Returns false if it is not a Daily Dua backup. */
    suspend fun restoreFrom(uri: Uri): Boolean {
        val resolver = app.contentResolver
        runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }.onFailure { Log.w(TAG, "no write grant", it) }
        val text = runCatching { resolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull() ?: return false
        if (!repo.importBackup(text)) return false
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("uri", uri.toString()).apply()
        finish()
        return true
    }

    /** The user chose to begin again without restoring. */
    fun startFresh() = finish()

    private fun finish() {
        needsRestore = false
        resolved.complete(Unit)
    }

    private suspend fun save() {
        // Ask Android to refresh its cloud backup soon; that copy restores itself after a reinstall.
        runCatching { android.app.backup.BackupManager(app).dataChanged() }
        runCatching {
            val text = repo.exportBackup()
            val saved = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("uri", null)
            if (saved != null) {
                val ok = runCatching { writeTo(Uri.parse(saved), text) }.isSuccess
                if (ok) { Log.w(TAG, "saved to chosen file"); return }
            }
            writeOwn(text)
            Log.w(TAG, "saved")
        }.onFailure { Log.w(TAG, "save failed", it) }
    }

    private fun writeTo(uri: Uri, text: String) {
        app.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(text.toByteArray()) }
    }

    private fun writeOwn(text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val resolver = app.contentResolver
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = findOwn() ?: resolver.insert(
            collection,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, FOLDER)
            },
        ) ?: return
        writeTo(uri, text)
    }

    private fun readOwn(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val uri = findOwn() ?: return null
        return app.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
    }

    private fun findOwn(): Uri? {
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        app.contentResolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
            arrayOf("daily-dua-backup%", FOLDER),
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
        )?.use { c -> if (c.moveToFirst()) return ContentUris.withAppendedId(collection, c.getLong(0)) }
        return null
    }
}
