package com.weighttracker.app.data.repository

import com.weighttracker.app.data.local.dao.WeightRecordDao
import com.weighttracker.app.data.local.entity.WeightRecordEntity
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeightRepositoryImplTest {

    private lateinit var dao: WeightRecordDao
    private lateinit var repository: WeightRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = WeightRepositoryImpl(dao)
    }

    @Test
    fun `should add record successfully`() = runTest {
        val record = WeightRecord(
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        coEvery { dao.insert(any()) } returns 1L

        val result = repository.addRecord(record)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `should get all records`() = runTest {
        val entities = listOf(
            WeightRecordEntity(1, 65.5, "2026-03-22", "08:30"),
            WeightRecordEntity(2, 66.0, "2026-03-21", "09:00")
        )
        coEvery { dao.getAllRecords() } returns flowOf(entities)

        val records = repository.getAllRecords().first()

        assertEquals(2, records.size)
        assertEquals(65.5, records[0].weight, 0.01)
    }

    @Test
    fun `should get latest record`() = runTest {
        val entity = WeightRecordEntity(1, 72.5, "2026-03-22", "08:30")
        coEvery { dao.getLatestRecord() } returns flowOf(entity)

        val record = repository.getLatestRecord().first()

        assertEquals(72.5, record?.weight ?: 0.0, 0.01)
    }

    @Test
    fun `should delete all records`() = runTest {
        coEvery { dao.deleteAll() } returns Unit

        val result = repository.deleteAllRecords()

        assertTrue(result.isSuccess)
        coVerify { dao.deleteAll() }
    }

    @Test
    fun `should get record count`() = runTest {
        coEvery { dao.getRecordCount() } returns 156

        val count = repository.getRecordCount()

        assertEquals(156, count)
    }
}
