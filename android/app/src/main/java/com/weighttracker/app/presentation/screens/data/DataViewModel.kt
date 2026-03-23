package com.weighttracker.app.presentation.screens.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val importFromExcelUseCase: ImportFromExcelUseCase
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
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val exportDir = File(context.getExternalFilesDir(null), "exports")
                    if (!exportDir.exists()) {
                        exportDir.mkdirs()
                    }
                    
                    val timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    val fileName = "体重记录_$timestamp.xlsx"
                    val exportFile = File(exportDir, fileName)
                    
                    exportToExcelUseCase(exportFile)
                } catch (e: Exception) {
                    Result.failure<File>(e)
                }
            }
            
            result.fold(
                onSuccess = { file ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            message = "已导出至: ${file.name}"
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

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
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
