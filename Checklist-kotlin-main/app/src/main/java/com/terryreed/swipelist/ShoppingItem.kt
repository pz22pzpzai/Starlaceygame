package com.terryreed.swipelist

/**
 * Represents a single item in the swipelist.
 */
data class ShoppingItem(
    val name: String,
    val note: String = "",
    var isChecked: Boolean = false
)
