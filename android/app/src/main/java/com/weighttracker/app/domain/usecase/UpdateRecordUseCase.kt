package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import javax.inject.Inject

class UpdateRecordUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(record: WeightRecord): Result<Unit> {
        return repository.updateRecord(record)
    }
}
