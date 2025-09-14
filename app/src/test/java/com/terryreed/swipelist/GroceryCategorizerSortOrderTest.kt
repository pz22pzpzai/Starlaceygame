package com.terryreed.swipelist

import org.junit.Assert.assertEquals
import org.junit.Test

class GroceryCategorizerSortOrderTest {
    @Test
    fun `sorts items using provided category order`() {
        val items = listOf(
            ShoppingItem("Eggs"), // EGGS
            ShoppingItem("Milk"), // DAIRY
            ShoppingItem("Apple"), // PRODUCE
            ShoppingItem("Beer") // ALCOHOL
        )
        val order = listOf(
            GroceryCategorizer.Category.ALCOHOL,
            GroceryCategorizer.Category.PRODUCE,
            GroceryCategorizer.Category.DAIRY,
            GroceryCategorizer.Category.EGGS
        )
        val sorted = GroceryCategorizer.sortByCategory(items, order)
        assertEquals(listOf("Beer", "Apple", "Milk", "Eggs"), sorted.map { it.name })
    }
}
