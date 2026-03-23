package com.weighttracker.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weighttracker.app.data.local.dao.WeightRecordDao
import com.weighttracker.app.data.local.database.AppDatabase
import com.weighttracker.app.data.local.entity.WeightRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightRecordDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: WeightRecordDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.weightRecordDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetRecord() = runTest {
        val record = WeightRecordEntity(
            weight = 65.5,
            recordDate = "2026-03-23",
            recordTime = "08:30",
            mood = 4
        )
        val id = dao.insert(record)
        assertTrue(id > 0)
    }

    @Test
    fun getAllRecordsOrderedByDateDesc() = runTest {
        dao.insert(WeightRecordEntity(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 66.0, recordDate = "2026-03-23", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:00"))

        val records = dao.getAllRecords().first()
        assertEquals(3, records.size)
        assertEquals("2026-03-23", records[0].recordDate)
        assertEquals("2026-03-22", records[1].recordDate)
        assertEquals("2026-03-21", records[2].recordDate)
    }

    @Test
    fun getRecordByDate() = runTest {
        dao.insert(WeightRecordEntity(weight = 65.0, recordDate = "2026-03-23", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 66.0, recordDate = "2026-03-24", recordTime = "08:00"))

        val record = dao.getRecordByDate("2026-03-23")
        assertNotNull(record)
        assertEquals(65.0, record!!.weight, 0.01)
    }

    @Test
    fun getRecordByDateNotFound() = runTest {
        val record = dao.getRecordByDate("2026-03-23")
        assertNull(record)
    }

    @Test
    fun getRecordsByDateRange() = runTest {
        dao.insert(WeightRecordEntity(weight = 65.0, recordDate = "2026-03-20", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 66.0, recordDate = "2026-03-25", recordTime = "08:00"))

        val records = dao.getRecordsByDateRange("2026-03-21", "2026-03-24").first()
        assertEquals(1, records.size)
        assertEquals("2026-03-22", records[0].recordDate)
    }

    @Test
    fun getLatestRecord() = runTest {
        dao.insert(WeightRecordEntity(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 66.0, recordDate = "2026-03-23", recordTime = "10:00"))
        dao.insert(WeightRecordEntity(weight = 65.5, recordDate = "2026-03-23", recordTime = "08:00"))

        val record = dao.getLatestRecord().first()
        assertNotNull(record)
        assertEquals("2026-03-23", record!!.recordDate)
        assertEquals("10:00", record.recordTime)
        assertEquals(66.0, record.weight, 0.01)
    }

    @Test
    fun updateRecord() = runTest {
        val original = WeightRecordEntity(
            id = 0,
            weight = 65.0,
            recordDate = "2026-03-23",
            recordTime = "08:00"
        )
        val id = dao.insert(original)
        
        val updated = original.copy(id = id, weight = 64.5, updatedAt = System.currentTimeMillis())
        dao.update(updated)

        val record = dao.getRecordByDate("2026-03-23")
        assertEquals(64.5, record!!.weight, 0.01)
    }

    @Test
    fun deleteRecord() = runTest {
        val record = WeightRecordEntity(weight = 65.0, recordDate = "2026-03-23", recordTime = "08:00")
        dao.insert(record)
        
        val retrieved = dao.getRecordByDate("2026-03-23")
        assertNotNull(retrieved)
        
        dao.delete(retrieved!!)
        
        val afterDelete = dao.getRecordByDate("2026-03-23")
        assertNull(afterDelete)
    }

    @Test
    fun deleteAllRecords() = runTest {
        dao.insert(WeightRecordEntity(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 66.0, recordDate = "2026-03-22", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 65.5, recordDate = "2026-03-23", recordTime = "08:00"))

        assertEquals(3, dao.getRecordCount())

        dao.deleteAll()

        assertEquals(0, dao.getRecordCount())
    }

    @Test
    fun getRecordCount() = runTest {
        assertEquals(0, dao.getRecordCount())

        dao.insert(WeightRecordEntity(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"))
        assertEquals(1, dao.getRecordCount())

        dao.insert(WeightRecordEntity(weight = 66.0, recordDate = "2026-03-22", recordTime = "08:00"))
        assertEquals(2, dao.getRecordCount())
    }

    @Test
    fun insertAllRecords() = runTest {
        val records = listOf(
            WeightRecordEntity(weight = 65.0, recordDate = "2026-03-21", recordTime = "08:00"),
            WeightRecordEntity(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:00"),
            WeightRecordEntity(weight = 66.0, recordDate = "2026-03-23", recordTime = "08:00")
        )
        
        dao.insertAll(records)
        
        assertEquals(3, dao.getRecordCount())
    }

    @Test
    fun deleteRecordsBefore() = runTest {
        dao.insert(WeightRecordEntity(weight = 65.0, recordDate = "2026-03-15", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 65.5, recordDate = "2026-03-20", recordTime = "08:00"))
        dao.insert(WeightRecordEntity(weight = 66.0, recordDate = "2026-03-25", recordTime = "08:00"))

        dao.deleteRecordsBefore("2026-03-20")

        val records = dao.getAllRecords().first()
        assertEquals(2, records.size)
        assertTrue(records.none { it.recordDate == "2026-03-15" })
    }

    @Test
    fun insertWithConflictReplaces() = runTest {
        val record1 = WeightRecordEntity(weight = 65.0, recordDate = "2026-03-23", recordTime = "08:00")
        val id1 = dao.insert(record1)
        
        val record2 = WeightRecordEntity(weight = 64.0, recordDate = "2026-03-23", recordTime = "08:00")
        dao.insert(record2)

        val records = dao.getAllRecords().first()
        assertEquals(1, records.size)
        assertEquals(64.0, records[0].weight, 0.01)
    }

    private fun assertTrue(condition: Boolean) {
        if (!condition) throw AssertionError("Expected true but was false")
    }
}
