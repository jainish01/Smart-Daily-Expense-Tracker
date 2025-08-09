package com.example.smartdailyexpensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch')) = :date ORDER BY timestamp DESC")
    fun getExpensesForDate(date: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category AND strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch')) = :date")
    fun getExpensesByCategoryForDate(category: String, date: String): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch')) = :date")
    fun getTotalSpentForDate(date: String): Flow<Double?>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :start AND :end")
    fun getExpensesBetween(start: Long, end: Long): Flow<List<ExpenseEntity>>

    // To check for duplicates by title + amount + date
    @Query("""
        SELECT COUNT(*) FROM expenses 
        WHERE title = :title AND amount = :amount AND strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch')) = :date
    """)
    suspend fun countByTitleAmountDate(title: String, amount: Double, date: String): Int

    // For category totals over last 7 days
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM expenses 
        WHERE timestamp BETWEEN :start AND :end
        GROUP BY category
    """)
    fun getCategoryTotalsBetween(start: Long, end: Long): Flow<List<CategoryTotal>>

    // For daily totals
    @Query("""
        SELECT strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch')) as day,
               SUM(amount) as total 
        FROM expenses 
        WHERE timestamp BETWEEN :start AND :end
        GROUP BY day
    """)
    fun getDailyTotalsBetween(start: Long, end: Long): Flow<List<DailyTotal>>

}

// Helper data classes for aggregates
data class CategoryTotal(val category: String, val total: Double)
data class DailyTotal(val day: String, val total: Double)