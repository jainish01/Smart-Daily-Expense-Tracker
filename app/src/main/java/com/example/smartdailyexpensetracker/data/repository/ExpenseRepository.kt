package com.example.smartdailyexpensetracker.data.repository

import com.example.smartdailyexpensetracker.data.local.CategoryTotal
import com.example.smartdailyexpensetracker.data.local.DailyTotal
import com.example.smartdailyexpensetracker.data.local.ExpenseDao
import com.example.smartdailyexpensetracker.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {

    suspend fun addExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    fun getExpensesForDate(date: String): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesForDate(date)

    fun getExpensesByCategoryForDate(category: String, date: String): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByCategoryForDate(category, date)

    fun getTotalSpentForDate(date: String): Flow<Double?> =
        expenseDao.getTotalSpentForDate(date)

    fun getAllExpenses(): Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses()

    fun getExpensesBetween(start: Long, end: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesBetween(start, end)

    suspend fun isDuplicate(title: String, amount: Double, date: String): Boolean =
        expenseDao.countByTitleAmountDate(title, amount, date) > 0

    fun getCategoryTotalsBetween(start: Long, end: Long): Flow<List<CategoryTotal>> =
        expenseDao.getCategoryTotalsBetween(start, end)

    fun getDailyTotalsBetween(start: Long, end: Long): Flow<List<DailyTotal>> =
        expenseDao.getDailyTotalsBetween(start, end)
}