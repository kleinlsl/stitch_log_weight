# PRESENTATION SCREENS

**Location:** `android/app/src/main/java/.../presentation/screens/`

## SCREENS

| Screen | Route | Files |
|--------|-------|-------|
| Home | `home` | `HomeScreen.kt`, `HomeViewModel.kt`, `HomeUiState.kt` |
| Entry | `entry` | `EntryScreen.kt`, `EntryViewModel.kt`, `EntryUiState.kt` |
| Trends | `trends` | `TrendsScreen.kt`, `TrendsViewModel.kt`, `TrendsUiState.kt` |
| Data | `data` | `DataScreen.kt`, `DataViewModel.kt`, `DataUiState.kt` |
| Settings | `settings` | `SettingsScreen.kt`, `SettingsViewModel.kt`, `SettingsRepository.kt` |
| History | `history` | `HistoryScreen.kt`, `HistoryViewModel.kt` |

## PATTERN

Each screen follows MVVM:
```
{Name}Screen.kt   → Compose UI, @Composable
{Name}ViewModel.kt → @HiltViewModel, StateFlow<UiState>
{Name}UiState.kt   → Data class with UI state
```

## NAVIGATION

- Entry: `Screen.Entry.route`, closed via `onNavigateBack`
- History: `Screen.History.route`, accessed from Trends screen
- Settings: `Screen.Settings.route`, accessible from Home/Entry/Data

## CONVENTIONS

- ViewModels inject UseCases via `@Inject constructor`
- UI state exposed as `StateFlow`, collected via `collectAsState()`
- Loading/error states in UiState data classes
