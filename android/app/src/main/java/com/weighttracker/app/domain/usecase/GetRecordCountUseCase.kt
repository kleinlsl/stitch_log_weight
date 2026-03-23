package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetRecordCountUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(): Int = repository.getRecordCount()
}
