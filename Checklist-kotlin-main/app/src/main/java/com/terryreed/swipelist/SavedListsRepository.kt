package com.terryreed.swipelist

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SavedListsRepository {

    private const val PREFS_NAME = "saved_lists"
    private const val KEY_LISTS = "lists"
    private var loaded = false

    val lists: MutableList<SavedList> = mutableListOf()

    /** Index of the list currently opened on the home page. */
    var currentListIndex: Int = -1

    fun load(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_LISTS, null) ?: return
        runCatching {
            val root = JSONArray(json)
            for (i in 0 until root.length()) {
                val element = root.get(i)
                if (element is JSONObject) {
                    val title = element.getString("title")
                    val itemsArray = element.getJSONArray("items")
                    val items = mutableListOf<ShoppingItem>()
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        items.add(
                            ShoppingItem(
                                name = itemObj.getString("name"),
                                note = itemObj.optString("note"),
                                isChecked = itemObj.optBoolean("isChecked")
                            )
                        )
                    }
                    val savedAt = element.optLong("savedAt", System.currentTimeMillis())
                    lists.add(SavedList(title, items, savedAt))
                } else if (element is JSONArray) {
                    val items = mutableListOf<ShoppingItem>()
                    for (j in 0 until element.length()) {
                        val obj = element.getJSONObject(j)
                        items.add(
                            ShoppingItem(
                                name = obj.getString("name"),
                                note = obj.optString("note"),
                                isChecked = obj.optBoolean("isChecked")
                            )
                        )
                    }
                    val title = items.take(3).joinToString(", ") { it.name }
                    lists.add(SavedList(title, items))
                }
            }
        }.onFailure {
            // If persisted data is corrupted, start with an empty list instead of crashing
            lists.clear()
        }
    }

    /**
     * Saves a list of [ShoppingItem]s with [title]. If [index] refers to an existing list,
     * that list is replaced; otherwise the new list is appended.
     */
    fun saveList(list: List<ShoppingItem>, index: Int = -1, title: String) {
        if (list.isEmpty()) {
            if (index in lists.indices) {
                lists.removeAt(index)
            }
            return
        }
        val savedList = SavedList(title, list)
        if (index in lists.indices) {
            lists[index] = savedList
        } else {
            lists.add(savedList)
        }
    }

    fun deleteList(index: Int) {
        if (index in lists.indices) {
            lists.removeAt(index)
        }
    }

    fun persist(context: Context) {
        val root = JSONArray()
        for (saved in lists) {
            val obj = JSONObject()
            obj.put("title", saved.title)
            obj.put("savedAt", saved.savedAt)
            val listArray = JSONArray()
            for (item in saved.items) {
                val itemObj = JSONObject()
                itemObj.put("name", item.name)
                itemObj.put("note", item.note)
                itemObj.put("isChecked", item.isChecked)
                listArray.put(itemObj)
            }
            obj.put("items", listArray)
            root.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LISTS, root.toString()).apply()
    }
}

