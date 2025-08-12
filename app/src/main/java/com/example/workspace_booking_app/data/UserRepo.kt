package com.example.workspace_booking_app.data

import android.content.Context

class UserRepo(context: Context) {
    private val dbHelper = MyDatabaseHelper(context)
    private val crud = UserCRUD(dbHelper)
    fun insertUser(name: String, email: String, password: String): Long {
        return crud.insertUser(name, email, password)
    }

    fun getValueByFilter(filterColumn : String, filterValue : String):
            Map<String, String?>? {
        return crud.getRowByFilter(filterColumn,filterValue)

    }



}