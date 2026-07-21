package com.axisstudio.simplelauncher

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var background: ImageView
    private lateinit var sectionsView: RecyclerView
    private lateinit var pageIndicator: TextView
    private lateinit var searchBox: EditText
    private val sectionCount = 8

    private var gestureStartY = 0f
    private val swipeThresholdPx by lazy { 60 * resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        goFullscreen()

        background = findViewById(R.id.background)
        sectionsView = findViewById(R.id.sectionsView)
        pageIndicator = findViewById(R.id.pageIndicator)
        searchBox = findViewById(R.id.searchBox)

        applyWallpaper()
        setupGestureZones()

        searchBox.setOnClickListener {
            startActivity(Intent(this, AppDrawerActivity::class.java))
        }

        if (!UsageHelper.hasUsagePermission(this)) {
            Toast.makeText(
                this,
                "لازم تفعّل صلاحية الوصول للاستخدام عشان يرتب تطبيقاتك تلقائيًا",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // منطقتين رفيعتين بأعلى وأسفل الشاشة: سحب لتحت من فوق = إعدادات، سحب لفوق من تحت = درج التطبيقات
    @Suppress("ClickableViewAccessibility")
    private fun setupGestureZones() {
        val topZone = findViewById<View>(R.id.topGestureZone)
        val bottomZone = findViewById<View>(R.id.bottomGestureZone)

        topZone.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> gestureStartY = event.rawY
                MotionEvent.ACTION_UP -> {
                    if (event.rawY - gestureStartY > swipeThresholdPx) {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                }
            }
            true
        }

        bottomZone.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> gestureStartY = event.rawY
                MotionEvent.ACTION_UP -> {
                    if (gestureStartY - event.rawY > swipeThresholdPx) {
                        startActivity(Intent(this, AppDrawerActivity::class.java))
                    }
                }
            }
            true
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
            mode,
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
            pageIndicator.text = "اسحب من فوق = إعدادات · من تحت = كل التطبيقات"
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
