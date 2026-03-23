# 自动化测试文档

**Project:** 体重记录 App  
**Version:** 1.0  
**Date:** 2026-03-22  
**Based on:** PRD + UI设计稿 + 技术方案  

---

## 1. 测试概述

### 1.1 测试金字塔

```
                    ┌─────────────┐
                    │   E2E Tests  │  ← 关键用户流程
                    │   (少量)      │
                    └─────────────┘
                 ┌───────────────────┐
                 │  Integration Tests  │  ← DAO, Repository
                 │   (中量)          │
                 └───────────────────┘
              ┌─────────────────────────┐
              │     Unit Tests         │  ← UseCase, ViewModel
              │     (大量)             │
              └─────────────────────────┘
```

### 1.2 测试覆盖率目标

| 类型 | 覆盖率目标 | 工具 |
|------|-------------|------|
| Unit Tests | 80%+ | JUnit 5, MockK |
| Integration Tests | 70%+ | Room Test, Truth |
| UI Tests | 关键流程 | Compose UI Testing |
| E2E Tests | 核心功能 | Espresso |

---

## 2. 单元测试 (Unit Tests)

### 2.1 UseCase 测试

#### 2.1.1 AddWeightRecordUseCaseTest

```kotlin
// test/java/com/weighttracker/domain/usecase/AddWeightRecordUseCaseTest.kt

@ExtendWith(MockKExtension::class)
class AddWeightRecordUseCaseTest {

    @MockK
    private lateinit var repository: WeightRepository

    @InjectMockKs
    private lateinit var useCase: AddWeightRecordUseCase

    @Test
    fun `should save weight record successfully`() = runTest {
        // Given
        val record = WeightRecord(
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30",
            mood = 3,
            note = "今天感觉不错"
        )
        every { repository.addRecord(record) } returns Result.success(1L)

        // When
        val result = useCase(record)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1L)
        verify { repository.addRecord(record) }
    }

    @Test
    fun `should return failure when weight is invalid`() = runTest {
        // Given - 超出范围
        val record = WeightRecord(
            weight = 999.0,  // 无效体重
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        every { repository.addRecord(record) } returns Result.failure(
            IllegalArgumentException("体重必须在20-300kg之间")
        )

        // When
        val result = useCase(record)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should return failure when date is in future`() = runTest {
        // Given - 未来日期
        val futureDate = LocalDate.now().plusDays(1).toString()
        val record = WeightRecord(
            weight = 65.5,
            recordDate = futureDate,
            recordTime = "08:30"
        )
        every { repository.addRecord(record) } returns Result.failure(
            IllegalArgumentException("记录日期不能是未来")
        )

        // When
        val result = useCase(record)

        // Then
        assertThat(result.isFailure).isTrue()
    }
}
```

#### 2.1.2 GetWeightStatsUseCaseTest

```kotlin
// test/java/com/weighttracker/domain/usecase/GetWeightStatsUseCaseTest.kt

@ExtendWith(MockKExtension::class)
class GetWeightStatsUseCaseTest {

    @MockK
    private lateinit var repository: WeightRepository

    @InjectMockKs
    private lateinit var useCase: GetWeightStatsUseCase

    @Test
    fun `should calculate correct stats for week`() = runTest {
        // Given - 7天数据
        val records = listOf(
            WeightRecord(weight = 70.0, recordDate = "2026-03-15"),
            WeightRecord(weight = 69.5, recordDate = "2026-03-16"),
            WeightRecord(weight = 69.0, recordDate = "2026-03-17"),
            WeightRecord(weight = 68.8, recordDate = "2026-03-18"),
            WeightRecord(weight = 68.5, recordDate = "2026-03-19"),
            WeightRecord(weight = 68.2, recordDate = "2026-03-20"),
            WeightRecord(weight = 68.0, recordDate = "2026-03-21")
        )
        every { repository.getRecordsByDateRange(any(), any()) } returns flowOf(records)

        // When
        val stats = useCase(DateRange.WEEK)

        // Then
        assertThat(stats.average).isEqualTo(69.0)
        assertThat(stats.max).isEqualTo(70.0)
        assertThat(stats.min).isEqualTo(68.0)
        assertThat(stats.count).isEqualTo(7)
    }

    @Test
    fun `should return empty stats when no records`() = runTest {
        // Given
        every { repository.getRecordsByDateRange(any(), any()) } returns flowOf(emptyList())

        // When
        val stats = useCase(DateRange.WEEK)

        // Then
        assertThat(stats.average).isEqualTo(0.0)
        assertThat(stats.max).isEqualTo(0.0)
        assertThat(stats.min).isEqualTo(0.0)
        assertThat(stats.count).isEqualTo(0)
    }
}
```

#### 2.1.3 ExportToExcelUseCaseTest

```kotlin
// test/java/com/weighttracker/domain/usecase/ExportToExcelUseCaseTest.kt

@ExtendWith(MockKExtension::class)
class ExportToExcelUseCaseTest {

    @MockK
    private lateinit var repository: WeightRepository
    
    @MockK
    private lateinit var excelManager: ExcelManager

    @InjectMockKs
    private lateinit var useCase: ExportToExcelUseCase

    @Test
    fun `should export all records to excel successfully`() = runTest {
        // Given
        val records = listOf(
            WeightRecord(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:30"),
            WeightRecord(weight = 65.8, recordDate = "2026-03-21", recordTime = "08:00")
        )
        val tempFile = File.createTempFile("test_export", ".xlsx")
        
        every { repository.getAllRecords() } returns flowOf(records)
        every { 
            excelManager.exportToExcel(eq(records), any<OutputStream>()) 
        } returns Result.success(Unit)

        // When
        val result = useCase(tempFile)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(tempFile.exists()).isTrue()
        assertThat(tempFile.length()).isGreaterThan(0L)
        
        tempFile.delete()
    }

    @Test
    fun `should return failure when excel write fails`() = runTest {
        // Given
        val tempFile = File.createTempFile("test_export", ".xlsx")
        every { repository.getAllRecords() } returns flowOf(emptyList())
        every { excelManager.exportToExcel(any(), any()) } returns Result.failure(
            IOException("磁盘空间不足")
        )

        // When
        val result = useCase(tempFile)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        
        tempFile.delete()
    }
}
```

### 2.2 ViewModel 测试

#### 2.2.1 HomeViewModelTest

```kotlin
// test/java/com/weighttracker/presentation/screens/home/HomeViewModelTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @MockK
    private lateinit var getLatestRecordUseCase: GetLatestRecordUseCase
    
    @MockK
    private lateinit var getWeeklyChangeUseCase: GetWeeklyChangeUseCase

    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = HomeViewModel(
            getLatestRecordUseCase = getLatestRecordUseCase,
            getWeeklyChangeUseCase = getWeeklyChangeUseCase
        )
    }

    @Test
    fun `should load latest record on init`() = runTest {
        // Given
        val record = WeightRecord(
            weight = 72.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        every { getLatestRecordUseCase() } returns flowOf(record)
        every { getWeeklyChangeUseCase() } returns flowOf(-0.8)

        // When
        viewModel = HomeViewModel(
            getLatestRecordUseCase = getLatestRecordUseCase,
            getWeeklyChangeUseCase = getWeeklyChangeUseCase
        )

        // Then
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertThat(state.latestRecord?.weight).isEqualTo(72.5)
                assertThat(state.weeklyChange).isEqualTo(-0.8)
                assertThat(state.isLoading).isFalse()
            }
        }
    }

    @Test
    fun `should show empty state when no records`() = runTest {
        // Given
        every { getLatestRecordUseCase() } returns flowOf(null)
        every { getWeeklyChangeUseCase() } returns flowOf(null)

        // When
        viewModel = HomeViewModel(
            getLatestRecordUseCase = getLatestRecordUseCase,
            getWeeklyChangeUseCase = getWeeklyChangeUseCase
        )

        // Then
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertThat(state.latestRecord).isNull()
                assertThat(state.isLoading).isFalse()
            }
        }
    }

    @Test
    fun `should show error when loading fails`() = runTest {
        // Given
        every { getLatestRecordUseCase() } returns flow { throw RuntimeException("DB Error") }
        every { getWeeklyChangeUseCase() } returns flowOf(null)

        // When
        viewModel = HomeViewModel(
            getLatestRecordUseCase = getLatestRecordUseCase,
            getWeeklyChangeUseCase = getWeeklyChangeUseCase
        )

        // Then
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertThat(state.error).isNotNull()
            }
        }
    }
}
```

#### 2.2.2 EntryViewModelTest

```kotlin
// test/java/com/weighttracker/presentation/screens/entry/EntryViewModelTest.kt

class EntryViewModelTest {

    @MockK
    private lateinit var addWeightRecordUseCase: AddWeightRecordUseCase

    private lateinit var viewModel: EntryViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = EntryViewModel(addWeightRecordUseCase)
    }

    @Test
    fun `should update weight when input changes`() {
        // When
        viewModel.onWeightChange("65.5")

        // Then
        assertThat(viewModel.uiState.value.weight).isEqualTo("65.5")
    }

    @Test
    fun `should update mood when selected`() {
        // When
        viewModel.onMoodSelected(3)

        // Then
        assertThat(viewModel.uiState.value.mood).isEqualTo(3)
    }

    @Test
    fun `should enable save button when weight is valid`() {
        // Given
        viewModel.onWeightChange("65.5")

        // Then
        assertThat(viewModel.uiState.value.canSave).isTrue()
    }

    @Test
    fun `should disable save button when weight is empty`() {
        // When
        viewModel.onWeightChange("")

        // Then
        assertThat(viewModel.uiState.value.canSave).isFalse()
    }

    @Test
    fun `should disable save button when weight is invalid`() {
        // When - 无效体重
        viewModel.onWeightChange("abc")

        // Then
        assertThat(viewModel.uiState.value.canSave).isFalse()
    }

    @Test
    fun `should save record successfully`() = runTest {
        // Given
        viewModel.onWeightChange("65.5")
        viewModel.onMoodSelected(4)
        viewModel.onNoteChange("感觉很好")
        
        every { 
            addWeightRecordUseCase(any<WeightRecord>()) 
        } returns Result.success(1L)

        // When
        viewModel.saveRecord()

        // Then
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertThat(state.saveSuccess).isTrue()
                assertThat(state.isSaving).isFalse()
            }
        }
        
        verify { addWeightRecordUseCase(any()) }
    }

    @Test
    fun `should show error when save fails`() = runTest {
        // Given
        viewModel.onWeightChange("65.5")
        every { 
            addWeightRecordUseCase(any()) 
        } returns Result.failure(RuntimeException("保存失败"))

        // When
        viewModel.saveRecord()

        // Then
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertThat(state.error).isNotNull()
                assertThat(state.saveSuccess).isFalse()
            }
        }
    }
}
```

### 2.3 Repository 测试

```kotlin
// test/java/com/weighttracker/data/repository/WeightRepositoryImplTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class WeightRepositoryImplTest {

    @MockK
    private lateinit var dao: WeightRecordDao

    private lateinit var repository: WeightRepositoryImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        repository = WeightRepositoryImpl(dao)
    }

    @Test
    fun `should add record successfully`() = runTest {
        // Given
        val record = WeightRecordEntity(
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        every { dao.insert(record) } returns 1L

        // When
        val result = repository.addRecord(record.toDomain())

        // Then
        assertThat(result.getOrNull()).isEqualTo(1L)
    }

    @Test
    fun `should get all records ordered by date desc`() = runTest {
        // Given
        val entities = listOf(
            WeightRecordEntity(1, 65.5, "2026-03-22", "08:30"),
            WeightRecordEntity(2, 66.0, "2026-03-21", "09:00")
        )
        every { dao.getAllRecords() } returns flowOf(entities)

        // When
        val records = repository.getAllRecords().first()

        // Then
        assertThat(records).hasSize(2)
        assertThat(records[0].recordDate).isEqualTo("2026-03-22")
    }

    @Test
    fun `should delete all records`() = runTest {
        // Given
        every { dao.deleteAll() } just runs

        // When
        val result = repository.deleteAllRecords()

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { dao.deleteAll() }
    }

    @Test
    fun `should get record count`() = runTest {
        // Given
        every { dao.getRecordCount() } returns 156

        // When
        val count = repository.getRecordCount()

        // Then
        assertThat(count).isEqualTo(156)
    }
}
```

---

## 3. 集成测试 (Integration Tests)

### 3.1 Room Database 测试

```kotlin
// test/java/com/weighttracker/data/local/database/AppDatabaseTest.kt

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
class AppDatabaseTest {

    private lateinit var dao: WeightRecordDao
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()  // 仅用于测试
            .build()
        dao = db.weightRecordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should insert and retrieve record`() {
        // Given
        val entity = WeightRecordEntity(
            id = 0,
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30",
            mood = 3,
            note = "测试"
        )

        // When
        val id = dao.insert(entity)
        val retrieved = dao.getRecordByDate("2026-03-22")

        // Then
        assertThat(id).isGreaterThan(0L)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.weight).isEqualTo(65.5)
        assertThat(retrieved?.mood).isEqualTo(3)
    }

    @Test
    fun `should get records by date range`() {
        // Given
        val records = listOf(
            WeightRecordEntity(0, 70.0, "2026-03-15", "08:00"),
            WeightRecordEntity(0, 69.5, "2026-03-16", "08:00"),
            WeightRecordEntity(0, 69.0, "2026-03-17", "08:00"),
            WeightRecordEntity(0, 68.5, "2026-03-18", "08:00"),
            WeightRecordEntity(0, 68.0, "2026-03-22", "08:00")  // 不在范围内
        )
        records.forEach { dao.insert(it) }

        // When
        val result = dao.getRecordsByDateRange("2026-03-15", "2026-03-18").first()

        // Then
        assertThat(result).hasSize(4)
        assertThat(result.map { it.recordDate }).containsExactly(
            "2026-03-15", "2026-03-16", "2026-03-17", "2026-03-18"
        )
    }

    @Test
    fun `should get latest record`() {
        // Given
        dao.insert(WeightRecordEntity(0, 65.5, "2026-03-20", "08:00"))
        dao.insert(WeightRecordEntity(0, 65.8, "2026-03-21", "09:00"))
        dao.insert(WeightRecordEntity(0, 66.0, "2026-03-22", "08:30"))  // 最新

        // When
        val latest = dao.getLatestRecord().first()

        // Then
        assertThat(latest?.recordDate).isEqualTo("2026-03-22")
        assertThat(latest?.weight).isEqualTo(66.0)
    }

    @Test
    fun `should delete all records`() {
        // Given
        dao.insert(WeightRecordEntity(0, 65.5, "2026-03-22", "08:30"))
        dao.insert(WeightRecordEntity(0, 66.0, "2026-03-21", "08:30"))

        // When
        dao.deleteAll()

        // Then
        val count = dao.getRecordCount()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `should replace on conflict`() {
        // Given - 同一日期已有记录
        dao.insert(WeightRecordEntity(0, 65.5, "2026-03-22", "08:30"))

        // When - 插入新记录
        val newRecord = WeightRecordEntity(0, 66.0, "2026-03-22", "09:00")
        dao.insert(newRecord)

        // Then - 应该只有一条记录，体重更新
        val records = dao.getAllRecords().first()
        assertThat(records).hasSize(1)
        assertThat(records[0].weight).isEqualTo(66.0)
    }
}
```

### 3.2 Excel Manager 测试

```kotlin
// test/java/com/weighttracker/data/file/ExcelManagerTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class ExcelManagerTest {

    private lateinit var excelManager: ExcelManager
    private lateinit var tempDir: File

    @BeforeEach
    fun setup() {
        excelManager = ExcelManager()
        tempDir = createTempDir()
    }

    @Test
    fun `should export records to excel file`() = runTest {
        // Given
        val records = listOf(
            WeightRecord(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:30", mood = 3, note = "测试"),
            WeightRecord(weight = 66.0, recordDate = "2026-03-21", recordTime = "08:00")
        )
        val outputFile = File(tempDir, "export.xlsx")

        // When
        val result = excelManager.exportToExcel(records, outputFile.outputStream())

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(outputFile.exists()).isTrue()
        assertThat(outputFile.length()).isGreaterThan(0L)

        // 验证文件内容
        val workbook = WorkbookFactory.create(outputFile)
        val sheet = workbook.getSheetAt(0)
        
        // 表头
        assertThat(sheet.getRow(0).getCell(0).stringCellValue).isEqualTo("日期")
        assertThat(sheet.getRow(0).getCell(2).stringCellValue).isEqualTo("体重(kg)")
        
        // 数据
        assertThat(sheet.getRow(1).getCell(0).stringCellValue).isEqualTo("2026-03-22")
        assertThat(sheet.getRow(1).getCell(2).numericCellValue).isEqualTo(65.5)
        
        workbook.close()
    }

    @Test
    fun `should import records from excel file`() = runTest {
        // Given - 创建测试Excel文件
        val inputFile = File(tempDir, "import.xlsx")
        createTestExcel(inputFile)

        // When
        val result = excelManager.importFromExcel(inputFile.inputStream())

        // Then
        assertThat(result.isSuccess).isTrue()
        val records = result.getOrNull()!!
        assertThat(records).hasSize(2)
        assertThat(records[0].weight).isEqualTo(65.5)
        assertThat(records[0].recordDate).isEqualTo("2026-03-22")
    }

    @Test
    fun `should handle empty excel file`() = runTest {
        // Given
        val inputFile = File(tempDir, "empty.xlsx")
        createEmptyExcel(inputFile)

        // When
        val result = excelManager.importFromExcel(inputFile.inputStream())

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `should return failure for invalid file`() = runTest {
        // Given - 非Excel文件
        val invalidFile = File(tempDir, "invalid.txt")
        invalidFile.writeText("not an excel file")

        // When
        val result = excelManager.importFromExcel(invalidFile.inputStream())

        // Then
        assertThat(result.isFailure).isTrue()
    }

    private fun createTestExcel(file: File) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("体重记录")
        
        // 表头
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("日期")
        header.createCell(1).setCellValue("时间")
        header.createCell(2).setCellValue("体重(kg)")
        
        // 数据行1
        val row1 = sheet.createRow(1)
        row1.createCell(0).setCellValue("2026-03-22")
        row1.createCell(1).setCellValue("08:30")
        row1.createCell(2).setCellValue(65.5)
        
        // 数据行2
        val row2 = sheet.createRow(2)
        row2.createCell(0).setCellValue("2026-03-21")
        row2.createCell(1).setCellValue("08:00")
        row2.createCell(2).setCellValue(66.0)
        
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
    }
}
```

---

## 4. UI 测试 (Compose UI Tests)

### 4.1 HomeScreen 测试

```kotlin
// test/java/com/weighttracker/presentation/screens/home/HomeScreenTest.kt

@ComposeTestRules
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeTestRule()

    @Test
    fun `should display current weight correctly`() {
        // Given
        val uiState = HomeUiState(
            latestRecord = WeightRecord(
                weight = 72.5,
                recordDate = "2026-03-22",
                recordTime = "08:30"
            ),
            weeklyChange = -0.8,
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            HomeScreen(
                uiState = uiState,
                onFabClick = {}
            )
        }

        // Then - UI稿对应: dashboard/code.html Hero区域
        composeTestRule.onNodeWithText("72.5")
            .assertExists()
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("KG")
            .assertExists()
        
        composeTestRule.onNodeWithText("-0.8 KG")
            .assertExists()
    }

    @Test
    fun `should display weekly trend indicator when weight decreased`() {
        // Given
        val uiState = HomeUiState(
            latestRecord = WeightRecord(weight = 72.5),
            weeklyChange = -0.8,  // 下降
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            HomeScreen(
                uiState = uiState,
                onFabClick = {}
            )
        }

        // Then - 验证下降指示器显示绿色
        composeTestRule.onNodeWithText("-0.8 KG")
            .assertTextColor(Color(0xFF059669))  // 绿色
    }

    @Test
    fun `should display BMI card with correct values`() {
        // Given - UI稿对应: dashboard/code.html BMI卡片
        val uiState = HomeUiState(
            latestRecord = WeightRecord(weight = 72.5),
            bmi = 22.4,
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            HomeScreen(
                uiState = uiState,
                onFabClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("BMI 指数")
            .assertExists()
        composeTestRule.onNodeWithText("22.4")
            .assertExists()
        composeTestRule.onNodeWithText("正常范围")
            .assertExists()
    }

    @Test
    fun `should display goal progress card`() {
        // Given - UI稿对应: dashboard/code.html 目标进度卡片
        val uiState = HomeUiState(
            goalWeight = 70.0,
            goalProgress = 0.7f,  // 70%
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            HomeScreen(
                uiState = uiState,
                onFabClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("目标进度")
            .assertExists()
        composeTestRule.onNodeWithText("70%")
            .assertExists()
        composeTestRule.onNodeWithText("距 70.0 KG")
            .assertExists()
    }

    @Test
    fun `should navigate to EntryScreen when FAB clicked`() {
        // Given
        var navigated = false
        val uiState = HomeUiState(isLoading = false)

        // When
        composeTestRule.setContent {
            HomeScreen(
                uiState = uiState,
                onFabClick = { navigated = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("添加")
            .performClick()

        // Then
        assertThat(navigated).isTrue()
    }

    @Test
    fun `should show loading state`() {
        // Given
        val uiState = HomeUiState(isLoading = true)

        // When
        composeTestRule.setContent {
            HomeScreen(
                uiState = uiState,
                onFabClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("72.5")
            .assertDoesNotExist()
    }
}
```

### 4.2 EntryScreen 测试

```kotlin
// test/java/com/weighttracker/presentation/screens/entry/EntryScreenTest.kt

@ComposeTestRules
class EntryScreenTest {

    @get:Rule
    val composeTestRule = createComposeTestRule()

    @Test
    fun `should display weight input with placeholder`() {
        // Given - UI稿对应: log_weight/code.html 体重输入区域
        val uiState = EntryUiState()

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("00.0")  // placeholder
            .assertExists()
        composeTestRule.onNodeWithText("KG")
            .assertExists()
    }

    @Test
    fun `should update weight when input changes`() {
        // Given
        var capturedWeight = ""
        val uiState = EntryUiState()

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = { capturedWeight = it },
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        composeTestRule.onNodeWithText("00.0")
            .performTextReplacement("65.5")

        // Then
        assertThat(capturedWeight).isEqualTo("65.5")
    }

    @Test
    fun `should display mood selector with 5 options`() {
        // Given - UI稿对应: log_weight/code.html 心情选择
        val uiState = EntryUiState()
        var selectedMood: Int? = null

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = { selectedMood = it },
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then - 应该有5个心情选项
        composeTestRule.onAllNodesWithContentDescription("心情选项")
            .assertCountEquals(5)
    }

    @Test
    fun `should highlight selected mood`() {
        // Given - 选择第3个心情
        val uiState = EntryUiState(mood = 3)

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then - 选中的心情应该有高亮背景
        composeTestRule.onNodeWithText("sentiment_satisfied")
            .assertBackgroundColor(Color(0xFFD3E3FD))  // primary container
    }

    @Test
    fun `should display date and time pickers`() {
        // Given - UI稿对应: log_weight/code.html 日期时间选择
        val uiState = EntryUiState(
            date = LocalDate.of(2023, 10, 27),
            time = LocalTime.of(8, 30)
        )

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("2023-10-27")
            .assertExists()
        composeTestRule.onNodeWithText("08:30")
            .assertExists()
    }

    @Test
    fun `should display note input`() {
        // Given - UI稿对应: log_weight/code.html 备注区域
        val uiState = EntryUiState()

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("备注")
            .assertExists()
    }

    @Test
    fun `should enable save button when weight is valid`() {
        // Given
        val uiState = EntryUiState(weight = "65.5")

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then - UI稿对应: log_weight/code.html 保存按钮
        composeTestRule.onNodeWithText("保存记录")
            .assertIsEnabled()
    }

    @Test
    fun `should disable save button when weight is empty`() {
        // Given
        val uiState = EntryUiState(weight = "")

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("保存记录")
            .assertIsNotEnabled()
    }

    @Test
    fun `should show loading when saving`() {
        // Given - UI稿对应: log_weight/code.html 保存按钮加载状态
        val uiState = EntryUiState(weight = "65.5", isSaving = true)

        // When
        composeTestRule.setContent {
            EntryScreen(
                uiState = uiState,
                onWeightChange = {},
                onDateChange = {},
                onTimeChange = {},
                onMoodSelected = {},
                onNoteChange = {},
                onSaveClick = {},
                onClose = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("保存记录")
            .assertIsNotEnabled()
    }
}
```

### 4.3 TrendsScreen 测试

```kotlin
// test/java/com/weighttracker/presentation/screens/trends/TrendsScreenTest.kt

@ComposeTestRules
class TrendsScreenTest {

    @get:Rule
    val composeTestRule = createComposeTestRule()

    @Test
    fun `should display time range selector`() {
        // Given - UI稿对应: trends/code.html 时间范围切换
        val uiState = TrendsUiState(timeRange = TimeRange.WEEK)

        // When
        composeTestRule.setContent {
            TrendsScreen(
                uiState = uiState,
                onTimeRangeChange = {},
                onViewAllClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("周")
            .assertExists()
        composeTestRule.onNodeWithText("月")
            .assertExists()
        composeTestRule.onNodeWithText("年")
            .assertExists()
    }

    @Test
    fun `should display trend chart with data`() {
        // Given - UI稿对应: trends/code.html 折线图
        val records = listOf(
            WeightRecord(weight = 70.0, recordDate = "2026-03-15"),
            WeightRecord(weight = 69.5, recordDate = "2026-03-16"),
            WeightRecord(weight = 69.0, recordDate = "2026-03-17"),
            WeightRecord(weight = 68.8, recordDate = "2026-03-18"),
            WeightRecord(weight = 68.5, recordDate = "2026-03-19"),
            WeightRecord(weight = 68.2, recordDate = "2026-03-20"),
            WeightRecord(weight = 68.0, recordDate = "2026-03-21")
        )
        val uiState = TrendsUiState(
            records = records,
            averageWeight = 68.8,
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TrendsScreen(
                uiState = uiState,
                onTimeRangeChange = {},
                onViewAllClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("68.8")  // 平均体重
            .assertExists()
    }

    @Test
    fun `should display stat cards for max and min weight`() {
        // Given - UI稿对应: trends/code.html 统计卡片
        val uiState = TrendsUiState(
            maxWeight = 70.2,
            minWeight = 67.8,
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TrendsScreen(
                uiState = uiState,
                onTimeRangeChange = {},
                onViewAllClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("最高体重")
            .assertExists()
        composeTestRule.onNodeWithText("70.2")
            .assertExists()
        composeTestRule.onNodeWithText("最低体重")
            .assertExists()
        composeTestRule.onNodeWithText("67.8")
            .assertExists()
    }

    @Test
    fun `should display change indicator with correct color`() {
        // Given
        val uiState = TrendsUiState(
            change = -1.2,  // 下降
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TrendsScreen(
                uiState = uiState,
                onTimeRangeChange = {},
                onViewAllClick = {}
            )
        }

        // Then - 下降显示绿色
        composeTestRule.onNodeWithText("-1.2kg")
            .assertTextColor(Color(0xFF059669))
    }

    @Test
    fun `should display insight card`() {
        // Given - UI稿对应: trends/code.html 智能分析卡片
        val uiState = TrendsUiState(
            insight = "你在早晨测量的体重通常比晚间低 0.8kg。",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TrendsScreen(
                uiState = uiState,
                onTimeRangeChange = {},
                onViewAllClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("智能分析")
            .assertExists()
    }

    @Test
    fun `should display recent records list`() {
        // Given - UI稿对应: trends/code.html 最近记录
        val records = listOf(
            WeightRecord(weight = 68.1, recordDate = "2026-03-22", recordTime = "08:30"),
            WeightRecord(weight = 68.4, recordDate = "2026-03-21", recordTime = "08:15")
        )
        val uiState = TrendsUiState(
            records = records,
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TrendsScreen(
                uiState = uiState,
                onTimeRangeChange = {},
                onViewAllClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("68.1 kg")
            .assertExists()
        composeTestRule.onNodeWithText("68.4 kg")
            .assertExists()
    }
}
```

### 4.4 DataScreen 测试

```kotlin
// test/java/com/weighttracker/presentation/screens/data/DataScreenTest.kt

@ComposeTestRules
class DataScreenTest {

    @get:Rule
    val composeTestRule = createComposeTestRule()

    @Test
    fun `should display storage info`() {
        // Given - UI稿对应: data_mgmt/code.html 存储占用
        val uiState = DataUiState(
            storageSize = "12.4 MB",
            lastBackupTime = "2023-10-24 14:20",
            recordCount = 156
        )

        // When
        composeTestRule.setContent {
            DataScreen(
                uiState = uiState,
                onExportClick = {},
                onImportClick = {},
                onBackupClick = {},
                onRestoreClick = {},
                onClearDataClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("12.4 MB")
            .assertExists()
        composeTestRule.onNodeWithText("2023-10-24 14:20")
            .assertExists()
    }

    @Test
    fun `should display export to excel button`() {
        // Given - UI稿对应: data_mgmt/code.html Excel操作
        val uiState = DataUiState()

        // When
        composeTestRule.setContent {
            DataScreen(
                uiState = uiState,
                onExportClick = {},
                onImportClick = {},
                onBackupClick = {},
                onRestoreClick = {},
                onClearDataClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("导出为 Excel")
            .assertExists()
        composeTestRule.onNodeWithText("从 Excel 导入")
            .assertExists()
    }

    @Test
    fun `should show loading when exporting`() {
        // Given
        val uiState = DataUiState(isExporting = true)

        // When
        composeTestRule.setContent {
            DataScreen(
                uiState = uiState,
                onExportClick = {},
                onImportClick = {},
                onBackupClick = {},
                onRestoreClick = {},
                onClearDataClick = {}
            )
        }

        // Then - 导出按钮应该显示加载状态
        composeTestRule.onNodeWithText("导出为 Excel")
            .assertIsNotEnabled()
    }

    @Test
    fun `should display data tools list`() {
        // Given - UI稿对应: data_mgmt/code.html 数据工具
        val uiState = DataUiState()

        // When
        composeTestRule.setContent {
            DataScreen(
                uiState = uiState,
                onExportClick = {},
                onImportClick = {},
                onBackupClick = {},
                onRestoreClick = {},
                onClearDataClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("自动清理旧数据")
            .assertExists()
        composeTestRule.onNodeWithText("数据迁移")
            .assertExists()
        composeTestRule.onNodeWithText("彻底清空数据")
            .assertExists()
    }

    @Test
    fun `should show confirmation dialog before clearing data`() {
        // Given
        var showDialog = false
        val uiState = DataUiState()

        // When
        composeTestRule.setContent {
            DataScreen(
                uiState = uiState,
                onExportClick = {},
                onImportClick = {},
                onBackupClick = {},
                onRestoreClick = {},
                onClearDataClick = { showDialog = true }
            )
        }

        composeTestRule.onNodeWithText("彻底清空数据")
            .performClick()

        // Then - 应该显示确认对话框
        assertThat(showDialog).isTrue()
    }

    @Test
    fun `should display success message after export`() {
        // Given
        val uiState = DataUiState(
            message = "导出成功",
            messageType = MessageType.SUCCESS
        )

        // When
        composeTestRule.setContent {
            DataScreen(
                uiState = uiState,
                onExportClick = {},
                onImportClick = {},
                onBackupClick = {},
                onRestoreClick = {},
                onClearDataClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("导出成功")
            .assertExists()
    }
}
```

---

## 5. E2E 测试 (End-to-End Tests)

### 5.1 体重记录完整流程

```kotlin
// test/java/com/weighttracker/e2e/WeightRecordingFlowTest.kt

@HiltAndroidTest
@Large
class WeightRecordingFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Before
    fun setup() {
        database.clearAllTables()
    }

    @Test
    fun `complete weight recording flow`() {
        // Step 1: 从首页进入记录页面
        composeTestRule.onNodeWithText("添加")
            .performClick()
        
        composeTestRule.onNodeWithText("体重记录")
            .assertExists()

        // Step 2: 输入体重
        composeTestRule.onNodeWithText("00.0")
            .performTextReplacement("65.5")

        // Step 3: 选择心情
        composeTestRule.onAllNodesWithContentDescription("心情选项")[2]  // 第3个
            .performClick()

        // Step 4: 添加备注
        composeTestRule.onNodeWithText("备注")
            .performTextReplacement("今天感觉很好")

        // Step 5: 保存
        composeTestRule.onNodeWithText("保存记录")
            .performClick()

        // Step 6: 验证返回首页
        composeTestRule.onNodeWithText("65.5")
            .assertExists()
        composeTestRule.onNodeWithText("KG")
            .assertExists()

        // Step 7: 验证数据持久化
        val record = database.weightRecordDao().getLatestRecord().first()
        assertThat(record?.weight).isEqualTo(65.5)
        assertThat(record?.mood).isEqualTo(3)
    }

    @Test
    fun `view trend after recording`() {
        // Given: 已有记录
        insertTestRecord(WeightRecordEntity(
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        ))

        // Step 1: 点击趋势Tab
        composeTestRule.onNodeWithText("趋势")
            .performClick()

        // Step 2: 验证显示趋势页面
        composeTestRule.onNodeWithText("趋势分析")
            .assertExists()
        
        // Step 3: 验证数据显示
        composeTestRule.onNodeWithText("65.5")
            .assertExists()
    }
}
```

### 5.2 数据导出导入流程

```kotlin
// test/java/com/weighttracker/e2e/DataManagementFlowTest.kt

@HiltAndroidTest
@Large
class DataManagementFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var filePicker: TestFilePicker

    @Before
    fun setup() {
        database.clearAllTables()
        insertMultipleTestRecords(10)
    }

    @Test
    fun `export and verify excel file`() {
        // Step 1: 进入数据管理
        composeTestRule.onNodeWithText("数据")
            .performClick()
        
        // Step 2: 点击导出
        composeTestRule.onNodeWithText("导出为 Excel")
            .performClick()

        // Step 3: 选择保存位置
        filePicker.selectSaveLocation("Downloads")

        // Step 4: 验证成功消息
        composeTestRule.onNodeWithText("导出成功")
            .assertExists()

        // Step 5: 验证文件生成
        val exportFile = File("/storage/emulated/0/Download/体重记录_*.xlsx")
        assertThat(exportFile.exists()).isTrue()
    }

    @Test
    fun `import from excel`() {
        // Step 1: 进入数据管理
        composeTestRule.onNodeWithText("数据")
            .performClick()

        // Step 2: 点击导入
        composeTestRule.onNodeWithText("从 Excel 导入")
            .performClick()

        // Step 3: 选择文件
        filePicker.selectFile("test_data.xlsx")

        // Step 4: 验证预览显示
        composeTestRule.onNodeWithText("即将导入 10 条记录")
            .assertExists()

        // Step 5: 确认导入
        composeTestRule.onNodeWithText("确认")
            .performClick()

        // Step 6: 验证成功
        composeTestRule.onNodeWithText("导入成功")
            .assertExists()

        // Step 7: 验证数据
        val count = database.weightRecordDao().getRecordCount()
        assertThat(count).isEqualTo(10)
    }
}
```

---

## 6. 测试数据构建

### 6.1 测试数据生成器

```kotlin
// test/java/com/weighttracker/util/TestDataFactory.kt

object TestDataFactory {
    
    fun createWeightRecord(
        id: Long = 0,
        weight: Double = 65.5,
        recordDate: String = "2026-03-22",
        recordTime: String = "08:30",
        mood: Int? = 3,
        note: String? = "测试记录"
    ): WeightRecord = WeightRecord(
        id = id,
        weight = weight,
        recordDate = recordDate,
        recordTime = recordTime,
        mood = mood,
        note = note
    )

    fun createWeightRecordEntity(
        id: Long = 0,
        weight: Double = 65.5,
        recordDate: String = "2026-03-22",
        recordTime: String = "08:30",
        mood: Int? = 3,
        note: String? = "测试记录"
    ): WeightRecordEntity = WeightRecordEntity(
        id = id,
        weight = weight,
        recordDate = recordDate,
        recordTime = recordTime,
        mood = mood,
        note = note
    )

    fun createHomeUiState(
        latestRecord: WeightRecord? = createWeightRecord(),
        weeklyChange: Double? = -0.8,
        bmi: Double? = 22.4,
        goalProgress: Float = 0.7f,
        isLoading: Boolean = false,
        error: String? = null
    ): HomeUiState = HomeUiState(
        latestRecord = latestRecord,
        weeklyChange = weeklyChange,
        bmi = bmi,
        goalProgress = goalProgress,
        isLoading = isLoading,
        error = error
    )

    fun createWeeklyRecords(): List<WeightRecord> = listOf(
        createWeightRecord(weight = 70.0, recordDate = "2026-03-15"),
        createWeightRecord(weight = 69.5, recordDate = "2026-03-16"),
        createWeightRecord(weight = 69.0, recordDate = "2026-03-17"),
        createWeightRecord(weight = 68.8, recordDate = "2026-03-18"),
        createWeightRecord(weight = 68.5, recordDate = "2026-03-19"),
        createWeightRecord(weight = 68.2, recordDate = "2026-03-20"),
        createWeightRecord(weight = 68.0, recordDate = "2026-03-21")
    )
}
```

---

## 7. 测试配置

### 7.1 build.gradle.kts (Test)

```kotlin
// test 模块配置
android {
    testOptions {
        unitTests {
            all {
                it.usesVehicle(JvmTestInstrumentation.Backend.JVM)
                it.jvmArgs(
                    "-Xmx2048m",
                    "-XX:+UseParallelGC"
                )
            }
        }
        managedDevices {
            localDevices {
                create("pixel3a") {
                    device = "Pixel 3a"
                    apiLevel = 34
                    resolution = Resolution(1080, 2160, 440)
                }
            }
        }
    }
}

dependencies {
    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.4.0")
    
    // Android Test
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.50")
    
    androidTestAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.50")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.50")
}
```

### 7.2 Room Test 配置

```kotlin
// test/java/com/weighttracker/data/local/database/AppDatabaseTest.kt

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
abstract class AppDatabaseTest {
    
    protected lateinit var dao: WeightRecordDao
    protected lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setJournalMode(JournalMode.INMEMORY)
            .build()
        dao = db.weightRecordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }
}
```

---

## 8. 测试执行计划

### 8.1 CI/CD 集成

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Run unit tests
        run: ./gradlew test --stacktrace
      
  integration-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Run integration tests
        run: ./gradlew connectedAndroidTest

  ui-tests:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Run UI tests
        run: ./gradlew connectedAndroidTest -Pdevice=pixel3a
```

### 8.2 本地执行命令

```bash
# 单元测试
./gradlew test

# 集成测试
./gradlew connectedAndroidTest

# 指定测试类
./gradlew test --tests "*.AddWeightRecordUseCaseTest"

# UI测试
./gradlew connectedAndroidTest --device pixel3a

# 生成覆盖率报告
./gradlew testDebugUnitTestCoverage
```

---

## 9. 测试覆盖矩阵

| 功能 | 单元测试 | 集成测试 | UI测试 | E2E |
|------|:---------:|:---------:|:-------:|:----:|
| 体重记录 | ✅ | ✅ | ✅ | ✅ |
| 趋势查看 | ✅ | ✅ | ✅ | ✅ |
| Excel导出 | ✅ | ✅ | ❌ | ✅ |
| Excel导入 | ✅ | ✅ | ❌ | ✅ |
| 数据备份 | ✅ | ✅ | ❌ | ❌ |
| 数据恢复 | ✅ | ✅ | ❌ | ❌ |
| 暗色模式 | ❌ | ❌ | ✅ | ❌ |
| 导航 | ❌ | ❌ | ✅ | ✅ |
| 首页仪表盘 | ✅ | ✅ | ✅ | ✅ |
| 心情选择 | ❌ | ❌ | ✅ | ✅ |
| 数据校验 | ✅ | ✅ | ❌ | ❌ |

---

## 10. 附录

### 10.1 MockK 常用模式

```kotlin
// 模拟Flow返回
every { repository.getAllRecords() } returns flowOf(listOf(record))

// 模拟suspend函数
every { repository.addRecord(any()) } returns Result.success(1L)

// 模拟抛出异常
every { repository.getRecord(any()) } throws RuntimeException("Not found")

// 验证调用次数
verify { repository.addRecord(any()) }
verify(exactly = 1) { repository.addRecord(record) }

// 验证无调用
verify { repository.deleteAllRecords() wasNot Called }
```

### 10.2 Truth 常用断言

```kotlin
// 基本断言
assertThat(value).isEqualTo(expected)
assertThat(value).isNotEqualTo(notExpected)
assertThat(value).isNull()
assertThat(value).isNotNull()
assertThat(value).isTrue()
assertThat(value).isFalse()

// 集合断言
assertThat(list).hasSize(3)
assertThat(list).contains(record)
assertThat(list).doesNotContain(missing)
assertThat(list).containsExactly(item1, item2)

// 浮点数断言
assertThat(weight).isWithin(0.01).of(65.5)

// 异常断言
assertThatThrownBy { function() }
    .isInstanceOf(RuntimeException::class.java)
    .hasMessage("Error")
```

### 10.3 Turbine (Flow测试)

```kotlin
// 测试Flow
viewModel.uiState.test {
    val state = awaitItem()
    assertThat(state.isLoading).isTrue()
    
    val nextState = awaitItem()
    assertThat(nextState.isLoading).isFalse()
    
    awaitComplete()
}
```
