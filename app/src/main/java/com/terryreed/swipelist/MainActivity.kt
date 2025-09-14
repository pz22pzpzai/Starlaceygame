package com.terryreed.swipelist

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ShoppingItemAdapter
    private var listIndex: Int = -1
    private var listTitle: String? = null

    companion object {
        const val EXTRA_LIST_INDEX = "extra_list_index"
    }

    override fun getLayoutResId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_home

        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ShoppingItemAdapter(mutableListOf()) { item ->
            showSaveItemToCategoryDialog(item)
        }
        recycler.adapter = adapter

        listIndex = intent.getIntExtra(EXTRA_LIST_INDEX, SavedListsRepository.currentListIndex)
        if (listIndex >= 0 && listIndex < SavedListsRepository.lists.size) {
            val saved = SavedListsRepository.lists[listIndex]
            listTitle = saved.title
            val items = saved.items.map { it.copy() }
            adapter.addItemsSorted(items, CategoryOrderRepository.asEnumList())
        } else {
            listIndex = -1
        }
        SavedListsRepository.currentListIndex = listIndex

        val saveButton: MaterialButton = findViewById(R.id.btnSaveList)
        saveButton.setOnClickListener {
            if (listTitle.isNullOrBlank()) {
                promptForTitle {
                    performSave(saveButton)
                }
            } else {
                performSave(saveButton)
            }
        }

        val touchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    val position = viewHolder.bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        adapter.removeItem(position)
                    }
                    if (adapter.itemCount > 0) {
                        saveButton.visibility = View.VISIBLE
                    } else {
                        saveButton.visibility = View.GONE
                        if (listIndex != -1) {
                            SavedListsRepository.deleteList(listIndex)
                            SavedListsRepository.persist(this@MainActivity)
                            listIndex = -1
                            SavedListsRepository.currentListIndex = -1
                        }
                    }
                }
            }
        )
        touchHelper.attachToRecyclerView(recycler)

        val quickAdd: MaterialButton = findViewById(R.id.btnQuickAdd)
        quickAdd.setOnClickListener {
            // If a list is already on screen, automatically save it and start fresh
            if (adapter.itemCount > 0) {
                val saveButton: MaterialButton = findViewById(R.id.btnSaveList)
                if (listTitle.isNullOrBlank()) {
                    listTitle = generateTitle(adapter.getItems())
                }
                performSave(saveButton, showConfirmation = false)
                adapter.clearItems()
                listTitle = null
                listIndex = -1
                SavedListsRepository.currentListIndex = -1
                saveButton.visibility = View.GONE
            }
            showPasteDialog()
        }

        val addButton: MaterialButton = findViewById(R.id.btnAddToList)
        addButton.setOnClickListener { showAddItemDialog() }

    }

    override fun onPause() {
        super.onPause()
        val saveButton: MaterialButton = findViewById(R.id.btnSaveList)
        if (saveButton.visibility == View.VISIBLE && adapter.getItems().isNotEmpty()) {
            if (listTitle.isNullOrBlank()) {
                listTitle = generateTitle(adapter.getItems())
            }
            performSave(saveButton, showConfirmation = false)
        }
    }

    private fun showPasteDialog() {
        val titleInput = EditText(this).apply {
            hint = getString(R.string.add_title_hint)
        }
        val itemsInput = EditText(this).apply {
            hint = getString(R.string.paste_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(titleInput, params)
            addView(itemsInput, params)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.paste_title)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val items = parseItems(itemsInput.text.toString())
                if (items.isNotEmpty()) {
                    val wasEmpty = adapter.itemCount == 0
                    adapter.addItemsSorted(items, CategoryOrderRepository.asEnumList())
                    recycler.smoothScrollToPosition(0)
                    val enteredTitle = titleInput.text.toString().trim()
                    if (enteredTitle.isNotEmpty()) {
                        listTitle = enteredTitle
                    }
                    findViewById<MaterialButton>(R.id.btnSaveList).visibility = View.VISIBLE
                    if (listIndex == -1 && wasEmpty) {
                        Snackbar.make(recycler, R.string.list_instructions, Snackbar.LENGTH_INDEFINITE)
                            .setDuration(5000)
                            .show()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showAddItemDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.add_item_hint)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.add_item_title)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val tokens = input.text.toString()
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                if (tokens.isNotEmpty()) {
                    val wasEmpty = adapter.itemCount == 0
                    val items = tokens.map { ShoppingItem(it) }
                    adapter.addItemsSorted(items, CategoryOrderRepository.asEnumList())
                    recycler.smoothScrollToPosition(0)
                    findViewById<MaterialButton>(R.id.btnSaveList).visibility = View.VISIBLE
                    if (listIndex == -1 && wasEmpty) {
                        Snackbar.make(recycler, R.string.list_instructions, Snackbar.LENGTH_INDEFINITE)
                            .setDuration(5000)
                            .show()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showSaveItemToCategoryDialog(item: ShoppingItem) {
        val categories = CategoryOrderRepository.order.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.save_item_to_category_title)
            .setItems(categories) { _, which ->
                val categoryName = categories[which]
                val category = runCatching {
                    GroceryCategorizer.Category.valueOf(
                        categoryName.uppercase().replace(' ', '_')
                    )
                }.getOrDefault(GroceryCategorizer.Category.OTHER)
                GroceryCategorizer.addKeywords(category, listOf(item.name), applicationContext)
                Snackbar.make(
                    recycler,
                    getString(
                        R.string.item_saved_to_category,
                        item.name,
                        categoryName
                    ),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun parseItems(text: String): List<ShoppingItem> {
        val primary = text.split(Regex("[,\\n]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val tokens = if (primary.size <= 1) {
            text.split(Regex("\\s+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } else {
            primary
        }
        val items = tokens.map { ShoppingItem(it) }
        return GroceryCategorizer.sortByCategory(items, CategoryOrderRepository.asEnumList())
    }

    private fun promptForTitle(onReady: () -> Unit) {
        val input = EditText(this).apply {
            hint = getString(R.string.title_hint)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.title_prompt)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val entered = input.text.toString().trim()
                listTitle = if (entered.isNotEmpty()) entered else generateTitle(adapter.getItems())
                onReady()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun generateTitle(items: List<ShoppingItem>): String =
        items.take(3).joinToString(", ") { it.name }

    private fun performSave(saveButton: MaterialButton, showConfirmation: Boolean = true) {
        val itemsCopy = adapter.getItems().map { it.copy() }
        val title = listTitle ?: generateTitle(itemsCopy)
        SavedListsRepository.saveList(itemsCopy, listIndex, title)
        SavedListsRepository.persist(this)
        if (listIndex == -1) {
            listIndex = SavedListsRepository.lists.size - 1
        }
        SavedListsRepository.currentListIndex = listIndex
        saveButton.visibility = View.GONE
        if (showConfirmation) {
            AlertDialog.Builder(this)
                .setMessage(R.string.list_saved)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }
}
