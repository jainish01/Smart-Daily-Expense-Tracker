// Report.kt
// Displays analytic views of expenses, including daily totals, category breakdowns, and export/share options.

package com.example.smartdailyexpensetracker.ui.reports

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * ReportScreen shows analytics for recent expenses (last 7 days).
 * Includes horizontal bar chart for totals, daily and category summaries, and export/share actions.
 *
 * @param viewModel Provides summary data from business logic layer (Hilt DI by default).
 */
@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
    // Observe state from ViewModel
    val dailyTotals by viewModel.dailyTotals.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val context = LocalContext.current

    // Main vertical layout, scrollable if needed
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Heading
        Text(
            "Expense Report (Last 7 Days)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // --- Bar chart visualization for daily spending totals ---
        Text("Spending Overview", style = MaterialTheme.typography.titleMedium)
        if (dailyTotals.isNotEmpty()) {
            val maxAmount = dailyTotals.maxOf { it.amount }
            Column {
                dailyTotals.forEach { day ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Day label
                        Text(
                            day.date,
                            modifier = Modifier.width(68.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                        // Bar (width proportional to amount)
                        Box(
                            Modifier
                                .height(20.dp)
                                .width((day.amount / maxAmount * 180).dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("₹%.0f".format(day.amount))
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Daily Totals", style = MaterialTheme.typography.titleMedium)
        // --- Daily totals in simple table ---
        Column(Modifier.fillMaxWidth()) {
            dailyTotals.forEach {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it.date)
                    Text("₹%.2f".format(it.amount), fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Category-wise Totals", style = MaterialTheme.typography.titleMedium)
        // --- Category breakdown table ---
        Column(Modifier.fillMaxWidth()) {
            categoryTotals.forEach {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it.category)
                    Text("₹%.2f".format(it.amount), fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        // --- Export/share actions ---
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                // Simulated PDF export (real implementation could generate file)
                Toast.makeText(context, "PDF export simulated", Toast.LENGTH_SHORT).show()
            }) { Text("Export as PDF") }
            Button(onClick = {
                // Share daily totals in plain text via Android system share sheet
                val shareText = buildString {
                    append("Expense Report (Last 7 Days)\n\n")
                    dailyTotals.forEach { append("${it.date}: ₹%.2f\n".format(it.amount)) }
                }
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }) { Text("Share") }
        }
    }
}