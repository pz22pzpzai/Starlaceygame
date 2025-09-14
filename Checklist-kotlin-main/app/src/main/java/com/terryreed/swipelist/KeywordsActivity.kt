package com.terryreed.swipelist

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** Displays and manages keywords for a selected category. */
class KeywordsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keywords)

        val categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: return finish()
        title = getString(R.string.keywords_for, categoryName)

        val category = runCatching {
            GroceryCategorizer.Category.valueOf(
                categoryName.uppercase().replace(' ', '_')
            )
        }.getOrDefault(GroceryCategorizer.Category.OTHER)

        val recycler: RecyclerView = findViewById(R.id.recyclerKeywords)
        recycler.layoutManager = LinearLayoutManager(this)
        lateinit var adapter: KeywordAdapter
        adapter = KeywordAdapter(GroceryCategorizer.keywordsFor(category)) { position ->
            showDeleteKeywordDialog(category, adapter, position)
        }
        recycler.adapter = adapter

        val query = intent.getStringExtra(EXTRA_QUERY)
        if (!query.isNullOrBlank()) {
            val index = adapter.findIndex(query)
            if (index >= 0) {
                recycler.post { recycler.smoothScrollToPosition(index) }
                adapter.highlightKeywordAt(index)
            }
        }

        findViewById<FloatingActionButton>(R.id.fabAddKeyword).setOnClickListener {
            showAddKeywordDialog(category, categoryName, adapter)
        }
    }

    private fun showDeleteKeywordDialog(
        category: GroceryCategorizer.Category,
        adapter: KeywordAdapter,
        position: Int
    ) {
        val keyword = adapter.getItem(position)
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_keyword_title)
            .setMessage(getString(R.string.delete_keyword_message, keyword))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                GroceryCategorizer.removeKeyword(category, keyword, applicationContext)
                adapter.removeItem(position)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showAddKeywordDialog(
        category: GroceryCategorizer.Category,
        categoryName: String,
        adapter: KeywordAdapter
    ) {
        val input = EditText(this).apply { hint = getString(R.string.add_keywords_hint) }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_keywords_title, categoryName))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val keywords = input.text.toString()
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                if (keywords.isNotEmpty()) {
                    GroceryCategorizer.addKeywords(category, keywords, applicationContext)
                    adapter.addItems(keywords)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_QUERY = "extra_query"
    }
}
