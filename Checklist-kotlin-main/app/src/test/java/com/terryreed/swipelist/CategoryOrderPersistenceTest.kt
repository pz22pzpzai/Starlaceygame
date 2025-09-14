package com.terryreed.swipelist

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoryOrderPersistenceTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = TestContext()
        CategoryOrderRepository.order.clear()
    }

    @Test
    fun `added categories persist after reload`() {
        CategoryOrderRepository.add("Custom")
        CategoryOrderRepository.persist(context)
        CategoryOrderRepository.order.clear()
        CategoryOrderRepository.load(context)
        assertTrue(CategoryOrderRepository.order.contains("Custom"))
    }

    private class TestContext : Application() {
        private val prefs = InMemorySharedPreferences()
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = prefs
    }

    private class InMemorySharedPreferences : SharedPreferences {
        private val data = mutableMapOf<String, String?>()
        override fun getString(key: String?, defValue: String?): String? = data[key] ?: defValue
        override fun edit(): SharedPreferences.Editor = Editor()

        private inner class Editor : SharedPreferences.Editor {
            private val updates = mutableMapOf<String, String?>()
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                updates[key!!] = value
                return this
            }
            override fun commit(): Boolean {
                for ((k, v) in updates) {
                    if (v == null) data.remove(k) else data[k] = v
                }
                updates.clear()
                return true
            }
            override fun apply() { commit() }
            override fun clear(): SharedPreferences.Editor { data.clear(); updates.clear(); return this }
            override fun remove(key: String?): SharedPreferences.Editor { updates[key!!] = null; return this }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
        }

        override fun contains(key: String?): Boolean = data.containsKey(key)
        override fun getAll(): MutableMap<String, *> = data as MutableMap<String, *>
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = null
        override fun getInt(key: String?, defValue: Int): Int = defValue
        override fun getLong(key: String?, defValue: Long): Long = defValue
        override fun getFloat(key: String?, defValue: Float): Float = defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }
}

