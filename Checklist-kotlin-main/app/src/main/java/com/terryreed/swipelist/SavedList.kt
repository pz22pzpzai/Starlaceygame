package com.terryreed.swipelist

data class SavedList(
    val title: String,
    val items: List<ShoppingItem>,
    /** Epoch millis when the list was saved. */
    val savedAt: Long = System.currentTimeMillis(),
)
