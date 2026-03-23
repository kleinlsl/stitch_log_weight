package com.weighttracker.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weighttracker.app.data.local.dao.WeightRecordDao
import com.weighttracker.app.data.local.database.AppDatabase
import com.weighttracker.app.data.repository.WeightRepositoryImpl
import com.weighttracker.app.domain.model.WeightRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: WeightRecordDao
    private lateinit var repository: WeightRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.weightRecordDao()
        repository = WeightRepositoryImpl(dao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun addAndGetRecord() = runTest {
        val record = WeightRecord(
            weight = 65.5,
            recordDate = "2026-03-23",
            recordTime = "08:30",
            mood = 4
        )
        
        val result = repository.addRecord(record)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!! > 0)
    }

    @Test
    fun getAllRecords() = runTest {
        repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 66.0, recordDate = "2026-03-23", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:00"))

        val records = repository.getAllRecords().first()
        assertEquals(3, records.size)
    }

    @Test
    fun getLatestRecord() = runTest {
        repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 66.0, recordDate = "2026-03-23", recordTime = "10:00"))
        repository.addRecord(WeightRecord(weight = 65.5, recordDate = "2026-03-23", recordTime = "08:00"))

        val record = repository.getLatestRecord().first()
        assertNotNull(record)
        assertEquals("2026-03-23", record!!.recordDate)
        assertEquals("10:00", record.recordTime)
        assertEquals(66.0, record.weight, 0.01)
    }

    @Test
    fun getRecordsByDateRange() = runTest {
        repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-20", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 66.0, recordDate = "2026-03-25", recordTime = "08:00"))

        val records = repository.getRecordsByDateRange("2026-03-21", "2026-03-24").first()
        assertEquals(1, records.size)
        assertEquals("2026-03-22", records[0].recordDate)
    }

    @Test
    fun updateRecord() = runTest {
        val result = repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-23", recordTime = "08:00"))
        val id = result.getOrNull()!!

        val updated = WeightRecord(
            id = id,
            weight = 64.5,
            recordDate = "2026-03-23",
            recordTime = "08:00"
        )
        val updateResult = repository.updateRecord(updated)
        assertTrue(updateResult.isSuccess)

        val record = repository.getRecordByDate("2026-03-23")
        assertEquals(64.5, record!!.weight, 0.01)
    }

    @Test
    fun deleteRecord() = runTest {
        val result = repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-23", recordTime = "08:00"))
        val id = result.getOrNull()!!

        val record = repository.getRecordByDate("2026-03-23")
        assertNotNull(record)

        val deleteResult = repository.deleteRecord(record!!)
        assertTrue(deleteResult.isSuccess)

        val afterDelete = repository.getRecordByDate("2026-03-23")
        assertNull(afterDelete)
    }

    @Test
    fun deleteAllRecords() = runTest {
        repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 66.0, recordDate = "2026-03-22", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 65.5, recordDate = "2026-03-23", recordTime = "08:00"))

        assertEquals(3, repository.getRecordCount())

        val deleteResult = repository.deleteAllRecords()
        assertTrue(deleteResult.isSuccess)

        assertEquals(0, repository.getRecordCount())
    }

    @Test
    fun getRecordCount() = runTest {
        assertEquals(0, repository.getRecordCount())

        repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        assertEquals(1, repository.getRecordCount())

        repository.addRecord(WeightRecord(weight = 66.0, recordDate = "2026-03-22", recordTime = "08:00"))
        assertEquals(2, repository.getRecordCount())
    }

    @Test
    fun deleteRecordsBefore() = runTest {
        repository.addRecord(WeightRecord(weight = 65.0, recordDate = "2026-03-15", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 65.5, recordDate = "2026-03-20", recordTime = "08:00"))
        repository.addRecord(WeightRecord(weight = 66.0, recordDate = "2026-03-25", recordTime = "08:00"))

        val result = repository.deleteRecordsBefore("2026-03-20")
        assertTrue(result.isSuccess)

        val records = repository.getAllRecords().first()
        assertEquals(2, records.size)
        assertTrue(records.none { it.recordDate == "2026-03-15" })
    }

    @Test
    fun weightRecordIsValid() {
        val validRecord = WeightRecord(weight = 65.0, recordDate = "2026-03-23", recordTime = "08:00")
        assertTrue(validRecord.isValid())

        val tooLight = WeightRecord(weight = 15.0, recordDate = "2026-03-23", recordTime = "08:00")
        assertTrue(!tooLight.isValid())

        val tooHeavy = WeightRecord(weight = 350.0, recordDate = "2026-03-23", recordTime = "08:00")
        assertTrue(!tooHeavy.isValid())
    }

    @Test
    fun weightRecordWithMood() = runTest {
        val record = WeightRecord(
            weight = 65.0,
            recordDate = "2026-03-23",
            recordTime = "08:00",
            mood = 5
        )

        val result = repository.addRecord(record)
        assertTrue(result.isSuccess)

        val retrieved = repository.getRecordByDate("2026-03-23")
        assertNotNull(retrieved)
        assertEquals(5, retrieved!!.mood)
    }
}
