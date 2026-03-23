package com.weighttracker.app.domain.model

data class WeightRecord(
    val id: Long = 0,
    val weight: Double,
    val recordDate: String,
    val recordTime: String,
    val mood: Int? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val weightUnit: String get() = "KG"

    fun isValid(): Boolean {
        return weight in 20.0..300.0
    }
}
