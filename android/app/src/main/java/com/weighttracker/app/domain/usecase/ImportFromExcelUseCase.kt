package com.weighttracker.app.domain.usecase

import com.weighttracker.app.data.file.ExcelManager
import com.weighttracker.app.domain.model.ImportResult
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class ImportFromExcelUseCase @Inject constructor(
    private val repository: WeightRepository,
    private val excelManager: ExcelManager
) {
    suspend operator fun invoke(file: File): Result<ImportResult> {
        return try {
            val records = excelManager.importFromExcel(FileInputStream(file)).getOrNull() ?: emptyList()

            var successCount = 0
            var updateCount = 0
            var insertCount = 0
            var failCount = 0

            records.forEach { record ->
                val existing = repository.getRecordByDate(record.recordDate)
                if (existing != null) {
                    repository.updateRecord(record.copy(id = existing.id))
                        .onSuccess { 
                            successCount++
                            updateCount++
                        }
                        .onFailure { failCount++ }
                } else {
                    repository.addRecord(record)
                        .onSuccess { 
                            successCount++
                            insertCount++
                        }
                        .onFailure { failCount++ }
                }
            }

            Result.success(ImportResult(
                successCount = successCount,
                failCount = failCount,
                records = records.take(successCount)
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
