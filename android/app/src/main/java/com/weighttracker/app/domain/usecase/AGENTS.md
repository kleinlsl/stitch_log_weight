# DOMAIN USECASES

**Location:** `android/app/src/main/java/.../domain/usecase/`

## USECASES

| UseCase | Purpose | Dependencies |
|---------|---------|--------------|
| `AddWeightRecordUseCase` | Insert new weight record | WeightRepository |
| `GetWeightRecordsUseCase` | Fetch records (all, by date range) | WeightRepository |
| `GetLatestRecordUseCase` | Get most recent record | WeightRepository |
| `GetWeightStatsUseCase` | Calculate avg/max/min from records | - (pure function) |
| `GetWeeklyChangeUseCase` | Calculate weekly weight change | WeightRepository |
| `GetRecordCountUseCase` | Count total records | WeightRepository |
| `UpdateRecordUseCase` | Update existing record | WeightRepository |
| `DeleteRecordUseCase` | Delete single record | WeightRepository |
| `DeleteAllRecordsUseCase` | Delete all records | WeightRepository |
| `ExportToExcelUseCase` | Export records to .xlsx | ExcelManager |
| `ImportFromExcelUseCase` | Import records from .xlsx | ExcelManager |

## PATTERN

```kotlin
class XxxUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(...): Flow<T> = repository.method(...)
}
```

## REPOSITORY INTERFACE

`WeightRepository` (domain layer) — implemented by `WeightRepositoryImpl` (data layer)

Methods: `getAllRecords()`, `getRecordsByDateRange()`, `getLatestRecord()`, `insert()`, `update()`, `delete()`, `deleteAll()`, `getRecordCount()`
