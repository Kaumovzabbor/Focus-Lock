package com.diyorbek.applock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Accessibility services restart automatically after boot once enabled in
 * system settings, so no explicit action is required here. This receiver
 * exists as a hook for future use (e.g. re-checking permission state).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Intentionally no-op for now.
        }
    }
}
