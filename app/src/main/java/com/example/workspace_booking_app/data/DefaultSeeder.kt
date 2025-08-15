package com.example.workspace_booking_app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import com.example.workspace_booking_app.utils.ImageUtils

object DatabaseSeeder {

    fun seedDefaults(context: Context, db: SQLiteDatabase) {
        insertDefaultUsers(db)
        insertDefaultWorkspaces(context, db)
    }

    private fun insertDefaultUsers(db: SQLiteDatabase) {
        db.execSQL("""
            INSERT OR IGNORE INTO users (id, name, email, password, role)
            VALUES (1 ,'Admin', 'admin9824@mailinator.com', 'Admin@2498', 'admin')
        """.trimIndent())
    }

    private fun insertDefaultWorkspaces(context: Context, db: SQLiteDatabase) {
        // 1️⃣ Convert drawable to bitmap
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.banner)

        // 2️⃣ Save to internal storage
        val path = ImageUtils.saveBitmapToInternalStorage(context, bitmap, "banners")

        // 3️⃣ Insert into DB using the path string
        db.execSQL("""
            INSERT OR IGNORE INTO workspace (id, name, banner_path)
            VALUES (1, 'Default Workspace', '$path')
        """.trimIndent())
    }
}
