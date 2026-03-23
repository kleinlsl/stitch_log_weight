package com.weighttracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.weighttracker.app.data.local.entity.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 体重记录数据访问对象
 */
@Dao
interface WeightRecordDao {

    /**
     * 插入记录，如果日期已存在则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WeightRecordEntity): Long

    /**
     * 批量插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<WeightRecordEntity>)

    /**
     * 更新记录
     */
    @Update
    suspend fun update(record: WeightRecordEntity)

    /**
     * 删除单条记录
     */
    @Delete
    suspend fun delete(record: WeightRecordEntity)

    /**
     * 删除所有记录
     */
    @Query("DELETE FROM weight_records")
    suspend fun deleteAll()

    /**
     * 获取所有记录，按日期和时间降序排序
     */
    @Query("SELECT * FROM weight_records ORDER BY record_date DESC, record_time DESC")
    fun getAllRecords(): Flow<List<WeightRecordEntity>>

    /**
     * 根据日期获取记录
     */
    @Query("SELECT * FROM weight_records WHERE record_date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): WeightRecordEntity?

    /**
     * 根据日期范围获取记录，按日期升序排序
     */
    @Query("SELECT * FROM weight_records WHERE record_date BETWEEN :startDate AND :endDate ORDER BY record_date ASC, record_time ASC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<WeightRecordEntity>>

    /**
     * 获取最新记录
     */
    @Query("SELECT * FROM weight_records ORDER BY record_date DESC, record_time DESC LIMIT 1")
    fun getLatestRecord(): Flow<WeightRecordEntity?>

    /**
     * 获取记录总数
     */
    @Query("SELECT COUNT(*) FROM weight_records")
    suspend fun getRecordCount(): Int

    /**
     * 删除指定天数之前的记录
     */
    @Query("DELETE FROM weight_records WHERE record_date < :date")
    suspend fun deleteRecordsBefore(date: String)
}
