package com.diyorbek.applock.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single source of truth for:
 *  - the shared unlock/admin code (stored as a salted hash, never in plaintext)
 *  - the set of locked package names
 *  - the event history log
 *
 * Backed by EncryptedSharedPreferences so the file on disk is not human-readable
 * even with root/file access, without requiring any special device-admin permission.
 */
class AppLockStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "app_lock_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ---------- Code management ----------

    fun isCodeSet(): Boolean = prefs.contains(KEY_CODE_HASH)

    fun setCode(newCode: String) {
        prefs.edit()
            .putString(KEY_CODE_HASH, hash(newCode))
            .apply()
        logEvent(EventType.CODE_CHANGED, null)
    }

    fun verifyCode(code: String): Boolean {
        val stored = prefs.getString(KEY_CODE_HASH, null) ?: return false
        return stored == hash(code)
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        // Static app-specific salt: not a secret on its own, just prevents trivial
        // rainbow-table lookups against the raw SHA-256 of short numeric codes.
        val salted = "focuslock_salt_v1:$input"
        val bytes = digest.digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ---------- Locked app list ----------

    fun getLockedPackages(): Set<String> {
        return prefs.getStringSet(KEY_LOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun isLocked(packageName: String): Boolean = getLockedPackages().contains(packageName)

    fun addLockedPackage(packageName: String, label: String) {
        val current = getLockedPackages().toMutableSet()
        if (current.add(packageName)) {
            prefs.edit().putStringSet(KEY_LOCKED_PACKAGES, current).apply()
            logEvent(EventType.APP_ADDED, label)
        }
    }

    fun removeLockedPackage(packageName: String, label: String) {
        val current = getLockedPackages().toMutableSet()
        if (current.remove(packageName)) {
            prefs.edit().putStringSet(KEY_LOCKED_PACKAGES, current).apply()
            logEvent(EventType.APP_REMOVED, label)
        }
    }

    // ---------- Session unlocks (in-memory would reset on process death, so persist
    // lightly here; cleared explicitly when an app leaves the foreground) ----------

    fun markUnlockedForSession(packageName: String) {
        val current = prefs.getStringSet(KEY_SESSION_UNLOCKED, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        current.add(packageName)
        prefs.edit().putStringSet(KEY_SESSION_UNLOCKED, current).apply()
    }

    fun isUnlockedForSession(packageName: String): Boolean {
        return prefs.getStringSet(KEY_SESSION_UNLOCKED, emptySet())?.contains(packageName) == true
    }

    fun clearSessionUnlock(packageName: String) {
        val current = prefs.getStringSet(KEY_SESSION_UNLOCKED, emptySet())?.toMutableSet()
            ?: return
        if (current.remove(packageName)) {
            prefs.edit().putStringSet(KEY_SESSION_UNLOCKED, current).apply()
        }
    }

    // ---------- History log ----------

    enum class EventType {
        UNLOCK_SUCCESS, UNLOCK_FAIL, APP_ADDED, APP_REMOVED,
        CODE_CHANGED, ACCESSIBILITY_DISABLED, UNINSTALL_ATTEMPT
    }

    data class HistoryEvent(val type: EventType, val detail: String?, val timestampMillis: Long) {
        fun formattedTime(): String {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }
    }

    fun logEvent(type: EventType, detail: String?) {
        val array = try {
            JSONArray(prefs.getString(KEY_HISTORY, "[]"))
        } catch (e: Exception) {
            JSONArray()
        }

        val entry = JSONObject()
        entry.put("type", type.name)
        entry.put("detail", detail ?: JSONObject.NULL)
        entry.put("time", System.currentTimeMillis())
        array.put(entry)

        // Cap history length to avoid unbounded growth.
        val trimmed = if (array.length() > MAX_HISTORY_ENTRIES) {
            JSONArray(
                (array.length() - MAX_HISTORY_ENTRIES until array.length()).map { array.get(it) }
            )
        } else {
            array
        }

        prefs.edit().putString(KEY_HISTORY, trimmed.toString()).apply()
    }

    fun getHistory(): List<HistoryEvent> {
        val array = try {
            JSONArray(prefs.getString(KEY_HISTORY, "[]"))
        } catch (e: Exception) {
            JSONArray()
        }

        val list = mutableListOf<HistoryEvent>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val type = try {
                EventType.valueOf(obj.getString("type"))
            } catch (e: Exception) {
                continue
            }
            val detail = if (obj.isNull("detail")) null else obj.getString("detail")
            val time = obj.optLong("time", 0L)
            list.add(HistoryEvent(type, detail, time))
        }
        return list.sortedByDescending { it.timestampMillis }
    }

    companion object {
        private const val KEY_CODE_HASH = "code_hash"
        private const val KEY_LOCKED_PACKAGES = "locked_packages"
        private const val KEY_SESSION_UNLOCKED = "session_unlocked"
        private const val KEY_HISTORY = "history_log"
        private const val MAX_HISTORY_ENTRIES = 300

        @Volatile
        private var instance: AppLockStore? = null

        fun getInstance(context: Context): AppLockStore {
            return instance ?: synchronized(this) {
                instance ?: AppLockStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
