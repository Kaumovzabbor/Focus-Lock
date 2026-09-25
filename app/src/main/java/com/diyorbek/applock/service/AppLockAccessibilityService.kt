package com.diyorbek.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.diyorbek.applock.data.AppLockStore
import com.diyorbek.applock.ui.UnlockActivity

/**
 * Watches for window (app) changes. When the foreground app matches a locked
 * package and hasn't been unlocked this session, it launches the UnlockActivity
 * as an opaque full-screen overlay on top of it.
 */
class AppLockAccessibilityService : AccessibilityService() {

    private var lastForegroundPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == BuildConfigHolder.SELF_PACKAGE) {
            // Our own UI (including the unlock screen) — never lock ourselves.
            lastForegroundPackage = packageName
            return
        }

        if (packageName != lastForegroundPackage) {
            // The user left whatever app was foregrounded before; clear its
            // session-unlock so it re-prompts next time it's opened.
            lastForegroundPackage?.let { previous ->
                if (previous != packageName) {
                    AppLockStore.getInstance(this).clearSessionUnlock(previous)
                }
            }
        }
        lastForegroundPackage = packageName

        val store = AppLockStore.getInstance(this)
        if (store.isLocked(packageName) && !store.isUnlockedForSession(packageName)) {
            val intent = Intent(this, UnlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(UnlockActivity.EXTRA_TARGET_PACKAGE, packageName)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // No-op: required override.
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        lastForegroundPackage = null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // The service was disabled (e.g. from Settings). Log it so the friend
        // can see this in history next time they check.
        try {
            AppLockStore.getInstance(this)
                .logEvent(AppLockStore.EventType.ACCESSIBILITY_DISABLED, null)
        } catch (e: Exception) {
            // Best-effort only.
        }
        return super.onUnbind(intent)
    }

    private object BuildConfigHolder {
        const val SELF_PACKAGE = "com.diyorbek.applock"
    }
}
