package com.axisstudio.simplelauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // ---- نمط العرض ----
        findViewById<Button>(R.id.btnFixedGrid).setOnClickListener {
            PrefsHelper.setLayoutMode(this, PrefsHelper.MODE_FIXED_GRID)
            restartMain()
        }
        findViewById<Button>(R.id.btnTabs).setOnClickListener {
            PrefsHelper.setLayoutMode(this, PrefsHelper.MODE_TABS)
            restartMain()
        }

        // ---- الخلفية ----
        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: SecurityException) {
                    // بعض المزودين ما يدعمون الصلاحية الدائمة، بتشتغل بالجلسة الحالية بس
                }
                PrefsHelper.setWallpaperUri(this, uri.toString())
                Toast.makeText(this, "تم تغيير الخلفية", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btnChooseWallpaper).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        findViewById<Button>(R.id.btnResetWallpaper).setOnClickListener {
            PrefsHelper.resetWallpaper(this)
            Toast.makeText(this, "رجّعت للخلفية الافتراضية", Toast.LENGTH_SHORT).show()
        }

        // ---- عدد الأعمدة ----
        findViewById<Button>(R.id.btnColumns3).setOnClickListener { PrefsHelper.setColumnCount(this, 3) }
        findViewById<Button>(R.id.btnColumns4).setOnClickListener { PrefsHelper.setColumnCount(this, 4) }
        findViewById<Button>(R.id.btnColumns5).setOnClickListener { PrefsHelper.setColumnCount(this, 5) }

        // ---- حجم الأيقونة ----
        findViewById<Button>(R.id.btnIconSmall).setOnClickListener { PrefsHelper.setIconSizeDp(this, 44) }
        findViewById<Button>(R.id.btnIconMedium).setOnClickListener { PrefsHelper.setIconSizeDp(this, 56) }
        findViewById<Button>(R.id.btnIconLarge).setOnClickListener { PrefsHelper.setIconSizeDp(this, 68) }

        // ---- إظهار الاسم ----
        val showLabelsSwitch = findViewById<Switch>(R.id.switchShowLabels)
        showLabelsSwitch.isChecked = PrefsHelper.getShowLabels(this)
        showLabelsSwitch.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setShowLabels(this, isChecked)
        }

        // ---- التجميع حسب الفئة ----
        val groupSwitch = findViewById<Switch>(R.id.switchGroupByCategory)
        groupSwitch.isChecked = PrefsHelper.getGroupByCategory(this)
        groupSwitch.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setGroupByCategory(this, isChecked)
        }

        findViewById<Button>(R.id.btnDone).setOnClickListener { restartMain() }
    }

    private fun restartMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
