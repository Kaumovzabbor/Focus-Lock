package com.diyorbek.applock.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.diyorbek.applock.data.AppLockStore
import com.diyorbek.applock.data.InstalledAppInfo
import com.diyorbek.applock.databinding.ActivityMainBinding
import com.diyorbek.applock.service.AppLockAccessibilityService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var store: AppLockStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = AppLockStore.getInstance(this)

        binding.btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnEnableOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
        binding.btnAddApp.setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnChangeCode.setOnClickListener {
            startActivity(Intent(this, SetCodeActivity::class.java))
        }

        if (!store.isCodeSet()) {
            // Force initial setup before anything else is usable.
            startActivity(Intent(this, SetCodeActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
        refreshLockedApps()
    }

    private fun refreshPermissionStatus() {
        val accessibilityOn = isAccessibilityServiceEnabled()
        binding.textAccessibilityStatus.text = if (accessibilityOn) {
            getString(com.diyorbek.applock.R.string.main_accessibility_ok)
        } else {
            getString(com.diyorbek.applock.R.string.main_accessibility_needed)
        }
        binding.btnEnableAccessibility.visibility =
            if (accessibilityOn) android.view.View.GONE else android.view.View.VISIBLE

        val overlayOn = Settings.canDrawOverlays(this)
        binding.rowOverlay.visibility = if (overlayOn) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun refreshLockedApps() {
        val lockedPackages = store.getLockedPackages()
        binding.textNoApps.visibility =
            if (lockedPackages.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        val pm = packageManager
        val infos = lockedPackages.mapNotNull { pkg ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                InstalledAppInfo(
                    packageName = pkg,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.label.lowercase() }

        binding.recyclerLockedApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerLockedApps.adapter = LockedAppsAdapter(infos)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = "$packageName/${AppLockAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedComponent, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
