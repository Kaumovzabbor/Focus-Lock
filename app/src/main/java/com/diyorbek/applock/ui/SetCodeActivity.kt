package com.diyorbek.applock.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.diyorbek.applock.R
import com.diyorbek.applock.data.AppLockStore
import com.diyorbek.applock.databinding.ActivitySetCodeBinding

/**
 * Screen where the friend sets or changes the shared code. If a code already
 * exists, the current code must be entered first — so the phone's own user
 * can't silently change it without knowing it.
 */
class SetCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetCodeBinding
    private lateinit var store: AppLockStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = AppLockStore.getInstance(this)

        val codeExists = store.isCodeSet()
        binding.textFirstTimeNote.visibility = if (codeExists) android.view.View.GONE else android.view.View.VISIBLE
        binding.layoutCurrentCode.visibility = if (codeExists) android.view.View.VISIBLE else android.view.View.GONE

        binding.btnSaveCode.setOnClickListener { handleSave() }
    }

    private fun handleSave() {
        val codeExists = store.isCodeSet()
        val current = binding.editCurrentCode.text?.toString().orEmpty()
        val newCode = binding.editNewCode.text?.toString().orEmpty()
        val confirm = binding.editConfirmCode.text?.toString().orEmpty()

        if (codeExists && !store.verifyCode(current)) {
            showError(getString(R.string.setcode_wrong_current))
            return
        }

        if (newCode.length < 4) {
            showError(getString(R.string.setcode_too_short))
            return
        }

        if (newCode != confirm) {
            showError(getString(R.string.setcode_mismatch))
            return
        }

        store.setCode(newCode)
        Toast.makeText(this, R.string.setcode_success, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showError(message: String) {
        binding.textSetCodeError.text = message
        binding.textSetCodeError.visibility = android.view.View.VISIBLE
    }
}
