package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.model.WeightStats
import com.weighttracker.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetWeightStatsUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(records: List<WeightRecord>): WeightStats {
        if (records.isEmpty()) {
            return WeightStats()
        }
        
        val weights = records.map { it.weight }
        return WeightStats(
            average = weights.average(),
            max = weights.maxOrNull() ?: 0.0,
            min = weights.minOrNull() ?: 0.0,
            count = records.size
        )
    }
}
