# 技术方案设计文档

**Project:** 体重记录 App  
**Version:** 1.0  
**Date:** 2026-03-22  
**Author:** 开发团队  
**Status:** Draft

---

## 1. 技术架构概述

### 1.1 整体架构

采用 **MVVM + Clean Architecture** 三层架构，确保代码清晰、易测试、易维护。

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │   Screens   │  │   ViewModels │  │    States    │  │
│  │  (Compose) │  │  (ViewModel) │  │   (UiState)  │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │  Use Cases  │  │   Entities  │  │ Repos IF    │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                       Data Layer                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │    Room DB  │  │   File I/O  │  │ Repos Impl  │    │
│  │  (SQLite)   │  │ (Excel/Json)│  │             │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### 1.2 技术栈选型

| 层级 | 技术选型 | 理由 |
|------|----------|------|
| **UI框架** | Jetpack Compose | 声明式UI，与设计稿的Tailwind风格高度匹配 |
| **语言** | Kotlin 1.9+ | 现代Android首选，协程支持好 |
| **本地数据库** | Room (SQLite) | Google官方ORM，类型安全，生命周期感知 |
| **依赖注入** | Hilt | Google推荐，与Compose集成良好 |
| **异步处理** | Kotlin Coroutines + Flow | 响应式数据流，适合UI状态更新 |
| **图表库** | Vico | Compose原生图表库，轻量可定制，支持折线图 |
| **Excel处理** | Apache POI | Java生态最成熟，支持.xlsx读写 |
| **Navigation** | Compose Navigation | 原生支持，类型安全路由 |
| **最低SDK** | API 26 (Android 8.0) | 覆盖95%+设备 |

---

## 2. 数据库设计

### 2.1 ER图

```
┌──────────────────┐
│   weight_record  │
├──────────────────┤
│ id (PK, Long)   │
│ weight (Double)  │
│ record_date      │
│ record_time      │
│ mood (Int?)      │
│ note (String?)   │
│ created_at       │
│ updated_at       │
└──────────────────┘
```

### 2.2 表结构 (Room Entity)

```kotlin
@Entity(tableName = "weight_records")
data class WeightRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "weight")
    val weight: Double,  // 公斤，精确到0.1
    
    @ColumnInfo(name = "record_date")
    val recordDate: String,  // 格式: "2026-03-22"
    
    @ColumnInfo(name = "record_time")
    val recordTime: String,  // 格式: "08:30"
    
    @ColumnInfo(name = "mood")
    val mood: Int? = null,  // 1=很差, 2=差, 3=一般, 4=好, 5=很好
    
    @ColumnInfo(name = "note")
    val note: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 2.3 DAO 接口

```kotlin
@Dao
interface WeightRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WeightRecordEntity): Long
    
    @Update
    suspend fun update(record: WeightRecordEntity)
    
    @Delete
    suspend fun delete(record: WeightRecordEntity)
    
    @Query("SELECT * FROM weight_records ORDER BY record_date DESC, record_time DESC")
    fun getAllRecords(): Flow<List<WeightRecordEntity>>
    
    @Query("SELECT * FROM weight_records WHERE record_date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): WeightRecordEntity?
    
    @Query("SELECT * FROM weight_records WHERE record_date BETWEEN :startDate AND :endDate ORDER BY record_date ASC, record_time ASC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<WeightRecordEntity>>
    
    @Query("SELECT * FROM weight_records ORDER BY record_date DESC, record_time DESC LIMIT 1")
    fun getLatestRecord(): Flow<WeightRecordEntity?>
    
    @Query("SELECT COUNT(*) FROM weight_records")
    suspend fun getRecordCount(): Int
    
    @Query("DELETE FROM weight_records")
    suspend fun deleteAll()
}
```

---

## 3. 功能模块设计

### 3.1 模块划分

```
app/
├── data/                      # Data Layer
│   ├── local/
│   │   ├── dao/              # WeightRecordDao
│   │   ├── entity/           # WeightRecordEntity
│   │   └── database/         # AppDatabase
│   ├── repository/           # WeightRepositoryImpl
│   └── file/
│       ├── ExcelManager.kt    # Excel导入导出
│       └── BackupManager.kt   # JSON备份恢复
│
├── domain/                    # Domain Layer
│   ├── model/
│   │   ├── WeightRecord.kt    # 领域模型
│   │   └── WeightStats.kt     # 统计数据
│   ├── repository/           # Repository接口
│   └── usecase/
│       ├── AddWeightRecordUseCase.kt
│       ├── GetWeightRecordsUseCase.kt
│       ├── GetWeightStatsUseCase.kt
│       ├── ExportToExcelUseCase.kt
│       ├── ImportFromExcelUseCase.kt
│       └── BackupUseCase.kt
│
├── presentation/              # Presentation Layer
│   ├── navigation/
│   ├── theme/                # Compose Theme
│   ├── components/           # 通用组件
│   └── screens/
│       ├── home/             # 首页仪表盘
│       ├── entry/            # 体重记录
│       ├── trends/           # 趋势分析
│       └── data/             # 数据管理
│
└── di/                       # Hilt模块
```

### 3.2 核心业务用例

| 用例 | 输入 | 输出 | 说明 |
|------|------|------|------|
| AddWeightRecord | WeightRecord | Result<Long> | 新增体重记录 |
| GetWeightRecords | DateRange | Flow<List<WeightRecord>> | 获取日期范围内记录 |
| GetLatestRecord | - | Flow<WeightRecord?> | 获取最新记录 |
| GetWeightStats | DateRange | WeightStats | 统计：最高/最低/平均 |
| GetWeeklyChange | - | Double? | 计算较上周变化 |
| ExportToExcel | File | Result<File> | 导出全部数据到Excel |
| ImportFromExcel | File | Result<ImportResult> | 导入Excel，返回预览 |
| BackupData | File | Result<File> | JSON格式完整备份 |
| RestoreData | File | Result<Unit> | 从备份恢复 |
| DeleteAllData | - | Result<Unit> | 清空所有数据 |

---

## 4. UI/UX 实现方案（基于UI稿）

### 4.1 页面结构（与UI稿一一对应）

```
App
├── HomeScreen (首页仪表盘) ← dashboard/code.html
│   ├── TopAppBar: "体重记录" + 设置按钮
│   ├── Hero: 当前体重 72.5 KG (大字)
│   ├── 较上周变化徽章: -0.8 KG ↓
│   ├── Bento Grid:
│   │   ├── BMI指数卡片
│   │   └── 目标进度卡片 (70%, 距70.0KG)
│   ├── 7日趋势迷你图
│   ├── 鼓励卡片: "继续保持！..."
│   └── FAB: 右下角添加按钮
│
├── EntryScreen (体重记录) ← log_weight/code.html
│   ├── TopAppBar: "体重记录" + 关闭按钮
│   ├── Hero: 体重输入 65.5 KG (大数字)
│   ├── 进度指示器
│   ├── Bento Grid:
│   │   ├── 日期选择器 (2023-10-27)
│   │   ├── 时间选择器 (08:30)
│   │   ├── 心情选择 (5档emoji)
│   │   └── 备注文本框
│   └── 底部保存按钮 (渐变+圆角)
│
├── TrendsScreen (趋势分析) ← trends/code.html
│   ├── TopAppBar: "趋势分析" + 分享按钮
│   ├── 时间范围切换: 周/月/年
│   ├── 平均体重 + 变化指示
│   ├── 折线图 (SVG风格)
│   ├── 统计卡片:
│   │   ├── 最高体重 70.2 KG
│   │   └── 最低体重 67.8 KG
│   ├── 最近记录列表
│   └── 智能分析卡片 (深色背景)
│
├── DataScreen (数据管理) ← data_mgmt/code.html
│   ├── TopAppBar: "数据管理"
│   ├── 存储占用: 12.4 MB + 上次备份时间
│   ├── 备份/恢复按钮
│   ├── Excel操作:
│   │   ├── 导出为Excel (蓝色大卡片)
│   │   └── 从Excel导入 (白色卡片)
│   ├── 数据工具列表:
│   │   ├── 自动清理旧数据
│   │   ├── 数据迁移
│   │   └── 彻底清空数据 (红色)
│
└── BottomNavBar (底部导航 - 全部页面)
    ├── 今日 (日历图标)
    ├── 趋势 (趋势线图标)
    ├── 数据 (数据库图标)
    └── 设置 (齿轮图标)
```

### 4.2 设计系统实现（与UI稿精确对应）

#### 4.2.1 颜色体系 (Color.kt)

```kotlin
// 亮色模式 - 对应UI稿的 light 模式
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0B57D0),           // Primary蓝
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),  // Primary Container
    onPrimaryContainer = Color(0xFF041E49),
    
    secondary = Color(0xFF4B5D8C),         // Secondary蓝灰
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDAE2FF),
    onSecondaryContainer = Color(0xFF415382),
    
    tertiary = Color(0xFF802B00),           // Tertiary橙
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCE),
    
    background = Color(0xFFF8F9FA),         // 背景色
    onBackground = Color(0xFF191C1D),
    
    surface = Color(0xFFF8F9FA),           // Surface底色
    surfaceVariant = Color(0xFFE1E3E4),     // Surface变体
    onSurfaceVariant = Color(0xFF424654),
    
    outline = Color(0xFF747785),           // 边框色
    outlineVariant = Color(0xFFC3C6D6),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    
    // Surface Container层级 (无分割线设计)
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F4F5),
    surfaceContainer = Color(0xFFEDEEEF),
    surfaceContainerHigh = Color(0xFFE7E8E9),
    surfaceContainerHighest = Color(0xFFE1E3E4),
)

// 暗色模式 - 对应UI稿的 dark 模式
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB2C5FF),            // 浅蓝
    onPrimary = Color(0xFF003295),
    primaryContainer = Color(0xFF0041A2),
    onPrimaryContainer = Color(0xFFD6E3FF),
    
    background = Color(0xFF191C1D),
    onBackground = Color(0xFFE2E2E6),
    
    surface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),
    
    // 暗色Surface Container层级
    surfaceContainerLowest = Color(0xFF0F1416),
    surfaceContainerLow = Color(0xFF1A1D21),
    surfaceContainer = Color(0xFF1F2427),
    surfaceContainerHigh = Color(0xFF2A2F32),
    surfaceContainerHighest = Color(0xFF353A3F),
)
```

#### 4.2.2 圆角体系 (Shape.kt) - Bento Grid设计

```kotlin
val AppShapes = Shapes(
    // 小圆角: 用于按钮、小卡片
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    
    // 中圆角: 用于输入框、小区块
    medium = RoundedCornerShape(12.dp),
    
    // 大圆角: 用于Bento卡片 ← UI稿的 2xl (1rem = 16px)
    large = RoundedCornerShape(16.dp),
    
    // 超大圆角: 用于主要Bento卡片 ← UI稿的 3xl (1.5rem = 24px)
    extraLarge = RoundedCornerShape(24.dp),
    
    // 全圆: 用于FAB、保存按钮 ← UI稿的 full
    full = RoundedCornerShape(50)  // 9999px
)
```

#### 4.2.3 字体体系 (Type.kt)

```kotlin
val AppTypography = Typography(
    // 展示字体 - 体重数字 (57sp/7xl)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.ExtraBold,  // 800
        fontSize = 57.sp,
        letterSpacing = (-0.25).sp,
        lineHeight = 64.sp
    ),
    
    // 标题字体 - 页面标题 (32sp)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // 副标题 (24sp)
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    
    // 卡片标题 (18sp semibold)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    
    // 正文 (14-16sp)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    
    // 标签/小字 (10-12sp)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)
```

### 4.3 导航配置（与UI稿一致）

```kotlin
// Screen.kt - 路由定义
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "体重记录")
    object Entry : Screen("entry", "体重记录")
    object Trends : Screen("trends", "趋势分析")
    object Data : Screen("data", "数据管理")
    object Settings : Screen("settings", "设置")
}

// NavGraph.kt - 导航图
NavHost(
    navController = navController,
    startDestination = Screen.Home.route
) {
    composable(Screen.Home.route) {
        HomeScreen(
            onFabClick = { navController.navigate(Screen.Entry.route) }
        )
    }
    composable(Screen.Entry.route) {
        EntryScreen(
            onClose = { navController.popBackStack() }
        )
    }
    composable(Screen.Trends.route) {
        TrendsScreen()
    }
    composable(Screen.Data.route) {
        DataScreen()
    }
}

// BottomNavBar.kt - 底部导航（与UI稿一致）
NavigationBar(
    containerColor = Color.Transparent,
    modifier = Modifier.navigationBarsPadding()
) {
    bottomNavItems.forEach { item ->
        NavigationBarItem(
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { Text(item.label) },
            selected = currentRoute == item.route,
            onClick = { /* 导航逻辑 */ }
        )
    }
}
```

### 4.4 核心组件实现

#### 4.4.1 顶部导航栏 (TopAppBar)

```kotlin
// 对应UI稿的固定顶部栏
@Composable
fun AppTopBar(
    title: String,
    onLeftClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null,
    leftIcon: ImageVector = Icons.Filled.Close,
    rightIcon: ImageVector = Icons.Filled.Settings
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (onLeftClick != null) {
                IconButton(onClick = onLeftClick) {
                    Icon(leftIcon, contentDescription = "返回")
                }
            }
        },
        actions = {
            if (onRightClick != null) {
                IconButton(onClick = onRightClick) {
                    Icon(rightIcon, contentDescription = "设置")
                }
            }
        }
    )
}
```

#### 4.4.2 Bento Grid卡片

```kotlin
// 对应UI稿的2列布局卡片
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),  // 3xl圆角
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp  // 微弱阴影
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

// 使用示例 - HomeScreen中的Bento Grid
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
) {
    StatCard(
        modifier = Modifier.weight(1f),
        title = "BMI 指数",
        icon = { Icon(Icons.Filled.MonitorWeight, null) }
    ) {
        Column {
            Text("22.4", style = MaterialTheme.typography.headlineMedium)
            Text("正常范围", color = Color(0xFF059669))
        }
    }
    
    StatCard(
        modifier = Modifier.weight(1f),
        title = "目标进度",
        icon = { Icon(Icons.Filled.Flag, null) }
    ) {
        Column {
            Row {
                Text("70%", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Text("距 70.0 KG", style = MaterialTheme.typography.labelSmall)
            }
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

#### 4.4.3 浮动操作按钮 (FAB)

```kotlin
// 对应UI稿的FAB: 右侧6个单位，底部28个单位
FloatingActionButton(
    onClick = onClick,
    modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 24.dp, bottom = 112.dp),  // 避开底部导航
    shape = RoundedCornerShape(16.dp),  // xl圆角 (非圆形)
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary
) {
    Icon(
        Icons.Filled.Add,
        contentDescription = "添加",
        modifier = Modifier.size(24.dp)
    )
}
```

#### 4.4.4 心情选择器

```kotlin
// 对应UI稿的5档心情选择
@Composable
fun MoodSelector(
    selectedMood: Int?,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = listOf(
        1 to "sentiment_very_dissatisfied",
        2 to "sentiment_dissatisfied", 
        3 to "sentiment_satisfied",
        4 to "sentiment_satisfied_alt",
        5 to "sentiment_very_satisfied"
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        moods.forEach { (value, icon) ->
            val isSelected = selectedMood == value
            
            IconButton(
                onClick = { onMoodSelected(value) },
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(16.dp)
                                )
                        } else Modifier
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled(icon),
                    contentDescription = null,
                    tint = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
```

#### 4.4.5 底部导航栏（玻璃态）

```kotlin
// 对应UI稿的玻璃态底部导航
@Composable
fun GlassBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            val items = listOf(
                Triple("今日", Icons.Filled.CalendarToday, "home"),
                Triple("趋势", Icons.Filled.TrendingUp, "trends"),
                Triple("数据", Icons.Filled.Database, "data"),
                Triple("设置", Icons.Filled.Settings, "settings")
            )
            
            items.forEach { (label, icon, route) ->
                val selected = currentRoute == route
                
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(route) },
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
```

#### 4.4.6 保存按钮（渐变+全圆角）

```kotlin
// 对应UI稿的底部保存按钮
@Composable
fun SaveButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(50),  // 全圆角
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Icon(Icons.Filled.Check, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("保存记录", fontWeight = FontWeight.Bold)
    }
}
```

---

## 5. Excel 导入导出方案

### 5.1 导出格式（与UI稿data_mgmt页面对应）

**文件名:** `体重记录_20260322_103000.xlsx`

**Sheet结构:**

| 列 | 字段名 | 类型 | 说明 |
|----|--------|------|------|
| A | 日期 | String | "2026-03-22" |
| B | 时间 | String | "08:30" |
| C | 体重(kg) | Double | 65.5 |
| D | 心情 | Integer | 1-5 |
| E | 备注 | String | 可为空 |

### 5.2 ExcelManager 实现

```kotlin
class ExcelManager @Inject constructor() {
    
    suspend fun exportToExcel(
        records: List<WeightRecord>,
        outputStream: OutputStream
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("体重记录")
            
            // 表头
            val header = sheet.createRow(0)
            listOf("日期", "时间", "体重(kg)", "心情", "备注").forEachIndexed { index, name ->
                header.createCell(index).setCellValue(name)
            }
            
            // 数据行
            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(record.recordDate)
                row.createCell(1).setCellValue(record.recordTime)
                row.createCell(2).setCellValue(record.weight)
                row.createCell(3).setCellValue(record.mood?.toDouble() ?: 0.0)
                row.createCell(4).setCellValue(record.note ?: "")
            }
            
            workbook.write(outputStream)
            workbook.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importFromExcel(inputStream: InputStream): Result<List<WeightRecord>> 
        = withContext(Dispatchers.IO) {
        // 解析Excel，映射到WeightRecord列表
    }
}
```

### 5.3 备份格式（JSON）

```json
{
  "version": "1.0",
  "app_version": "1.0.0",
  "exported_at": "2026-03-22T10:30:00Z",
  "total_records": 156,
  "records": [
    {
      "weight": 65.5,
      "record_date": "2026-03-22",
      "record_time": "08:30",
      "mood": 3,
      "note": "今天感觉不错"
    }
  ]
}
```

---

## 6. 数据流设计

### 6.1 单向数据流

```
User Action → ViewModel → UseCase → Repository → Room DB
                 ↓
             UiState (StateFlow)
                 ↓
             Compose UI (collectAsStateWithLifecycle)
```

### 6.2 State设计（与UI稿页面一一对应）

```kotlin
// HomeScreen UiState - 对应dashboard/code.html
data class HomeUiState(
    val latestRecord: WeightRecord? = null,       // 当前体重
    val weeklyRecords: List<WeightRecord> = emptyList(),
    val weeklyChange: Double? = null,              // 较上周变化
    val bmi: Double? = null,                       // BMI指数
    val goalProgress: Float = 0f,                  // 目标进度
    val goalWeight: Double = 70.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

// EntryScreen UiState - 对应log_weight/code.html
data class EntryUiState(
    val weight: String = "",                        // "65.5"
    val date: LocalDate = LocalDate.now(),         // 默认今天
    val time: LocalTime = LocalTime.of(8, 30),    // 默认08:30
    val mood: Int? = null,                         // 1-5
    val note: String = "",
    val weightUnit: String = "KG",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

// TrendsScreen UiState - 对应trends/code.html
data class TrendsUiState(
    val timeRange: TimeRange = TimeRange.WEEK,     // 周/月/年
    val records: List<WeightRecord> = emptyList(),
    val averageWeight: Double = 0.0,
    val maxWeight: Double = 0.0,
    val minWeight: Double = 0.0,
    val change: Double = 0.0,                      // 较上期变化
    val isLoading: Boolean = true,
    val error: String? = null
)

// DataScreen UiState - 对应data_mgmt/code.html
data class DataUiState(
    val storageSize: String = "0 MB",              // "12.4 MB"
    val lastBackupTime: String? = null,            // "2023-10-24 14:20"
    val recordCount: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null
)
```

---

## 7. 依赖管理

### 7.1 build.gradle.kts (app)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

android {
    namespace = "com.weighttracker.app"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.weighttracker.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Charts - Vico (Compose原生)
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    
    // Apache POI for Excel
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    
    // JSON for backup
    implementation("com.google.code.gson:gson:2.10.1")
}
```

---

## 8. 文件结构

```
app/src/main/
├── AndroidManifest.xml
├── java/com/weighttracker/app/
│   ├── WeightTrackerApp.kt           # Application + Hilt
│   ├── MainActivity.kt
│   │
│   ├── data/
│   │   ├── local/
│   │   │   ├── dao/WeightRecordDao.kt
│   │   │   ├── entity/WeightRecordEntity.kt
│   │   │   └── database/AppDatabase.kt
│   │   ├── repository/WeightRepositoryImpl.kt
│   │   └── file/
│   │       ├── ExcelManager.kt
│   │       └── BackupManager.kt
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── WeightRecord.kt
│   │   │   ├── WeightStats.kt
│   │   │   └── TimeRange.kt
│   │   ├── repository/WeightRepository.kt
│   │   └── usecase/
│   │       ├── AddWeightRecordUseCase.kt
│   │       ├── GetWeightRecordsUseCase.kt
│   │       ├── GetWeightStatsUseCase.kt
│   │       ├── ExportToExcelUseCase.kt
│   │       ├── ImportFromExcelUseCase.kt
│   │       └── BackupUseCase.kt
│   │
│   ├── presentation/
│   │   ├── MainScreen.kt             # 主容器 + 底部导航
│   │   ├── navigation/
│   │   │   ├── NavGraph.kt
│   │   │   └── Screen.kt
│   │   ├── theme/
│   │   │   ├── Color.kt              # 完整配色体系
│   │   │   ├── Type.kt               # 字体体系
│   │   │   ├── Shape.kt              # 圆角体系
│   │   │   └── Theme.kt              # Material3 Theme
│   │   ├── components/
│   │   │   ├── AppTopBar.kt          # 顶部导航栏
│   │   │   ├── GlassBottomNav.kt      # 玻璃态底部导航
│   │   │   ├── StatCard.kt           # Bento卡片
│   │   │   ├── MoodSelector.kt       # 心情选择
│   │   │   ├── SaveButton.kt         # 保存按钮
│   │   │   ├── MiniChart.kt          # 迷你趋势图
│   │   │   └── TrendChart.kt        # 完整趋势图
│   │   └── screens/
│   │       ├── home/
│   │       │   ├── HomeScreen.kt
│   │       │   └── HomeViewModel.kt
│   │       ├── entry/
│   │       │   ├── EntryScreen.kt
│   │       │   └── EntryViewModel.kt
│   │       ├── trends/
│   │       │   ├── TrendsScreen.kt
│   │       │   └── TrendsViewModel.kt
│   │       └── data/
│   │           ├── DataScreen.kt
│   │           └── DataViewModel.kt
│   │
│   └── di/
│       ├── AppModule.kt
│       ├── DatabaseModule.kt
│       └── RepositoryModule.kt
│
└── res/
    ├── values/strings.xml             # "体重记录", "保存记录" 等
    ├── values-zh-rCN/strings.xml
    └── values-night/colors.xml        # 暗色配色
```

---

## 9. 测试策略

| 测试类型 | 覆盖范围 | 工具 |
|----------|----------|------|
| 单元测试 | UseCase, Repository, ViewModel | JUnit 5, MockK |
| 集成测试 | Room DAO, Repository | Instrumented tests |
| UI测试 | Compose Screens | Compose UI Testing |
| 回归测试 | 核心功能 | Espresso |

---

## 10. 开发里程碑

| 阶段 | 时间 | 交付物 |
|------|------|--------|
| **M1: 项目搭建** | Day 1-2 | 项目骨架、设计系统(Theme)、Room数据库 |
| **M2: HomeScreen** | Day 3-5 | 首页仪表盘 + EntryScreen |
| **M3: TrendsScreen** | Day 6-8 | 趋势分析页面 + 图表 |
| **M4: DataScreen** | Day 9-12 | 数据管理 + Excel导入导出 |
| **M5: 测试发布** | Day 13-18 | 功能测试、性能测试、发布APK |

---

## 11. 关键技术决策

### 11.1 为什么用Vico而不是MPAndroidChart？

- **Compose原生**: 无需Bridge，性能更好
- **声明式配置**: 与Kotlin DSL风格一致
- **轻量级**: 功能足够覆盖折线图需求

### 11.2 为什么用Surface Container层级而不是Card？

- Material3的Surface Container提供更精细的层级控制
- 与UI稿的"无分割线"设计理念一致
- 支持更灵活的背景色组合

### 11.3 为什么不支持.xls格式？

- Apache POI对.xlsx支持更好，.xls已过时
- 现代Android设备默认导出.xlsx格式

---

## 12. 验收清单

- [ ] 体重记录保存成功，数据正确写入SQLite
- [ ] 首页正确显示最新体重和周变化（对应dashboard/code.html）
- [ ] BMI指数和目标进度卡片正确显示
- [ ] 7日趋势迷你图正确渲染
- [ ] 趋势图表正确渲染周/月/年数据（对应trends/code.html）
- [ ] Excel导出生成正确格式的.xlsx文件
- [ ] Excel导入正确解析并显示预览
- [ ] 数据备份/恢复功能正常
- [ ] 暗色模式切换正常
- [ ] 冷启动时间 < 2秒
- [ ] 所有圆角符合设计规范（24dp用于主要卡片）
- [ ] 无1px分割线，使用背景色区分层级

---

## 13. UI稿与代码映射表

| UI稿文件 | 对应页面 | 关键组件 |
|----------|----------|----------|
| `dashboard/code.html` | HomeScreen | Hero体重展示、Bento Grid统计卡片、迷你图、鼓励卡片、FAB |
| `log_weight/code.html` | EntryScreen | 大数字输入、日期/时间选择器、心情选择器、备注文本、保存按钮 |
| `trends/code.html` | TrendsScreen | 时间切换Tab、折线图、统计卡片、最近记录、分析卡片 |
| `data_mgmt/code.html` | DataScreen | 存储占用、备份按钮、Excel导入导出卡片、数据工具列表 |
| `azure_horizon/DESIGN.md` | Theme | 完整设计系统规范 |
