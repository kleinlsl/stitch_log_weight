package com.weighttracker.app.presentation.screens.entry

import java.time.LocalDate
import java.time.LocalTime

data class EntryUiState(
    val weight: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val mood: Int? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean
        get() = weight.toDoubleOrNull()?.let { it in 20.0..300.0 } ?: false
}
