package com.weighttracker.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.weighttracker.app.data.local.dao.WeightRecordDao
import com.weighttracker.app.data.local.entity.WeightRecordEntity

/**
 * 应用数据库
 */
@Database(
    entities = [WeightRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun weightRecordDao(): WeightRecordDao

    companion object {
        const val DATABASE_NAME = "weight_tracker_db"
    }
}
