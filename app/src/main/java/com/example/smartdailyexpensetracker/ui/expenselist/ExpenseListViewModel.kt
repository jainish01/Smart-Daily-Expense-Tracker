// ExpenseListViewModel.kt
// Provides UI state and business logic for displaying expenses, grouped by date/category/time.

package com.example.smartdailyexpensetracker.ui.expenselist

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartdailyexpensetracker.data.local.ExpenseEntity
import com.example.smartdailyexpensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Enum for how expenses are grouped in the list: by category or time-of-day.
 */
enum class GroupingMode {
    CATEGORY, TIME
}

/**
 * ViewModel backing the ExpenseListScreen:
 * - Tracks selected date, grouping mode, and serves filtered/processed flows for expenses and totals.
 * - Uses Hilt DI to inject the ExpenseRepository.
 */
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    // Current date selected by user for filtering (start of day)
    private val _selectedDate = MutableStateFlow(todayDate())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    // Current grouping mode (category or time-of-day)
    private val _groupingMode = MutableStateFlow(GroupingMode.CATEGORY)
    val groupingMode: StateFlow<GroupingMode> = _groupingMode.asStateFlow()

    // Expenses for the selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<ExpenseEntity>> =
        _selectedDate.flatMapLatest {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            repository.getExpensesForDate(dateStr)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total sum for filtered expenses
    val totalAmount: StateFlow<Double> =
        expenses.map { list -> list.sumOf { it.amount } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Count for filtered expenses
    val totalCount: StateFlow<Int> =
        expenses.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Group expenses by either category or hour-of-day, for display in sections
    val groupedExpenses: StateFlow<Map<String, List<ExpenseEntity>>> =
        combine(expenses, groupingMode) { expenses, grouping ->
            when (grouping) {
                GroupingMode.CATEGORY -> expenses.groupBy { it.category }
                GroupingMode.TIME -> expenses.groupBy { hourLabel(it.timestamp) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Sets the selected date (used to filter expenses). */
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    /** Sets the grouping mode (by category or by time). */
    fun setGroupingMode(mode: GroupingMode) {
        _groupingMode.value = mode
    }

    companion object {
        /** Returns a Date set to midnight today. */
        fun todayDate(): Date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(
            Calendar.SECOND,
            0
        ); set(Calendar.MILLISECOND, 0)
        }.time

        /**
         * Formats expense timestamps into hour-of-day label (e.g., '08:00 PM').
         */
        @SuppressLint("DefaultLocale")
        fun hourLabel(timestamp: Long): String {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp
            val hour = cal.get(Calendar.HOUR)
            val ampm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            val formattedHour = if (hour == 0) 12 else hour
            return String.format("%02d:00 %s", formattedHour, ampm)
        }
    }
}
