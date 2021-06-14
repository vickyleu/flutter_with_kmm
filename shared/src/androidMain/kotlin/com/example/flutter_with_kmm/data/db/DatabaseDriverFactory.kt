package com.example.flutter_with_kmm.data.db

import android.content.Context
import com.example.flutter_with_kmm.shared.db.AppDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AppDatabase.Schema, context, "flutter_with_kmm.db")
    }
}


