package com.diyorbek.applock.ui

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diyorbek.applock.R
import com.diyorbek.applock.data.AppLockStore
import com.diyorbek.applock.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val context: Context,
    private val events: List<AppLockStore.HistoryEvent>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        val appLabel = event.detail?.let { resolveAppLabel(it) } ?: event.detail

        val description = when (event.type) {
            AppLockStore.EventType.UNLOCK_SUCCESS ->
                context.getString(R.string.event_attempt_success, appLabel ?: "?")
            AppLockStore.EventType.UNLOCK_FAIL ->
                context.getString(R.string.event_attempt_fail, appLabel ?: "?")
            AppLockStore.EventType.APP_ADDED ->
                context.getString(R.string.event_app_added, appLabel ?: "?")
            AppLockStore.EventType.APP_REMOVED ->
                context.getString(R.string.event_app_removed, appLabel ?: "?")
            AppLockStore.EventType.CODE_CHANGED ->
                context.getString(R.string.event_code_changed)
            AppLockStore.EventType.ACCESSIBILITY_DISABLED ->
                context.getString(R.string.event_accessibility_disabled)
            AppLockStore.EventType.UNINSTALL_ATTEMPT ->
                context.getString(R.string.event_uninstall_attempt)
        }

        holder.binding.textEventDescription.text = description
        holder.binding.textEventTime.text = event.formattedTime()
    }

    override fun getItemCount(): Int = events.size

    private fun resolveAppLabel(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
