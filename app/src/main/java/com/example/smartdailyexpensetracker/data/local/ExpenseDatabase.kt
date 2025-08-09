package com.example.smartdailyexpensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExpenseEntity::class], version = 1)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}