package com.weighttracker.app.domain.usecase

import com.weighttracker.app.data.file.ExcelManager
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportToExcelUseCase @Inject constructor(
    private val repository: WeightRepository,
    private val excelManager: ExcelManager
) {
    suspend operator fun invoke(file: File): Result<File> {
        return try {
            val records = repository.getAllRecords().first()
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = "体重记录_$timestamp.xlsx"
            val outputFile = File(file.parentFile, fileName)

            excelManager.exportToExcel(records, FileOutputStream(outputFile))
                .map { outputFile }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
