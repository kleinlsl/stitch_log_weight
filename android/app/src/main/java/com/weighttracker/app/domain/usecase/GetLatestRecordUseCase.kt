package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestRecordUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(): Flow<WeightRecord?> = repository.getLatestRecord()
}
