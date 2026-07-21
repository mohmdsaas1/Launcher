package com.axisstudio.simplelauncher

import android.graphics.drawable.Drawable

data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val lastTimeUsed: Long,
    val category: Int // ApplicationInfo.CATEGORY_* أو -1 لو غير محدد
)
