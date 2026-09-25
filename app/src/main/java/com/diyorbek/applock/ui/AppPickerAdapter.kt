package com.diyorbek.applock.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diyorbek.applock.data.InstalledAppInfo
import com.diyorbek.applock.databinding.ItemAppBinding

class AppPickerAdapter(
    private val allApps: List<InstalledAppInfo>,
    private val isChecked: (String) -> Boolean,
    private val onToggle: (InstalledAppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppPickerAdapter.ViewHolder>() {

    private var filtered: List<InstalledAppInfo> = allApps

    inner class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filtered[position]
        holder.binding.textAppName.text = app.label
        holder.binding.imageAppIcon.setImageDrawable(app.icon)

        // Avoid firing the listener while we programmatically set state.
        holder.binding.checkboxLocked.setOnCheckedChangeListener(null)
        holder.binding.checkboxLocked.isChecked = isChecked(app.packageName)
        holder.binding.checkboxLocked.setOnCheckedChangeListener { _, checked ->
            onToggle(app, checked)
        }

        holder.binding.root.setOnClickListener {
            holder.binding.checkboxLocked.toggle()
        }
    }

    override fun getItemCount(): Int = filtered.size

    fun filter(query: String) {
        filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { it.label.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
