// ExpenseListScreen.kt
// Displays the main expense history with grouping, filtering, and details.

package com.example.smartdailyexpensetracker.ui.expenselist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.smartdailyexpensetracker.data.local.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * The main screen showing a list of expenses for a given day, grouped by category or time.
 * Supports picking dates, grouping modes, and viewing details for each expense.
 *
 * @param viewModel The viewmodel providing UI state and actions, with Hilt injection as default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    // --- State (derived from ViewModel), reflects current date, mode, filtered+grouped data ---
    val selectedDate by viewModel.selectedDate.collectAsState()
    val groupingMode by viewModel.groupingMode.collectAsState()
    val groupedExpenses by viewModel.groupedExpenses.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()

    // Local state for showing date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // --- Date picker dialog when activated ---
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time,
            // optionally, restrict year range here
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.setSelectedDate(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // Tracks which expense item has been clicked for detail dialog
    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    // --- Expense details dialog/modal ---
    selectedExpense?.let { expense ->
        AlertDialog(
            onDismissRequest = { selectedExpense = null },
            title = { Text(expense.title) },
            text = {
                Column {
                    Text("Category: ${expense.category}")
                    Text("Amount: ₹%.2f".format(expense.amount))
                    expense.notes?.let { Text("Notes: $it") }
                    expense.receiptImageUri?.let { uri ->
                        Spacer(Modifier.height(8.dp))
                        Text("Receipt Image:")
                        AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(120.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedExpense = null }) { Text("Close") }
            }
        )
    }

    // --- Main column layout: header, divider, list or placeholder ---
    Column(modifier = Modifier.fillMaxSize()) {
        ExpenseListHeader(
            selectedDate = selectedDate,
            onDateChange = { showDatePicker = true }, // Triggers dialog
            groupingMode = groupingMode,
            onGroupingChange = viewModel::setGroupingMode,
            totalCount = totalCount,
            totalAmount = totalAmount
        )
        HorizontalDivider()
        if (groupedExpenses.isEmpty()) {
            // Informative placeholder if no results found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No expenses found for this date",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            // Expenses grouped (by category or hour) with sticky group headers
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                groupedExpenses.forEach { (group, expenses) ->
                    item(key = group) {
                        Text(
                            text = group,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp)
                        )
                    }
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItem(expense = expense, onClick = { selectedExpense = it })
                    }
                }
            }
        }
    }
}

/**
 * Header for the expense list. Provides quick controls:
 *  - Selected date display + picker button
 *  - Grouping mode toggle
 *  - Total count and amount summary
 *  - Hint for current filter
 */
@Composable
fun ExpenseListHeader(
    selectedDate: Date,
    onDateChange: () -> Unit,
    groupingMode: GroupingMode,
    onGroupingChange: (GroupingMode) -> Unit,
    totalCount: Int,
    totalAmount: Double
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())
    val simpleDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    // Select correct summary depending on grouping
    val filterText = when (groupingMode) {
        GroupingMode.CATEGORY ->
            "Showing expenses for ${simpleDate.format(selectedDate)} grouped by category"
        GroupingMode.TIME ->
            "Showing expenses for ${simpleDate.format(selectedDate)} grouped by hour"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onDateChange) {
                Text(dateFormat.format(selectedDate))
            }
            Spacer(Modifier.width(16.dp))
            SegmentedButton(groupingMode, onGroupingChange)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = filterText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "$totalCount expenses · ₹%.2f".format(totalAmount),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Two-part toggle button to change grouping mode (category/time of day).
 * Highlights current selection.
 */
@Composable
fun SegmentedButton(selected: GroupingMode, onClick: (GroupingMode) -> Unit) {
    Row {
        Button(
            onClick = { onClick(GroupingMode.CATEGORY) },
            colors = if (selected == GroupingMode.CATEGORY) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
            shape = MaterialTheme.shapes.small
        ) { Text("Category") }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = { onClick(GroupingMode.TIME) },
            colors = if (selected == GroupingMode.TIME) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
            shape = MaterialTheme.shapes.small
        ) { Text("Time") }
    }
}

/**
 * Individual expense row in the list. Clickable to open detail dialog.
 */
@Composable
fun ExpenseItem(
    expense: ExpenseEntity,
    onClick: (ExpenseEntity) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(expense) }, // Opens the details modal
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(expense.category, modifier = Modifier.width(80.dp))
        Spacer(Modifier.width(16.dp))
        Text(expense.title, modifier = Modifier.weight(1f))
        // Show icons if applicable
        if (!expense.notes.isNullOrBlank()) {
            Icon(Icons.AutoMirrored.Default.Note, contentDescription = "Has notes", modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
        }
        if (!expense.receiptImageUri.isNullOrBlank()) {
            Icon(Icons.Default.Image, contentDescription = "Has image", modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text("₹%.2f".format(expense.amount), fontWeight = FontWeight.Bold)
    }
}
