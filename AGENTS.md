# PROJECT KNOWLEDGE BASE

**Generated:** 2026-03-23
**Branch:** dev

## OVERVIEW

Android weight tracking app (体重记录). Kotlin + Jetpack Compose, MVVM + Clean Architecture, Hilt DI, Room DB.

## STRUCTURE

```
stitch_log_weight/
├── android/                    # Android module (main code)
│   ├── app/src/main/java/      # Kotlin sources
│   │   └── com/weighttracker/app/
│   │       ├── data/           # Room DB, Excel/Backup managers
│   │       ├── domain/         # Models, UseCases, Repository interfaces
│   │       ├── presentation/   # Compose UI, ViewModels, Navigation
│   │       └── di/             # Hilt modules
│   ├── app/build.gradle.kts    # App build config
│   └── gradle-8.4/             # Vendored Gradle (non-standard)
├── docs/                       # PRD, guides, AGENTS.md
└── stitch_log_weight/          # UI prototype HTML mockups
```

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Screen UI + VM | `android/app/src/main/java/.../presentation/screens/{screen}/` |
| Navigation | `android/app/src/main/java/.../presentation/navigation/NavGraph.kt` |
| Business logic | `android/app/src/main/java/.../domain/usecase/` |
| DB access | `android/app/src/main/java/.../data/local/dao/` |
| DI wiring | `android/app/src/main/java/.../di/` |

## CONVENTIONS

### Architecture
- MVVM: `Screen.kt` + `ViewModel.kt` + `UiState.kt` per feature
- UseCase pattern for business logic (single `operator fun invoke()`)
- Repository pattern: interface in domain, impl in data

### Naming
- Screen files: `{Name}Screen.kt`, `{Name}ViewModel.kt`, `{Name}UiState.kt`
- UseCases: `{Verb}Record{Noun}UseCase.kt` (e.g., `GetWeightRecordsUseCase`)
- Hilt modules: `{Feature}Module.kt` in `di/`

### Compose
- Material3 + Material Icons Extended
- `@HiltViewModel` on all ViewModels
- `StateFlow` for UI state, `collectAsState()` in composables

## ANTI-PATTERNS (THIS PROJECT)

- **NO `as any` or `@ts-ignore`** — type safety enforced
- **NO direct DB access in ViewModels** — always through UseCase/Repository
- **NO blocking calls in UI** — coroutines + Flow only

## COMMANDS

```bash
cd android
export JAVA_HOME=$(/usr/libexec/java_home)
./gradle-8.4/bin/gradle :app:assembleDebug --no-daemon
```

## NOTES

- Target SDK 34, Min SDK 26
- Vico chart library for Compose charts
- Apache POI for Excel import/export
- Gson for JSON backup/restore
- Chinese UI (zh-CN)
- Non-standard: Gradle 8.4 vendored in repo (`android/gradle-8.4/`)

## WORKFLOW RULES

**每次会话必须遵守以下规则：**

1. **加载过程文件** — 每次新会话开始时，必须读取 `docs/` 下的过程文件（TODO.md、CHANGELOG.md、USER_GUIDE.md 等），了解项目当前状态
2. **先维护待办** — 做每件事之前，先在 `docs/TODO.md` 中维护待办条目，评估好优先级（高🔴/中🟡/低🟢）
3. **等待明确指令** — 待办创建后，**不主动处理**，等用户明确说"处理"或"开始"时才执行
4. **完成后更新状态** — 处理完的待办必须在 `docs/TODO.md` 中标记为 ✅ 已完成
5. **测试回归 + 构建 APK** — 处理完待办后，必须进行测试回归验证，最终构建出 .apk 文件确认无编译错误
