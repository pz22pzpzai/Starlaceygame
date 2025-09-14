package com.terryreed.swipelist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryOrderRepositoryTest {
    @Test
    fun `add inserts category when missing`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.add("Produce")
        CategoryOrderRepository.add("Snacks")
        assertEquals(listOf("Produce", "Snacks"), CategoryOrderRepository.order)
    }

    @Test
    fun `add ignores duplicate categories`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.add("Produce")
        CategoryOrderRepository.add("Produce")
        assertEquals(listOf("Produce"), CategoryOrderRepository.order)
    }

    @Test
    fun `removeAt deletes category at index`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.addAll(listOf("Produce", "Snacks"))
        CategoryOrderRepository.removeAt(1)
        assertEquals(listOf("Produce"), CategoryOrderRepository.order)
    }

    @Test
    fun `removeAt ignores invalid index`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.add("Produce")
        CategoryOrderRepository.removeAt(5)
        assertEquals(listOf("Produce"), CategoryOrderRepository.order)
    }

    @Test
    fun `renameAt changes name when valid`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.addAll(listOf("Produce", "Snacks"))
        CategoryOrderRepository.renameAt(1, "Drinks")
        assertEquals(listOf("Produce", "Drinks"), CategoryOrderRepository.order)
    }

    @Test
    fun `renameAt ignores duplicate`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.addAll(listOf("Produce", "Snacks"))
        CategoryOrderRepository.renameAt(1, "Produce")
        assertEquals(listOf("Produce", "Snacks"), CategoryOrderRepository.order)
    }

    @Test
    fun `migrateLegacyCategories splits combined names`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.addAll(listOf("MEAT_FISH", "DAIRY_EGGS"))
        assertTrue(CategoryOrderRepository.migrateLegacyCategories())
        assertEquals(
            listOf("MEAT", "FISH", "DAIRY", "EGGS"),
            CategoryOrderRepository.order
        )
    }

    @Test
    fun `migrateLegacyCategories removes deprecated categories`() {
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.order.addAll(
            listOf("FOOD_CUPBOARD", "Health Beauty", "Produce")
        )
        assertTrue(CategoryOrderRepository.migrateLegacyCategories())
        assertEquals(listOf("Produce"), CategoryOrderRepository.order)
    }
}
