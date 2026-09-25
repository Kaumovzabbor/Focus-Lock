package com.diyorbek.applock.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.diyorbek.applock.data.AppLockStore
import com.diyorbek.applock.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var store: AppLockStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = AppLockStore.getInstance(this)

        binding.btnGateSubmit.setOnClickListener { attemptGate() }
        binding.editGateCode.setOnEditorActionListener { _, _, _ ->
            attemptGate()
            true
        }
    }

    private fun attemptGate() {
        val code = binding.editGateCode.text?.toString().orEmpty()
        if (code.isEmpty()) return

        if (store.verifyCode(code)) {
            showHistory()
        } else {
            binding.editGateCode.error = null
            binding.editGateCode.text?.clear()
            binding.editGateCode.requestFocus()
        }
    }

    private fun showHistory() {
        binding.layoutGate.visibility = android.view.View.GONE
        binding.layoutContent.visibility = android.view.View.VISIBLE

        val events = store.getHistory()
        if (events.isEmpty()) {
            binding.textHistoryEmpty.visibility = android.view.View.VISIBLE
            binding.recyclerHistory.visibility = android.view.View.GONE
        } else {
            binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
            binding.recyclerHistory.adapter = HistoryAdapter(this, events)
        }
    }
}
