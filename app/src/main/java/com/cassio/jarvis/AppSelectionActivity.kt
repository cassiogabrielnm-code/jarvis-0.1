package com.cassio.jarvis

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

data class JarvisApp(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable
)

class AppSelectionActivity : AppCompatActivity() {
    private lateinit var adapter: AppListAdapter
    private lateinit var countText: TextView
    private lateinit var search: EditText
    private val apps = mutableListOf<JarvisApp>()
    private val filteredApps = mutableListOf<JarvisApp>()
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        preferences = getSharedPreferences("jarvis_preferences", MODE_PRIVATE)
        countText = findViewById(R.id.selectedCount)
        search = findViewById(R.id.appSearch)

        loadApps()

        adapter = AppListAdapter()
        findViewById<android.widget.ListView>(R.id.appList).adapter = adapter

        findViewById<Button>(R.id.selectAllButton).setOnClickListener {
            preferences.edit().putStringSet(
                "selected_apps",
                apps.map { it.packageName }.toSet()
            ).apply()
            adapter.notifyDataSetChanged()
            updateCount()
        }

        findViewById<Button>(R.id.clearAllButton).setOnClickListener {
            preferences.edit().putStringSet("selected_apps", emptySet()).apply()
            adapter.notifyDataSetChanged()
            updateCount()
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        updateCount()
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val results = packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }

        apps.clear()
        apps.addAll(results.map {
            JarvisApp(
                label = it.loadLabel(packageManager).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(packageManager)
            )
        })

        filteredApps.clear()
        filteredApps.addAll(apps)
    }

    private fun filterApps(query: String) {
        val normalized = query.trim().lowercase(Locale.getDefault())
        filteredApps.clear()
        filteredApps.addAll(
            if (normalized.isEmpty()) apps
            else apps.filter { it.label.lowercase(Locale.getDefault()).contains(normalized) }
        )
        adapter.notifyDataSetChanged()
    }

    private fun isSelected(packageName: String): Boolean {
        return preferences.getStringSet("selected_apps", emptySet())?.contains(packageName) == true
    }

    private fun setSelected(packageName: String, selected: Boolean) {
        val current = preferences.getStringSet("selected_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (selected) current.add(packageName) else current.remove(packageName)
        preferences.edit().putStringSet("selected_apps", current).apply()
        updateCount()
    }

    private fun updateCount() {
        val count = preferences.getStringSet("selected_apps", emptySet())?.size ?: 0
        countText.text = "$count aplicativo(s) autorizado(s)"
    }

    private inner class AppListAdapter : BaseAdapter() {
        override fun getCount() = filteredApps.size
        override fun getItem(position: Int) = filteredApps[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(this@AppSelectionActivity)
                .inflate(R.layout.item_app, parent, false)

            val app = getItem(position)
            val icon = view.findViewById<ImageView>(R.id.appIcon)
            val name = view.findViewById<TextView>(R.id.appName)
            val check = view.findViewById<CheckBox>(R.id.appCheck)

            icon.setImageDrawable(app.icon)
            name.text = app.label

            check.setOnCheckedChangeListener(null)
            check.isChecked = isSelected(app.packageName)
            check.setOnCheckedChangeListener { _, checked ->
                setSelected(app.packageName, checked)
            }

            view.setOnClickListener {
                check.isChecked = !check.isChecked
            }

            return view
        }
    }
}