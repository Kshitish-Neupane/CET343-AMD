package com.example.android_development

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabase(context: Context) :
    SQLiteOpenHelper(context, "users.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT
            )
            """.trimIndent()
        )
    }
    fun getUserName(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM users WHERE email = ?",
            arrayOf(email)
        )

        var name = ""
        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }

        cursor.close()
        return name
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun registerUser(name: String, email: String, password: String): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }

        return try {
            db.insertOrThrow("users", null, values)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loginUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}
