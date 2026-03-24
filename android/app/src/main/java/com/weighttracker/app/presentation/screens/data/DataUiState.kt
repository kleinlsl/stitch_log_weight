package com.weighttracker.app.presentation.screens.data

data class DataUiState(
    val storageSize: String = "0 MB",
    val lastBackupTime: String? = null,
    val recordCount: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    // Migration import loading state
    val isMigratingImport: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isCleaningUp: Boolean = false,
    val message: String? = null,
    val showClearConfirmDialog: Boolean = false,
    val showRestoreConfirmDialog: Boolean = false,
    val showCleanupConfirmDialog: Boolean = false,
    // Migration import confirmation and URI
    val showMigrationImportConfirmDialog: Boolean = false,
    val migrationImportFileUri: android.net.Uri? = null,
    val restoreFileUri: android.net.Uri? = null,
    val exportedFilePath: String? = null,
    val showShareDialog: Boolean = false
)
