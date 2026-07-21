package com.axisstudio.simplelauncher

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Process
import java.util.Calendar

object UsageHelper {

    fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // كل التطبيقات المثبتة (بدون استبعاد أي شي)، مرتبة من الأحدث استخدامًا للأقدم
    fun getAllAppsSortedByRecency(context: Context): List<AppEntry> {
        val pm = context.packageManager

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = Calendar.getInstance().apply {
            timeInMillis = end
            add(Calendar.DAY_OF_YEAR, -30)
        }.timeInMillis

        val usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val lastUsedMap = HashMap<String, Long>()
        for (stat in usageStats) {
            val existing = lastUsedMap[stat.packageName] ?: 0L
            if (stat.lastTimeUsed > existing) lastUsedMap[stat.packageName] = stat.lastTimeUsed
        }

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)

        return resolvedApps.map { resolveInfo ->
            val appInfo: ApplicationInfo = resolveInfo.activityInfo.applicationInfo
            AppEntry(
                label = pm.getApplicationLabel(appInfo).toString(),
                packageName = appInfo.packageName,
                icon = pm.getApplicationIcon(appInfo),
                lastTimeUsed = lastUsedMap[appInfo.packageName] ?: 0L,
                category = appInfo.category
            )
        }.distinctBy { it.packageName }
            .sortedWith(
                compareByDescending<AppEntry> { it.lastTimeUsed }
                    .thenBy { it.label.lowercase() }
            )
    }

    // يبني الأقسام النهائية آخذ بعين الاعتبار: الاستبعاد، التثبيت، التصنيف، التجميد
    fun buildSections(context: Context, sectionCount: Int): List<List<AppEntry>> {
        val allApps = getAllAppsSortedByRecency(context)
        val allAppsByPackage = allApps.associateBy { it.packageName }

        val excluded = PrefsHelper.getExcludedApps(context)
        val pinned = PrefsHelper.getPinnedApps(context)
        val groupByCategory = PrefsHelper.getGroupByCategory(context)

        val pinnedApps = allApps.filter { pinned.contains(it.packageName) }
        var remaining = allApps.filter { !excluded.contains(it.packageName) && !pinned.contains(it.packageName) }

        if (groupByCategory) {
            remaining = remaining.sortedWith(
                compareBy<AppEntry> { it.category == ApplicationInfo.CATEGORY_UNDEFINED || it.category == -1 }
                    .thenBy { it.category }
                    .thenByDescending { it.lastTimeUsed }
            )
        }

        // المثبت دائمًا يطلع أول القائمة (بالقسم الأول)
        val orderedApps = pinnedApps + remaining

        val freshSections = splitIntoSections(orderedApps, sectionCount)

        val frozenIndices = PrefsHelper.getFrozenSections(context).map { it.toInt() }.toSet()

        val finalSections = (0 until sectionCount).map { index ->
            if (frozenIndices.contains(index)) {
                val savedPackages = PrefsHelper.getFrozenSectionContent(context, index)
                val savedApps = savedPackages.mapNotNull { allAppsByPackage[it] }
                if (savedApps.isNotEmpty()) savedApps else freshSections.getOrElse(index) { emptyList() }
            } else {
                val section = freshSections.getOrElse(index) { emptyList() }
                // نحفظ آخر حالة معروفة لهذا القسم (تستخدم لو المستخدم يجمّده لاحقًا)
                PrefsHelper.saveFrozenSectionContent(context, index, section.map { it.packageName })
                section
            }
        }

        return finalSections
    }

    private fun splitIntoSections(apps: List<AppEntry>, sectionCount: Int): List<List<AppEntry>> {
        if (apps.isEmpty()) return List(sectionCount) { emptyList() }
        val perSection = ((apps.size + sectionCount - 1) / sectionCount).coerceAtLeast(1)
        val chunks = apps.chunked(perSection).toMutableList()
        while (chunks.size < sectionCount) chunks.add(emptyList())
        return chunks.take(sectionCount)
    }
}
