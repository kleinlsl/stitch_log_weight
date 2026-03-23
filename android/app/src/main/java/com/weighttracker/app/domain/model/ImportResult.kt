package com.weighttracker.app.domain.model

data class ImportResult(
    val successCount: Int,
    val failCount: Int,
    val records: List<WeightRecord>
)
