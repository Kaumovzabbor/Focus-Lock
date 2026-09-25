package com.diyorbek.applock.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.diyorbek.applock.data.AppLockStore
import com.diyorbek.applock.data.InstalledAppInfo
import com.diyorbek.applock.databinding.ActivityAppPickerBinding
import com.diyorbek.applock.util.InstalledAppsHelper

class AppPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppPickerBinding
    private lateinit var store: AppLockStore
    private lateinit var adapter: AppPickerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
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
            showPicker()
        } else {
            binding.editGateCode.text?.clear()
        }
    }

    private fun showPicker() {
        binding.layoutGate.visibility = android.view.View.GONE
        binding.layoutContent.visibility = android.view.View.VISIBLE

        val apps = InstalledAppsHelper.getLaunchableApps(this)

        adapter = AppPickerAdapter(
            allApps = apps,
            isChecked = { pkg -> store.isLocked(pkg) },
            onToggle = { app: InstalledAppInfo, checked: Boolean ->
                if (checked) {
                    store.addLockedPackage(app.packageName, app.label)
                } else {
                    store.removeLockedPackage(app.packageName, app.label)
                }
            }
        )

        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = adapter

        binding.editSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
}
