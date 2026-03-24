# DATA LAYER

**Location:** `android/app/src/main/java/.../data/`

## STRUCTURE

```
data/
├── local/
│   ├── dao/WeightRecordDao.kt        # Room DAO
│   ├── database/AppDatabase.kt       # Room database
│   └── entity/WeightRecordEntity.kt  # Room entity
├── repository/WeightRepositoryImpl.kt # Repository impl
└── file/
    ├── ExcelManager.kt               # Excel import/export (Apache POI)
    └── BackupManager.kt              # JSON backup/restore (Gson)
```

## KEY CLASSES

| Class | Role |
|-------|------|
| `WeightRecordEntity` | Room entity, maps to `weight_records` table |
| `WeightRecordDao` | DAO with queries: `getAll`, `getByDateRange`, `insert`, `update`, `delete` |
| `AppDatabase` | Room database, singleton, version 1 |
| `WeightRepositoryImpl` | Implements domain `WeightRepository` interface |
| `ExcelManager` | Handles .xlsx read/write using Apache POI |
| `BackupManager` | JSON serialization using Gson |

## ROOM CONVENTIONS

- Entity: `@Entity(tableName = "...")` with auto-generated ID
- DAO: `@Dao` interface returning `Flow<List<T>>` for queries
- Database: `@Database(entities = [...], version = 1)` with fallback to destructive migration

## DI

Provided via `DatabaseModule.kt` and `RepositoryModule.kt` in `di/`
