package com.weighttracker.app.domain.repository

import com.weighttracker.app.domain.model.WeightRecord
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun getAllRecords(): Flow<List<WeightRecord>>
    suspend fun getRecordByDate(date: String): WeightRecord?
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<WeightRecord>>
    fun getLatestRecord(): Flow<WeightRecord?>
    suspend fun addRecord(record: WeightRecord): Result<Long>
    suspend fun updateRecord(record: WeightRecord): Result<Unit>
    suspend fun deleteRecord(record: WeightRecord): Result<Unit>
    suspend fun deleteAllRecords(): Result<Unit>
    suspend fun getRecordCount(): Int
    suspend fun deleteRecordsBefore(date: String): Result<Unit>
}
