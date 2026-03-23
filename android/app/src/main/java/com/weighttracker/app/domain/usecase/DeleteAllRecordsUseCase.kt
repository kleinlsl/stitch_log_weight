package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.repository.WeightRepository
import javax.inject.Inject

class DeleteAllRecordsUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.deleteAllRecords()
}
