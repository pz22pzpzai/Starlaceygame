package com.terryreed.swipelist

import android.content.Context

/**
 * Stores and persists the preferred ordering of grocery categories.
 */
object CategoryOrderRepository {
    private const val PREFS_NAME = "category_prefs"
    private const val KEY_ORDER = "category_order"

    private val defaultOrder =
        GroceryCategorizer.Category.values().map { it.name.replace('_', ' ') }

    /** Current category order, including any user-added categories. */
    val order: MutableList<String> = defaultOrder.toMutableList()

    /** Loads the saved order from [SharedPreferences] and migrates legacy entries. */
    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_ORDER, null)
        var changed = false
        if (stored != null) {
            val tokens = stored.split(',')
            order.clear()
            tokens.map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { order.add(it) }
            if (order.isEmpty()) {
                order.addAll(defaultOrder)
            }
        }

        if (migrateLegacyCategories()) {
            changed = true
        }

        defaultOrder.forEach {
            if (!order.contains(it)) {
                order.add(it)
                changed = true
            }
        }

        if (changed) {
            persist(context)
        }
    }

    /** Replaces combined legacy category names with the new split names. */
    internal fun migrateLegacyCategories(): Boolean {
        var changed = false
        val iterator = order.listIterator()
        while (iterator.hasNext()) {
            when (iterator.next().uppercase().replace(' ', '_')) {
                "MEAT_FISH" -> {
                    iterator.remove()
                    iterator.add("MEAT")
                    iterator.add("FISH")
                    changed = true
                }
                "DAIRY_EGGS" -> {
                    iterator.remove()
                    iterator.add("DAIRY")
                    iterator.add("EGGS")
                    changed = true
                }
                "FOOD_CUPBOARD", "HEALTH_BEAUTY" -> {
                    iterator.remove()
                    changed = true
                }
            }
        }
        return changed
    }

    /** Persists the current order to [SharedPreferences]. */
    fun persist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ORDER, order.joinToString(",")).commit()
    }

    /** Moves a category from one position to another. */
    fun move(from: Int, to: Int) {
        if (from == to) return
        val item = order.removeAt(from)
        order.add(to, item)
    }

    /** Adds a new category to the end of the order if not already present. */
    fun add(category: String) {
        val trimmed = category.trim()
        if (trimmed.isNotEmpty() && !order.contains(trimmed)) {
            order.add(trimmed)
        }
    }

    /** Removes the category at the given [index] if it exists. */
    fun removeAt(index: Int) {
        if (index in order.indices) {
            order.removeAt(index)
        }
    }

    /** Renames the category at the given [index] to [name] if valid. */
    fun renameAt(index: Int, name: String) {
        if (index in order.indices) {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty() && !order.contains(trimmed)) {
                order[index] = trimmed
            }
        }
    }

    /**
     * Returns the current order converted into [GroceryCategorizer.Category] values for
     * use with sorting. Unknown categories are treated as [GroceryCategorizer.Category.OTHER].
     */
    fun asEnumList(): List<GroceryCategorizer.Category> =
        order.map {
            runCatching {
                GroceryCategorizer.Category.valueOf(it.uppercase().replace(' ', '_'))
            }.getOrDefault(GroceryCategorizer.Category.OTHER)
        }
}

