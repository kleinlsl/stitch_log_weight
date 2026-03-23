package com.weighttracker.app.data.repository

import com.weighttracker.app.data.local.dao.WeightRecordDao
import com.weighttracker.app.data.local.entity.WeightRecordEntity
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 体重记录仓库实现类
 */
class WeightRepositoryImpl @Inject constructor(
    private val dao: WeightRecordDao
) : WeightRepository {

    override fun getAllRecords(): Flow<List<WeightRecord>> {
        return dao.getAllRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecordByDate(date: String): WeightRecord? {
        return dao.getRecordByDate(date)?.toDomain()
    }

    override fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<WeightRecord>> {
        return dao.getRecordsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLatestRecord(): Flow<WeightRecord?> {
        return dao.getLatestRecord().map { it?.toDomain() }
    }

    override suspend fun addRecord(record: WeightRecord): Result<Long> {
        return try {
            val entity = record.toEntity()
            val id = dao.insert(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRecord(record: WeightRecord): Result<Unit> {
        return try {
            val entity = record.toEntity()
            dao.update(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecord(record: WeightRecord): Result<Unit> {
        return try {
            val entity = record.toEntity()
            dao.delete(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllRecords(): Result<Unit> {
        return try {
            dao.deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecordCount(): Int {
        return dao.getRecordCount()
    }

    override suspend fun deleteRecordsBefore(date: String): Result<Unit> {
        return try {
            dao.deleteRecordsBefore(date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Extension functions for mapping
    private fun WeightRecordEntity.toDomain(): WeightRecord = WeightRecord(
        id = id,
        weight = weight,
        recordDate = recordDate,
        recordTime = recordTime,
        mood = mood,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun WeightRecord.toEntity(): WeightRecordEntity = WeightRecordEntity(
        id = id,
        weight = weight,
        recordDate = recordDate,
        recordTime = recordTime,
        mood = mood,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
