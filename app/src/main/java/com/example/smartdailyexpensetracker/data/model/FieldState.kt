package com.example.smartdailyexpensetracker.data.model

data class FieldState(
    val text: String = "",
    val focusLeft: Boolean = false,
    val hasBeenFocusedOnce: Boolean = false
)
