package com.axisstudio.simplelauncher

import android.content.Context

object PrefsHelper {

    private const val PREFS_NAME = "qadi_launcher_prefs"
    private const val KEY_LAYOUT_MODE = "layout_mode"
    private const val KEY_WALLPAPER_URI = "wallpaper_uri"
    private const val KEY_COLUMN_COUNT = "column_count"
    private const val KEY_ICON_SIZE = "icon_size_dp"
    private const val KEY_SHOW_LABELS = "show_labels"
    private const val KEY_GROUP_BY_CATEGORY = "group_by_category"
    private const val KEY_PINNED_APPS = "pinned_apps"
    private const val KEY_EXCLUDED_APPS = "excluded_apps"
    private const val KEY_FROZEN_SECTIONS = "frozen_sections"
    private const val KEY_FROZEN_SECTION_PREFIX = "frozen_section_content_"

    const val MODE_FIXED_GRID = "fixed"
    const val MODE_TABS = "tabs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- نمط العرض ----
    fun getLayoutMode(context: Context): String =
        prefs(context).getString(KEY_LAYOUT_MODE, MODE_TABS) ?: MODE_TABS

    fun setLayoutMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_LAYOUT_MODE, mode).apply()
    }

    // ---- الخلفية ----
    fun getWallpaperUri(context: Context): String? =
        prefs(context).getString(KEY_WALLPAPER_URI, null)

    fun setWallpaperUri(context: Context, uri: String) {
        prefs(context).edit().putString(KEY_WALLPAPER_URI, uri).apply()
    }

    fun resetWallpaper(context: Context) {
        prefs(context).edit().remove(KEY_WALLPAPER_URI).apply()
    }

    // ---- عدد الأعمدة ----
    fun getColumnCount(context: Context): Int =
        prefs(context).getInt(KEY_COLUMN_COUNT, 4)

    fun setColumnCount(context: Context, count: Int) {
        prefs(context).edit().putInt(KEY_COLUMN_COUNT, count).apply()
    }

    // ---- حجم الأيقونة ----
    fun getIconSizeDp(context: Context): Int =
        prefs(context).getInt(KEY_ICON_SIZE, 56)

    fun setIconSizeDp(context: Context, sizeDp: Int) {
        prefs(context).edit().putInt(KEY_ICON_SIZE, sizeDp).apply()
    }

    // ---- إظهار اسم التطبيق ----
    fun getShowLabels(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_LABELS, true)

    fun setShowLabels(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_LABELS, show).apply()
    }

    // ---- تجميع حسب الفئة ----
    fun getGroupByCategory(context: Context): Boolean =
        prefs(context).getBoolean(KEY_GROUP_BY_CATEGORY, false)

    fun setGroupByCategory(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_GROUP_BY_CATEGORY, enabled).apply()
    }

    // ---- التطبيقات المثبتة (Pin) ----
    fun getPinnedApps(context: Context): MutableSet<String> =
        HashSet(prefs(context).getStringSet(KEY_PINNED_APPS, emptySet()) ?: emptySet())

    fun togglePinned(context: Context, packageName: String) {
        val current = getPinnedApps(context)
        if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
        prefs(context).edit().putStringSet(KEY_PINNED_APPS, current).apply()
    }

    // ---- التطبيقات المستبعدة من الترتيب التلقائي ----
    fun getExcludedApps(context: Context): MutableSet<String> =
        HashSet(prefs(context).getStringSet(KEY_EXCLUDED_APPS, emptySet()) ?: emptySet())

    fun toggleExcluded(context: Context, packageName: String) {
        val current = getExcludedApps(context)
        if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
        prefs(context).edit().putStringSet(KEY_EXCLUDED_APPS, current).apply()
    }

    // ---- الأقسام المجمّدة ----
    fun getFrozenSections(context: Context): MutableSet<String> =
        HashSet(prefs(context).getStringSet(KEY_FROZEN_SECTIONS, emptySet()) ?: emptySet())

    fun toggleFrozenSection(context: Context, index: Int) {
        val key = index.toString()
        val current = getFrozenSections(context)
        if (current.contains(key)) current.remove(key) else current.add(key)
        prefs(context).edit().putStringSet(KEY_FROZEN_SECTIONS, current).apply()
    }

    // محتوى القسم المجمّد (قائمة أسماء حزم مفصولة بفاصلة) — يحفظ آخر حالة معروفة للقسم المجمّد
    fun saveFrozenSectionContent(context: Context, index: Int, packageNames: List<String>) {
        prefs(context).edit()
            .putString(KEY_FROZEN_SECTION_PREFIX + index, packageNames.joinToString(","))
            .apply()
    }

    fun getFrozenSectionContent(context: Context, index: Int): List<String> {
        val raw = prefs(context).getString(KEY_FROZEN_SECTION_PREFIX + index, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }
}
