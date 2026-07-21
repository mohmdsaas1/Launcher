package com.axisstudio.simplelauncher

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var background: ImageView
    private lateinit var sectionsView: RecyclerView
    private lateinit var pageIndicator: TextView
    private lateinit var searchBox: EditText
    private lateinit var drawerButton: ImageButton
    private val sectionCount = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        goFullscreen()

        background = findViewById(R.id.background)
        sectionsView = findViewById(R.id.sectionsView)
        pageIndicator = findViewById(R.id.pageIndicator)
        searchBox = findViewById(R.id.searchBox)
        drawerButton = findViewById(R.id.drawerButton)

        applyWallpaper()

        sectionsView.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        drawerButton.setOnClickListener {
            startActivity(Intent(this, AppDrawerActivity::class.java))
        }

        searchBox.setOnClickListener {
            startActivity(Intent(this, AppDrawerActivity::class.java))
        }

        if (!UsageHelper.hasUsagePermission(this)) {
            Toast.makeText(
                this,
                "لازم تفعّل صلاحية الوصول للاستخدام عشان يرتب تطبيقاتك تلقائيًا",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            loadApps()
        }
    }

    override fun onResume() {
        super.onResume()
        goFullscreen()
        applyWallpaper()
        if (UsageHelper.hasUsagePermission(this)) {
            loadApps()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) goFullscreen()
    }

    private fun goFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }

    private fun applyWallpaper() {
        val uriString = PrefsHelper.getWallpaperUri(this)
        if (uriString != null) {
            try {
                val uri = Uri.parse(uriString)
                contentResolver.openInputStream(uri)?.use { stream ->
                    background.setImageBitmap(BitmapFactory.decodeStream(stream))
                }
                return
            } catch (e: Exception) {
                // فشل تحميل الخلفية المخصصة، نرجع للافتراضية
            }
        }
        try {
            assets.open("default_wallpaper.jpg").use { stream ->
                background.setImageBitmap(BitmapFactory.decodeStream(stream))
            }
        } catch (e: Exception) {
            // ما فيه خلفية افتراضية، تبقى سادة
        }
    }

    private fun loadApps() {
        val sections = UsageHelper.buildSections(this, sectionCount)
        val mode = PrefsHelper.getLayoutMode(this)

        val adapter = SectionCardAdapter(
            sections,
            onAppClick = { app -> launchApp(app) },
            onAppChanged = { loadApps() },
            onFreezeToggle = { index ->
                PrefsHelper.toggleFrozenSection(this, index)
                loadApps()
            }
        )
        sectionsView.adapter = adapter
        sectionsView.onFlingListener = null

        if (mode == PrefsHelper.MODE_FIXED_GRID) {
            sectionsView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            pageIndicator.text = "عرض: شبكة ثابتة"
        } else {
            sectionsView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            PagerSnapHelper().attachToRecyclerView(sectionsView)
            pageIndicator.text = "1 / $sectionCount"
            sectionsView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val lm = recyclerView.layoutManager as? LinearLayoutManager
                        val pos = lm?.findFirstVisibleItemPosition() ?: 0
                        pageIndicator.text = "${pos + 1} / $sectionCount"
                    }
                }
            })
        }
    }

    private fun launchApp(app: AppEntry) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "ما قدرت أفتح التطبيق", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        // متعمد نتجاهله — هذا لانشر
    }
}
