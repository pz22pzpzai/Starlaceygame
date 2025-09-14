package com.terryreed.swipelist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class SavedListsActivity : BaseActivity() {

    private lateinit var titleView: TextView
    private lateinit var listView: ListView

    override fun getLayoutResId(): Int = R.layout.activity_saved_lists

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_saved_lists

        titleView = findViewById(R.id.textViewSavedListsTitle)
        listView = findViewById(R.id.listViewSaved)

        updateTitle()

        val adapter = SavedListsAdapter(SavedListsRepository.lists)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            adapter.collapse()
            val intent = Intent(this@SavedListsActivity, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_LIST_INDEX, position)
            startActivity(intent)
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            adapter.toggleExpanded(position)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        (listView.adapter as? BaseAdapter)?.notifyDataSetChanged()
        updateTitle()
    }

    private fun updateTitle() {
        titleView.text = if (SavedListsRepository.lists.isEmpty()) {
            getString(R.string.no_saved_lists)
        } else {
            getString(R.string.saved_lists)
        }
    }

    private inner class SavedListsAdapter(
        private val lists: List<SavedList>
    ) : BaseAdapter() {

        private var expandedPosition = -1

        fun toggleExpanded(position: Int) {
            expandedPosition = if (expandedPosition == position) -1 else position
            notifyDataSetChanged()
        }

        fun collapse() {
            if (expandedPosition != -1) {
                expandedPosition = -1
                notifyDataSetChanged()
            }
        }

        override fun getCount(): Int = lists.size

        override fun getItem(position: Int): Any = lists[position]

        override fun getItemId(position: Int): Long = position.toLong()

        private val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(this@SavedListsActivity)
                .inflate(R.layout.list_item_saved_list, parent, false)
            val saved = lists[position]
            val textView = view.findViewById<TextView>(R.id.textViewSavedItem)
            textView.text = saved.title
            val dateView = view.findViewById<TextView>(R.id.textViewSavedDate)
            dateView.text = dateFormat.format(Date(saved.savedAt))

            val buttonLayout = view.findViewById<View>(R.id.layoutButtons)
            val copyButton = view.findViewById<MaterialButton>(R.id.btnCopySavedList)
            val editButton = view.findViewById<MaterialButton>(R.id.btnEditSavedList)
            val deleteButton = view.findViewById<MaterialButton>(R.id.btnDeleteSavedList)

            val isExpanded = position == expandedPosition
            buttonLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

            copyButton.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = saved.items.joinToString("\n") { it.name }
                clipboard.setPrimaryClip(ClipData.newPlainText("list", text))
                Toast.makeText(
                    this@SavedListsActivity,
                    R.string.list_copied,
                    Toast.LENGTH_SHORT
                ).show()
                expandedPosition = -1
                notifyDataSetChanged()
            }

            editButton.setOnClickListener {
                val intent = Intent(this@SavedListsActivity, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_LIST_INDEX, position)
                startActivity(intent)
                expandedPosition = -1
                notifyDataSetChanged()
            }

            deleteButton.setOnClickListener {
                SavedListsRepository.deleteList(position)
                SavedListsRepository.persist(this@SavedListsActivity)
                expandedPosition = -1
                this@SavedListsAdapter.notifyDataSetChanged()
                updateTitle()
            }
            return view
        }
    }
}

