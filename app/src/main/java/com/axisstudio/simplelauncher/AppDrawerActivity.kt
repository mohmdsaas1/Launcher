package com.axisstudio.simplelauncher

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppDrawerActivity : AppCompatActivity() {

    private lateinit var allApps: List<AppEntry>
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)

        recyclerView = findViewById(R.id.drawerRecyclerView)
        searchBox = findViewById(R.id.drawerSearchBox)
        recyclerView.layoutManager = LinearLayoutManager(this)

        allApps = UsageHelper.getAllAppsSortedByRecency(this).sortedBy { it.label.lowercase() }
        renderList(allApps)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) allApps
                else allApps.filter { it.label.lowercase().contains(query) }
                renderList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        searchBox.requestFocus()
    }

    private fun renderList(apps: List<AppEntry>) {
        recyclerView.adapter = DrawerListAdapter(apps) { app ->
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                finish()
            } else {
                Toast.makeText(this, "ما قدرت أفتح التطبيق", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed() // هنا نسمح بالرجوع عادي (يرجع للانشر الرئيسي)
    }
}
