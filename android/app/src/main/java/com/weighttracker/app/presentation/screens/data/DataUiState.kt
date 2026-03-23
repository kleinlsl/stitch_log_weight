package com.weighttracker.app.presentation.screens.data

data class DataUiState(
    val storageSize: String = "0 MB",
    val lastBackupTime: String? = null,
    val recordCount: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isCleaningUp: Boolean = false,
    val message: String? = null,
    val showClearConfirmDialog: Boolean = false,
    val showRestoreConfirmDialog: Boolean = false,
    val showCleanupConfirmDialog: Boolean = false,
    val restoreFileUri: android.net.Uri? = null
)
