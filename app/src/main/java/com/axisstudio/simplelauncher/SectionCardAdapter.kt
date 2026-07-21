package com.axisstudio.simplelauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SectionCardAdapter(
    private val sections: List<List<AppEntry>>,
    private val onAppClick: (AppEntry) -> Unit,
    private val onAppChanged: (() -> Unit)?,
    private val onFreezeToggle: (Int) -> Unit
) : RecyclerView.Adapter<SectionCardAdapter.SectionViewHolder>() {

    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val innerGrid: RecyclerView = view.findViewById(R.id.sectionGrid)
        val freezeIcon: ImageView = view.findViewById(R.id.freezeIcon)
        val sectionLabel: TextView = view.findViewById(R.id.sectionLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val context = holder.itemView.context
        val apps = sections[position]
        val columns = PrefsHelper.getColumnCount(context)

        holder.innerGrid.layoutManager = GridLayoutManager(context, columns)
        holder.innerGrid.adapter = AppGridAdapter(apps, onAppClick, onAppChanged)
        holder.innerGrid.isNestedScrollingEnabled = false

        holder.sectionLabel.text = "قسم ${position + 1}"

        val frozen = PrefsHelper.getFrozenSections(context).contains(position.toString())
        holder.freezeIcon.alpha = if (frozen) 1.0f else 0.35f
        holder.freezeIcon.setOnClickListener { onFreezeToggle(position) }
    }

    override fun getItemCount(): Int = sections.size
}
