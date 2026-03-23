package com.weighttracker.app.data.file

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.weighttracker.app.domain.model.WeightRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor() {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun createBackup(
        records: List<WeightRecord>,
        outputStream: OutputStream
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = BackupData(
                version = "1.0",
                appVersion = "1.0.0",
                exportedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                totalRecords = records.size,
                records = records.map { record ->
                    RecordBackup(
                        weight = record.weight,
                        recordDate = record.recordDate,
                        recordTime = record.recordTime,
                        mood = record.mood,
                        note = record.note
                    )
                }
            )

            gson.toJson(backup, outputStream.writer().buffered())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(inputStream: InputStream): Result<List<WeightRecord>> =
        withContext(Dispatchers.IO) {
            try {
                val backup = gson.fromJson(
                    inputStream.bufferedReader(),
                    BackupData::class.java
                )

                val records = backup.records.map { record ->
                    WeightRecord(
                        weight = record.weight,
                        recordDate = record.recordDate,
                        recordTime = record.recordTime,
                        mood = record.mood,
                        note = record.note
                    )
                }

                Result.success(records)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    data class BackupData(
        val version: String,
        val appVersion: String,
        val exportedAt: String,
        val totalRecords: Int,
        val records: List<RecordBackup>
    )

    data class RecordBackup(
        val weight: Double,
        val recordDate: String,
        val recordTime: String,
        val mood: Int?,
        val note: String?
    )
}
