package com.terryreed.swipelist

import org.junit.Assert.assertEquals
import org.junit.Test

class SavedListsRepositoryTest {

    @Test
    fun `saveList replaces existing list`() {
        SavedListsRepository.lists.clear()
        val original = listOf(ShoppingItem("Milk"))
        SavedListsRepository.saveList(original, title = "first")
        val updated = listOf(ShoppingItem("Milk"), ShoppingItem("Bread"))
        SavedListsRepository.saveList(updated, 0, title = "second")
        assertEquals(updated, SavedListsRepository.lists[0].items)
        assertEquals("second", SavedListsRepository.lists[0].title)
    }

    @Test
    fun `saveList with empty list removes existing`() {
        SavedListsRepository.lists.clear()
        val original = listOf(ShoppingItem("Milk"))
        SavedListsRepository.saveList(original, title = "first")
        SavedListsRepository.saveList(emptyList(), index = 0, title = "first")
        assertEquals(0, SavedListsRepository.lists.size)
    }
}
