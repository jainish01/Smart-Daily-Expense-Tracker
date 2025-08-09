// ExpenseEntryViewModel.kt
// UI/business state for adding a new expense: controls fields, validation, submit logic.

package com.example.smartdailyexpensetracker.ui.expenseentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartdailyexpensetracker.data.local.ExpenseEntity
import com.example.smartdailyexpensetracker.data.model.FieldState
import com.example.smartdailyexpensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel backing the UI for entering a new expense.
 * Manages field state, validation, category selection, notes/image, success/error feedback.
 */
@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    // State flows for entry fields (compose observes these directly)
    private val _title = MutableStateFlow(FieldState())
    val title: StateFlow<FieldState> = _title.asStateFlow()

    private val _amount = MutableStateFlow(FieldState())
    val amount: StateFlow<FieldState> = _amount.asStateFlow()

    private val _category = MutableStateFlow(categoryList.first())
    val category: StateFlow<String> = _category.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    // Optional receipt image (URI as string if attached)
    private val _receiptImageUri = MutableStateFlow<String?>(null)
    val receiptImageUri: StateFlow<String?> = _receiptImageUri.asStateFlow()

    // UI feedback state for validation errors and success
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showSuccess = MutableStateFlow(false)
    val showSuccess: StateFlow<Boolean> = _showSuccess.asStateFlow()

    companion object {
        // List of available expense categories
        val categoryList = listOf("Staff", "Travel", "Food", "Utility")
        // Notes input length limit
        const val MAX_NOTES_LENGTH = 100
    }

    // Real-time "Total Spent Today" for current date
    val todayTotal: StateFlow<Double?> =
        repository.getTotalSpentForDate(todayDateString())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ----------------- UI Action Handlers -----------------

    /** Title field changed in UI */
    fun onTitleChange(newTitle: String) {
        _title.value = _title.value.copy(text = newTitle)
    }

    /** Manages focus leave/enter for title field. */
    fun onTitleFocusChange(hasFocus: Boolean) {
        val current = _title.value
        if (hasFocus && !current.hasBeenFocusedOnce) {
            _title.value = current.copy(hasBeenFocusedOnce = true)
        }
        if (!hasFocus && current.hasBeenFocusedOnce) {
            _title.value = current.copy(focusLeft = true)
        }
    }

    /** Manages focus leave/enter for amount field. */
    fun onAmountFocusChange(hasFocus: Boolean) {
        val current = _amount.value
        if (hasFocus && !current.hasBeenFocusedOnce) {
            _amount.value = current.copy(hasBeenFocusedOnce = true)
        }
        if (!hasFocus && current.hasBeenFocusedOnce) {
            _amount.value = current.copy(focusLeft = true)
        }
    }

    /** Amount field changed in UI */
    fun onAmountChange(newAmount: String) {
        _amount.value = _amount.value.copy(text = newAmount)
    }

    /** Category selector changed in UI */
    fun onCategoryChange(newCategory: String) {
        _category.value = newCategory
    }

    /** Notes changed in UI, enforces length */
    fun onNotesChange(newNotes: String) {
        _notes.value = newNotes.take(MAX_NOTES_LENGTH)
    }

    /** Updates receipt image URI */
    fun onReceiptImageUriChange(uri: String?) {
        _receiptImageUri.value = uri
    }

    // ----------------- Submission and Validation -----------------

    /**
     * Attempts to submit the new expense.
     * Validates required fields, duplicate entry, and field constraints.
     * Sets success/error states for UI.
     */
    fun onSubmit() = viewModelScope.launch {
        val title = _title.value.text.trim()
        val amountValue = _amount.value.text.toDoubleOrNull()
        if (title.isEmpty()) {
            _errorMessage.value = "Title cannot be empty"
            return@launch
        }
        if (amountValue == null || amountValue <= 0.0) {
            _errorMessage.value = "Amount must be greater than ₹0"
            return@launch
        }
        if (_notes.value.length > MAX_NOTES_LENGTH) {
            _errorMessage.value = "Notes cannot exceed $MAX_NOTES_LENGTH characters"
            return@launch
        }

        // Check for duplicate entry based on title, amount, and date
        val today = todayDateString()
        val isDuplicate = repository.isDuplicate(title, amountValue, today)
        if (isDuplicate) {
            _errorMessage.value = "Duplicate expense detected"
            return@launch
        }

        // Add new expense
        val expense = ExpenseEntity(
            title = title,
            amount = amountValue,
            category = _category.value,
            notes = _notes.value.ifBlank { null },
            receiptImageUri = _receiptImageUri.value,
            timestamp = System.currentTimeMillis(),
        )
        repository.addExpense(expense)
        _showSuccess.value = true

        // Reset inputs
        _title.value = FieldState()
        _amount.value = FieldState()
        _category.value = categoryList.first()
        _notes.value = ""
        _receiptImageUri.value = null
        _errorMessage.value = null
    }

    /** Returns today's date in yyyy-MM-dd format. */
    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /** Call after success snackbar is shown */
    fun onSuccessShown() {
        _showSuccess.value = false
    }

    /** Call after error snackbar is shown */
    fun onErrorShown() {
        _errorMessage.value = null
    }
}