package com.diyorbek.applock.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diyorbek.applock.data.InstalledAppInfo
import com.diyorbek.applock.databinding.ItemAppBinding

/**
 * Shows locked apps on the main screen without an interactive checkbox —
 * changing the list requires going through the code-gated AppPickerActivity.
 */
class LockedAppsAdapter(
    private val apps: List<InstalledAppInfo>
) : RecyclerView.Adapter<LockedAppsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.textAppName.text = app.label
        holder.binding.imageAppIcon.setImageDrawable(app.icon)
        holder.binding.checkboxLocked.visibility = android.view.View.GONE
    }

    override fun getItemCount(): Int = apps.size
}
