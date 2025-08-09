package com.example.smartdailyexpensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val notes: String?,
    val receiptImageUri: String?,
    val timestamp: Long
)