package com.weighttracker.app.data.file

import com.weighttracker.app.domain.model.WeightRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelManager @Inject constructor() {

    suspend fun exportToExcel(
        records: List<WeightRecord>,
        outputStream: OutputStream
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("体重记录")

            val header = sheet.createRow(0)
            val headers = listOf("日期", "时间", "体重(kg)", "心情", "备注")
            headers.forEachIndexed { index, name ->
                header.createCell(index).setCellValue(name)
            }

            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(record.recordDate)
                row.createCell(1).setCellValue(record.recordTime)
                row.createCell(2).setCellValue(record.weight)
                row.createCell(3).setCellValue(record.mood?.toDouble() ?: 0.0)
                row.createCell(4).setCellValue(record.note ?: "")
            }

            sheet.setColumnWidth(0, 15 * 256)
            sheet.setColumnWidth(1, 10 * 256)
            sheet.setColumnWidth(2, 12 * 256)
            sheet.setColumnWidth(3, 8 * 256)
            sheet.setColumnWidth(4, 30 * 256)

            workbook.write(outputStream)
            workbook.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromExcel(inputStream: InputStream): Result<List<WeightRecord>> =
        withContext(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)
                val records = mutableListOf<WeightRecord>()

                val iterator = sheet.iterator()
                var rowIndex = 0

                while (iterator.hasNext()) {
                    val row = iterator.next()
                    if (rowIndex == 0) {
                        rowIndex++
                        continue
                    }

                    val date = getCellStringValue(row.getCell(0))
                    val time = getCellStringValue(row.getCell(1))
                    val weight = row.getCell(2)?.numericCellValue ?: 0.0
                    val mood = row.getCell(3)?.numericCellValue?.toInt()
                    val note = getCellStringValue(row.getCell(4))

                    if (date.isNotBlank() && weight > 0) {
                        records.add(
                            WeightRecord(
                                weight = weight,
                                recordDate = date,
                                recordTime = time.ifBlank { "08:00" },
                                mood = if (mood != null && mood > 0) mood else null,
                                note = note.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                    rowIndex++
                }

                workbook.close()
                Result.success(records)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun getCellStringValue(cell: org.apache.poi.ss.usermodel.Cell?): String {
        return when (cell?.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    val date = cell.localDateTimeCellValue
                    val dateStr = date.toLocalDate().toString()
                    val timeStr = date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    if (dateStr.contains("1899") || dateStr.contains("1900")) {
                        timeStr
                    } else if (timeStr == "00:00") {
                        dateStr
                    } else {
                        dateStr
                    }
                } else {
                    cell.numericCellValue.toString()
                }
            }
            else -> ""
        }
    }
}
