package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import javax.inject.Inject

class AddWeightRecordUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(record: WeightRecord): Result<Long> {
        if (!record.isValid()) {
            return Result.failure(IllegalArgumentException("体重必须在20-300kg之间"))
        }
        return repository.addRecord(record)
    }
}
