package com.diyorbek.applock.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.diyorbek.applock.data.AppLockStore
import com.diyorbek.applock.databinding.ActivityUnlockBinding

class UnlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnlockBinding
    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show above the lock screen / on top of the target app reliably.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)

        binding.btnUnlock.setOnClickListener { attemptUnlock() }
        binding.editCode.setOnEditorActionListener { _, _, _ ->
            attemptUnlock()
            true
        }
        binding.btnGoHome.setOnClickListener { goHome() }
    }

    override fun onBackPressed() {
        // Disallow dismissing the lock screen with back — send to home instead.
        goHome()
    }

    private fun attemptUnlock() {
        val code = binding.editCode.text?.toString().orEmpty()
        val store = AppLockStore.getInstance(this)
        val pkg = targetPackage

        if (code.isEmpty()) return

        if (store.verifyCode(code)) {
            if (pkg != null) {
                store.markUnlockedForSession(pkg)
                store.logEvent(AppLockStore.EventType.UNLOCK_SUCCESS, pkg)
            }
            finish()
        } else {
            store.logEvent(AppLockStore.EventType.UNLOCK_FAIL, pkg)
            binding.textError.visibility = android.view.View.VISIBLE
            binding.editCode.text?.clear()
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }
}
