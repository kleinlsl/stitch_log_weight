package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.model.TimeRange
import com.weighttracker.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetWeeklyChangeUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(currentRecords: List<WeightRecord>): Double? {
        if (currentRecords.size < 2) return null
        
        val sorted = currentRecords.sortedByDescending { it.recordDate }
        val latest = sorted.firstOrNull()?.weight ?: return null
        val previous = sorted.getOrNull(1)?.weight ?: return null
        
        return latest - previous
    }
}
