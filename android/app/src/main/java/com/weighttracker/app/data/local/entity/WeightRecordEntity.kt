package com.weighttracker.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 体重记录实体类
 * 对应数据库中的 weight_records 表
 */
@Entity(
    tableName = "weight_records",
    indices = [Index(value = ["record_date"])]
)
data class WeightRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "weight")
    val weight: Double,  // 公斤，精确到0.1

    @ColumnInfo(name = "record_date")
    val recordDate: String,  // 格式: "2026-03-22"

    @ColumnInfo(name = "record_time")
    val recordTime: String,  // 格式: "08:30"

    @ColumnInfo(name = "mood")
    val mood: Int? = null,  // 1=很差, 2=差, 3=一般, 4=好, 5=很好

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
