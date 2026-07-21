package com.axisstudio.simplelauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DrawerListAdapter(
    private val apps: List<AppEntry>,
    private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<DrawerListAdapter.RowViewHolder>() {

    class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.rowIcon)
        val label: TextView = view.findViewById(R.id.rowLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drawer_app, parent, false)
        return RowViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.label.text = app.label
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = apps.size
}
