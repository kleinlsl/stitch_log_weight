package com.weighttracker.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weighttracker.app.presentation.components.BmiCard
import com.weighttracker.app.presentation.components.GoalProgressCard
import com.weighttracker.app.presentation.components.MiniTrendChart
import com.weighttracker.app.presentation.components.StatCard
import com.weighttracker.app.presentation.components.WeightChangeBadge
import com.weighttracker.app.presentation.theme.AppColors

@Composable
fun HomeScreen(
    onFabClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                
                WeightDisplaySection(
                    weight = uiState.latestRecord?.weight,
                    change = uiState.weeklyChange
                )

                Spacer(Modifier.height(24.dp))

                uiState.latestRecord?.let { record ->
                    BmiCard(
                        weightKg = record.weight,
                        heightCm = uiState.height
                    )

                    Spacer(Modifier.height(24.dp))
                }

                StatsGrid(
                    bmi = uiState.bmi,
                    goalProgress = uiState.goalProgress,
                    goalWeight = uiState.goalWeight,
                    startWeight = uiState.startWeight,
                    weightChange = uiState.weightChange,
                    currentWeight = uiState.latestRecord?.weight
                )

                Spacer(Modifier.height(24.dp))

                WeeklyTrendSection(
                    records = uiState.weeklyRecords.map { it.weight.toFloat() }
                )

                Spacer(Modifier.height(24.dp))

                EncouragementCard(
                    weeklyChange = uiState.weeklyChange
                )

                Spacer(Modifier.height(120.dp))
            }
        }

        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 112.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "添加",
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeightDisplaySection(
    weight: Double?,
    change: Double?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "当前体重",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = weight?.let { String.format("%.1f", it) } ?: "--",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = " KG",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        if (change != null) {
            WeightChangeBadge(change = change)
        }
    }
}

@Composable
private fun StatsGrid(
    bmi: Double?,
    goalProgress: Float,
    goalWeight: Double,
    startWeight: Double,
    weightChange: Double,
    currentWeight: Double?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "BMI 指数",
            icon = Icons.Filled.MonitorWeight
        ) {
            Column {
                Text(
                    text = bmi?.let { String.format("%.1f", it) } ?: "--",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "正常范围",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Emerald600
                )
            }
        }

        GoalProgressCard(
            modifier = Modifier.weight(1f),
            title = "目标进度",
            progress = goalProgress,
            currentText = "${(goalProgress * 100).toInt()}%",
            targetText = "距 ${goalWeight} KG",
            startWeight = startWeight,
            weightChange = weightChange,
            currentWeight = currentWeight
        )
    }
}

@Composable
private fun WeeklyTrendSection(
    records: List<Float>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7日趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "最近一周",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            MiniTrendChart(
                heights = records.ifEmpty { listOf(0.5f, 0.6f, 0.4f, 0.5f, 0.3f, 0.4f, 0.35f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun EncouragementCard(
    weeklyChange: Double?
) {
    if (weeklyChange == null) return

    val message = if (weeklyChange < 0) {
        "继续保持！你在过去7天内成功减重了 ${String.format("%.1f", -weeklyChange)} KG。"
    } else if (weeklyChange > 0) {
        "体重略有上升，注意饮食控制和运动。"
    } else {
        "体重保持稳定，继续加油！"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💪", fontSize = 24.sp)
            }
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = if (weeklyChange < 0) "继续保持！" else "加油！",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
