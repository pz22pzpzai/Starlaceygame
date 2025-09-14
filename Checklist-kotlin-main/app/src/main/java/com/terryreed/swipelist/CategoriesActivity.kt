package com.terryreed.swipelist

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriesActivity : BaseActivity() {
    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var adapter: CategoryOrderAdapter
    private lateinit var searchView: SearchView

    override fun getLayoutResId(): Int = R.layout.activity_categories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_categories

        searchView = findViewById(R.id.searchCategories)

        val recycler: RecyclerView = findViewById(R.id.recyclerCategories)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = CategoryOrderAdapter(
            CategoryOrderRepository.order,
            { viewHolder -> if (searchView.query.isEmpty()) touchHelper.startDrag(viewHolder) },
            { category ->
                val intent = Intent(this, KeywordsActivity::class.java).apply {
                    putExtra(KeywordsActivity.EXTRA_CATEGORY, category)
                    val query = searchView.query.toString()
                    if (query.isNotBlank()) {
                        putExtra(KeywordsActivity.EXTRA_QUERY, query)
                    }
                }
                startActivity(intent)
            },
            { position ->
                showCategoryOptionsDialog(adapter, position)
            }
        )
        recycler.adapter = adapter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                adapter.updateItems(filterCategories(newText))
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean = false
        })

        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            showAddCategoryDialog(adapter)
        }

        touchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    adapter.moveItem(from, to)
                    CategoryOrderRepository.move(from, to)
                    CategoryOrderRepository.persist(this@CategoriesActivity)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }
        )
        touchHelper.attachToRecyclerView(recycler)
    }

    private fun showAddCategoryDialog(adapter: CategoryOrderAdapter) {
        val input = EditText(this).apply { hint = getString(R.string.add_category_hint) }
        AlertDialog.Builder(this)
            .setTitle(R.string.add_category_title)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    CategoryOrderRepository.add(name)
                    CategoryOrderRepository.persist(this)
                    adapter.updateItems(filterCategories(searchView.query.toString()))
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showCategoryOptionsDialog(adapter: CategoryOrderAdapter, position: Int) {
        AlertDialog.Builder(this)
            .setItems(arrayOf(getString(R.string.rename), getString(R.string.delete))) { _, which ->
                when (which) {
                    0 -> showRenameCategoryDialog(adapter, position)
                    1 -> showDeleteCategoryDialog(adapter, position)
                }
            }
            .show()
    }

    private fun showRenameCategoryDialog(adapter: CategoryOrderAdapter, position: Int) {
        val original = adapter.getItem(position)
        val input = EditText(this).apply {
            setText(original)
            setSelection(original.length)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.rename_category_title)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = input.text.toString().trim()
                val index = CategoryOrderRepository.order.indexOf(original)
                if (name.isNotEmpty() && index >= 0) {
                    CategoryOrderRepository.renameAt(index, name)
                    CategoryOrderRepository.persist(this)
                    adapter.updateItems(filterCategories(searchView.query.toString()))
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteCategoryDialog(adapter: CategoryOrderAdapter, position: Int) {
        val name = adapter.getItem(position)
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_category_title)
            .setMessage(getString(R.string.delete_category_message, name))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val index = CategoryOrderRepository.order.indexOf(name)
                if (index >= 0) {
                    CategoryOrderRepository.removeAt(index)
                    CategoryOrderRepository.persist(this)
                    adapter.updateItems(filterCategories(searchView.query.toString()))
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** Returns categories containing items or keywords matching [query]. */
    private fun filterCategories(query: String): List<String> {
        if (query.isBlank()) {
            return CategoryOrderRepository.order
        }
        return CategoryOrderRepository.order.filter { name ->
            val category = runCatching {
                GroceryCategorizer.Category.valueOf(name.uppercase().replace(' ', '_'))
            }.getOrDefault(GroceryCategorizer.Category.OTHER)
            GroceryCategorizer.keywordsFor(category).any {
                it.contains(query, ignoreCase = true)
            }
        }
    }

}
