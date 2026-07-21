package com.axisstudio.simplelauncher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class AppGridAdapter(
    private val apps: List<AppEntry>,
    private val onAppClick: (AppEntry) -> Unit,
    private val onAppChanged: (() -> Unit)? = null // يُستدعى بعد تثبيت/استبعاد عشان الشاشة تحدّث نفسها
) : RecyclerView.Adapter<AppGridAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        val context = holder.itemView.context

        holder.icon.setImageDrawable(app.icon)
        holder.label.text = app.label

        val sizeDp = PrefsHelper.getIconSizeDp(context)
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
        val params = holder.icon.layoutParams
        params.width = sizePx
        params.height = sizePx
        holder.icon.layoutParams = params

        holder.label.visibility = if (PrefsHelper.getShowLabels(context)) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onAppClick(app) }
        holder.itemView.setOnLongClickListener {
            showAppOptionsDialog(context, app)
            true
        }
    }

    private fun showAppOptionsDialog(context: Context, app: AppEntry) {
        val isPinned = PrefsHelper.getPinnedApps(context).contains(app.packageName)
        val isExcluded = PrefsHelper.getExcludedApps(context).contains(app.packageName)

        val options = arrayOf(
            if (isPinned) "إلغاء التثبيت" else "تثبيت (يبقى بمكانه)",
            if (isExcluded) "رجّعه للترتيب التلقائي" else "استبعاد من الترتيب التلقائي"
        )

        AlertDialog.Builder(context)
            .setTitle(app.label)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> PrefsHelper.togglePinned(context, app.packageName)
                    1 -> PrefsHelper.toggleExcluded(context, app.packageName)
                }
                onAppChanged?.invoke()
            }
            .show()
    }

    override fun getItemCount(): Int = apps.size
}
