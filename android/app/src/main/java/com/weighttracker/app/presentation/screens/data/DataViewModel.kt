package com.weighttracker.app.presentation.screens.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weighttracker.app.data.file.BackupManager
import com.weighttracker.app.domain.repository.WeightRepository
import com.weighttracker.app.domain.usecase.DeleteAllRecordsUseCase
import com.weighttracker.app.domain.usecase.ExportToExcelUseCase
import com.weighttracker.app.domain.usecase.GetRecordCountUseCase
import com.weighttracker.app.domain.usecase.ImportFromExcelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRecordCountUseCase: GetRecordCountUseCase,
    private val deleteAllRecordsUseCase: DeleteAllRecordsUseCase,
    private val exportToExcelUseCase: ExportToExcelUseCase,
    private val importFromExcelUseCase: ImportFromExcelUseCase,
    private val backupManager: BackupManager,
    private val repository: WeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val count = getRecordCountUseCase()
            val size = estimateStorageSize(count)
            _uiState.update {
                it.copy(
                    recordCount = count,
                    storageSize = size
                )
            }
        }
    }

    fun onExportClick() {
        _uiState.update { it.copy(message = "请选择保存位置") }
    }

    fun onExport(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val filePath = getFilePathFromUri(uri)
                    
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        exportToExcelUseCase.exportToStream(output)
                    } ?: return@withContext Result.failure(Exception("无法打开文件"))
                    
                    Result.success(filePath)
                } catch (e: Exception) {
                    Result.failure<String>(e)
                }
            }
            
            result.fold(
                onSuccess = { filePath ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            message = "已导出成功",
                            exportedFilePath = filePath,
                            showShareDialog = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            message = "导出失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex("_data")
                    if (index >= 0) it.getString(index) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun onImportClick(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            
            val result = withContext(Dispatchers.IO) {
                try {
                    // 先清空现有数据（覆盖模式）
                    deleteAllRecordsUseCase()
                    
                    // 读取并导入新数据
                    val tempFile = File(context.cacheDir, "import_temp.xlsx")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    val importResult = importFromExcelUseCase(tempFile)
                    tempFile.delete()
                    importResult
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            
            result.fold(
                onSuccess = { importResult ->
                    loadData()
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            message = "导入成功：更新 ${importResult.successCount} 条"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            message = "导入失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun onBackupClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackingUp = true) }
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val backupDir = File(context.getExternalFilesDir(null), "backups")
                    if (!backupDir.exists()) {
                        backupDir.mkdirs()
                    }
                    
                    val timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    val backupFile = File(backupDir, "backup_$timestamp.json")
                    
                    Result.success(backupFile)
                } catch (e: Exception) {
                    Result.failure<File>(e)
                }
            }
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isBackingUp = false,
                            lastBackupTime = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            message = "备份成功"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isBackingUp = false,
                            message = "备份失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun onClearDataClick() {
        _uiState.update { it.copy(showClearConfirmDialog = true) }
    }

    fun onConfirmClearData() {
        viewModelScope.launch {
            deleteAllRecordsUseCase()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showClearConfirmDialog = false,
                            recordCount = 0,
                            storageSize = "0 KB",
                            message = "数据已清空"
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            showClearConfirmDialog = false,
                            message = "清空失败"
                        )
                    }
                }
        }
    }

    fun onDismissClearDialog() {
        _uiState.update { it.copy(showClearConfirmDialog = false) }
    }

    fun onRestoreClick(uri: Uri) {
        _uiState.update { 
            it.copy(
                showRestoreConfirmDialog = true,
                restoreFileUri = uri
            ) 
        }
    }

    fun onConfirmRestore() {
        val uri = _uiState.value.restoreFileUri ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, showRestoreConfirmDialog = false) }
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val tempFile = File(context.cacheDir, "restore_temp.json")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    val restoreResult = backupManager.restoreBackup(tempFile.inputStream())
                    tempFile.delete()
                    
                    restoreResult.map { records ->
                        deleteAllRecordsUseCase()
                        records.forEach { record ->
                            repository.addRecord(record)
                        }
                        records.size
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            
            result.fold(
                onSuccess = { count ->
                    loadData()
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            restoreFileUri = null,
                            message = "恢复成功：导入 $count 条记录"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            restoreFileUri = null,
                            message = "恢复失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun onDismissRestoreDialog() {
        _uiState.update { 
            it.copy(
                showRestoreConfirmDialog = false,
                restoreFileUri = null
            ) 
        }
    }

    fun onCleanupClick() {
        _uiState.update { it.copy(showCleanupConfirmDialog = true) }
    }

    fun onConfirmCleanup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCleaningUp = true, showCleanupConfirmDialog = false) }
            
            val oneYearAgo = java.time.LocalDate.now()
                .minusYears(1)
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            
            val result = withContext(Dispatchers.IO) {
                try {
                    repository.deleteRecordsBefore(oneYearAgo)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            
            result.fold(
                onSuccess = {
                    loadData()
                    _uiState.update {
                        it.copy(
                            isCleaningUp = false,
                            message = "已清理 1 年前的数据"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCleaningUp = false,
                            message = "清理失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun onDismissCleanupDialog() {
        _uiState.update { it.copy(showCleanupConfirmDialog = false) }
    }

    fun onMigrationExportClick() {
        _uiState.update { it.copy(message = "请选择保存位置") }
    }

    fun onMigrationExport(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(message = null) }
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val records = repository.getAllRecords().let { flow ->
                        var result: List<com.weighttracker.app.domain.model.WeightRecord>? = null
                        flow.collect { result = it }
                        result ?: emptyList()
                    }
                    
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        backupManager.createBackup(records, output)
                    } ?: return@withContext Result.failure(Exception("无法打开文件"))
                    
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(message = "迁移文件导出成功")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(message = "导出失败: ${error.message}")
                    }
                }
            )
        }
    }

    // Migration Import Flow
    fun onMigrationImport(uri: Uri) {
        _uiState.update {
            it.copy(
                showMigrationImportConfirmDialog = true,
                migrationImportFileUri = uri
            )
        }
    }

    fun onDismissMigrationImportDialog() {
        _uiState.update {
            it.copy(showMigrationImportConfirmDialog = false, migrationImportFileUri = null)
        }
    }

    fun onConfirmMigrationImport() {
        val uri = _uiState.value.migrationImportFileUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMigratingImport = true, showMigrationImportConfirmDialog = false) }

            val result = withContext(Dispatchers.IO) {
                try {
                    val tempFile = File(context.cacheDir, "migration_import.json")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output -> input.copyTo(output) }
                    }

                    val restoreResult = backupManager.restoreBackup(tempFile.inputStream())
                    tempFile.delete()
                    restoreResult.map { records ->
                        deleteAllRecordsUseCase()
                        records.forEach { record -> repository.addRecord(record) }
                        records.size
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            result.fold(
                onSuccess = { count ->
                    loadData()
                    _uiState.update {
                        it.copy(
                            isMigratingImport = false,
                            migrationImportFileUri = null,
                            message = "迁移导入成功：导入 $count 条记录"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isMigratingImport = false,
                            migrationImportFileUri = null,
                            message = "导入失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearExportedFilePath() {
        _uiState.update { it.copy(exportedFilePath = null, showShareDialog = false) }
    }

    fun getShareIntent(): Intent? {
        val filePath = _uiState.value.exportedFilePath ?: return null
        val file = File(filePath)
        if (!file.exists()) return null
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun estimateStorageSize(recordCount: Int): String {
        val bytesPerRecord = 200
        val totalBytes = recordCount * bytesPerRecord
        return when {
            totalBytes < 1024 -> "$totalBytes B"
            else -> "${String.format("%.1f", totalBytes / 1024.0)} KB"
        }
    }
}
