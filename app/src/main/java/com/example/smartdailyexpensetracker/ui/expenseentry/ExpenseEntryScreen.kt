// ExpenseEntryScreen.kt
// Provides UI for adding a new expense (title, amount, category, notes, optional receipt image), including validation and feedback.

package com.example.smartdailyexpensetracker.ui.expenseentry

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

/**
 * Screen for user to input details of a new expense.
 * Includes validation, feedback (snackbar/toast), category dropdown, and receipt image pick/removal (mocked for now).
 *
 * @param viewModel ExpenseEntryViewModel (defaults to Hilt-provided instance).
 */
@Composable
fun ExpenseEntryScreen(
    viewModel: ExpenseEntryViewModel = hiltViewModel()
) {
    // --- UI State from ViewModel (using StateFlow + Compose lifecycle awareness) ---
    // Collect UI state from ViewModel
    val title by viewModel.title.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val receiptImageUri by viewModel.receiptImageUri.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val showSuccess by viewModel.showSuccess.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Permission state + helper launchers (needed for picking gallery images) ---
    // Permission handling for attaching receipt images
    var showPermissionDialog by remember { mutableStateOf(false) }
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onReceiptImageUriChange(it.toString()) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            showPermissionDialog = true
        }
    }

    // --- Permission dialog for gallery access ---
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission needed") },
            text = { Text("Please grant photo access to attach a receipt image.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // --- Show error feedback as snackbar, reset after ---
    // Display error message as snackbar when needed
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    // --- On success, show a Toast then reset flag ---
    // Show success message as a toast
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            Toast.makeText(context, "Expense added!", Toast.LENGTH_SHORT).show()
            viewModel.onSuccessShown()
        }
    }

    // --- Main scaffold: heading, entry form, feedback ---
    // Main UI structure with top bar and snackbar host
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Heading
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(6.dp))
                // Show today's total at the top
                Text(
                    text = "Total Spent Today: ₹${"%.2f".format(todayTotal ?: 0.0)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // --- Input Form (title, amount, category, notes, receipt, submit) ---
        // Expense entry form
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Title input field (required) ---
            // Input field for expense title
            OutlinedTextField(
                value = title.text,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title*") },
                isError = title.focusLeft && title.text.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        viewModel.onTitleFocusChange(it.isFocused)
                    },
                singleLine = true
            )

            // --- Amount input field (required, numeric keyboard) ---
            // Input field for expense amount
            OutlinedTextField(
                value = amount.text,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount (₹)* ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amount.focusLeft && amount.text.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        viewModel.onAmountFocusChange(it.isFocused)
                    },
                singleLine = true
            )

            // --- Category selection dropdown ---
            // Category selection dropdown menu
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.height(IntrinsicSize.Min)){
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // Layer to intercept taps
                Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .clickable { expanded = true },
                color = Color.Transparent,
                ) {}
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ExpenseEntryViewModel.categoryList.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                viewModel.onCategoryChange(cat)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- Notes input (optional, capped at 100 chars) ---
            // Input field for notes
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (Optional, max 100 chars)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // --- Receipt image: pick, show, or remove (mock) ---
            // Receipt image handling
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Receipt Image (Optional)")
                Spacer(Modifier.width(12.dp))
                if (receiptImageUri != null) {
                    // Show the selected image
                    AsyncImage(
                        model = receiptImageUri,
                        contentDescription = "Receipt",
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Remove image button
                    TextButton(onClick = { viewModel.onReceiptImageUriChange(null) }) {
                        Text("Remove")
                    }
                } else {
                    // Pick image button, requesting permission if needed
                    TextButton(onClick = {
                        val permissionStatus = ContextCompat.checkSelfPermission(context, requiredPermission)
                        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                            imagePickerLauncher.launch("image/*")
                        } else {
                            permissionLauncher.launch(requiredPermission)
                        }
                    }) {
                        Text("Pick Image")
                    }

                }
            }

            // --- Submit: Only visible/enabled when required fields have input ---
            // Submit button, only visible when title and amount are filled
            AnimatedVisibility(
                visible = title.text.isNotBlank() && amount.text.isNotBlank(),
                enter = fadeIn()
            ) {
                Button(
                    onClick = { viewModel.onSubmit() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Expense")
                }
            }
        }
    }
}