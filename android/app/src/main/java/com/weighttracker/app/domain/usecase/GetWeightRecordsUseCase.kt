package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeightRecordsUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(): Flow<List<WeightRecord>> = repository.getAllRecords()
    
    fun byDateRange(startDate: String, endDate: String): Flow<List<WeightRecord>> =
        repository.getRecordsByDateRange(startDate, endDate)
}
