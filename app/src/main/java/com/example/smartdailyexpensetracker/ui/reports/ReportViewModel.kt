// ReportViewModel.kt
// Produces summary data for report screen including last 7 days totals and categories (mocked data).

package com.example.smartdailyexpensetracker.ui.reports

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * Daily totals for the chart and summary view.
 * @param rawDate Used for chronological sorting.
 */
data class DailyTotal(val date: String, val amount: Double, val rawDate: Long)

/** Category totals for the summary table. */
data class CategoryTotal(val category: String, val amount: Double)

/** Expense record entity with all possible fields. */
data class ExpenseEntity(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String?,
    val notes: String,
    val timestamp: Long,
    val receiptImageUri: String?
)

/**
 * ViewModel for analytics/report screen, feeding daily and category summaries from mock data.
 */
@HiltViewModel
class ReportViewModel @Inject constructor() : ViewModel() {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displaySdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    // Mock up expenses for last 7 days
    private val expenses: List<ExpenseEntity> = mockExpensesForLastWeek()

    // Calculate daily totals, sorted chronologically (descending)
    val dailyTotals: StateFlow<List<DailyTotal>> = MutableStateFlow(
        expenses.groupBy { sdf.format(Date(it.timestamp)) }
            .map { (dateStr, items) ->
                val rawDateMillis = sdf.parse(dateStr)?.time ?: items[0].timestamp
                DailyTotal(
                    date = displaySdf.format(Date(rawDateMillis)),
                    amount = items.sumOf { it.amount },
                    rawDate = rawDateMillis
                )
            }
            .sortedByDescending { it.rawDate }
    ).asStateFlow()

    // Calculate category totals
    val categoryTotals: StateFlow<List<CategoryTotal>> = MutableStateFlow(
        expenses.groupBy { it.category ?: "Other" }
            .map { (category, items) ->
                CategoryTotal(category = category, amount = items.sumOf { it.amount })
            }
    ).asStateFlow()
}

/**
 * Generates randomized mock expense data for the last 7 days.
 */
fun mockExpensesForLastWeek(): List<ExpenseEntity> {
    val now = Calendar.getInstance()
    val categories = listOf("Food", "Travel", "Utility", "Staff")
    return (0..6).flatMap { daysAgo ->
        val day = (now.clone() as Calendar).apply { add(Calendar.DATE, -daysAgo) }
        // For each day, add several random expenses with different amounts and categories
        List(Random.nextInt(3, 7)) {  // Random number of expenses per day
            val cat = categories.random()
            ExpenseEntity(
                id = Random.nextInt(),
                title = "$cat expense",
                amount = Random.nextDouble(100.0, 800.0),
                category = cat,
                notes = "",
                timestamp = day.timeInMillis + Random.nextInt(0, 86400000), // Spread timestamps during that day
                receiptImageUri = null
            )
        }
    }
}