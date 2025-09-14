package com.terryreed.swipelist

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

/**
 * Simple helper to persist user credentials and placeholder app data.
 */
class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Table for storing user credentials.
        db.execSQL(
            "CREATE TABLE $TABLE_USERS (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE, " +
                    "password TEXT)"
        )
        // Placeholder table for other app data.
        db.execSQL(
            "CREATE TABLE $TABLE_DATA (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "content TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DATA")
        onCreate(db)
    }

    /**
     * Inserts a new user. Returns true if successful.
     */
    fun insertUser(username: String, password: String? = null): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
        }
        val result = db.insertWithOnConflict(
            TABLE_USERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
        return result != -1L
    }

    /**
     * Returns true if a user with the given credentials exists.
     */
    fun verifyUser(username: String, password: String): Boolean {
        val db = readableDatabase
        db.rawQuery(
            "SELECT id FROM $TABLE_USERS WHERE username=? AND password= ?",
            arrayOf(username, password)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    companion object {
        private const val DATABASE_NAME = "checklist.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val TABLE_DATA = "data"
    }
}

