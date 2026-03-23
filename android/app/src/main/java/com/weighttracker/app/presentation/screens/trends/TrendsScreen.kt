package com.weighttracker.app.presentation.screens.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weighttracker.app.domain.model.TimeRange
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.presentation.components.WeightLineChart
import com.weighttracker.app.presentation.theme.AppColors

@Composable
fun TrendsScreen(
    onNavigateToHistory: () -> Unit = {},
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopBar(
            onHistoryClick = onNavigateToHistory,
            onShareClick = { viewModel.shareTrendData(context) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            TimeRangeSelector(
                selected = uiState.timeRange,
                onSelect = viewModel::onTimeRangeChange
            )

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ChartSection(
                            averageWeight = uiState.averageWeight,
                            change = uiState.change,
                            records = uiState.records
                        )
                    }

                    item {
                        StatsGrid(
                            maxWeight = uiState.maxWeight,
                            minWeight = uiState.minWeight
                        )
                    }

                    item {
                        Text(
                            text = "最近记录",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.records.take(5)) { record ->
                        RecordItem(record = record)
                    }

                    item {
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onHistoryClick: () -> Unit, onShareClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
        }

        Text(
            text = "趋势分析",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row {
            IconButton(onClick = onShareClick) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onHistoryClick) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = "历史记录",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.shapes.medium
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val label = when (range) {
                TimeRange.WEEK -> "周"
                TimeRange.MONTH -> "月"
                TimeRange.YEAR -> "年"
            }
            val isSelected = range == selected

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clickable { onSelect(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChartSection(
    averageWeight: Double,
    change: Double,
    records: List<WeightRecord>
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
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "平均体重",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", averageWeight),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                if (change != 0.0) {
                    Row(
                        modifier = Modifier
                            .background(
                                AppColors.Emerald50,
                                MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (change < 0) "↓" else "↑",
                            color = AppColors.Emerald600,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", kotlin.math.abs(change))}kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Emerald600,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SimpleLineChart(
                weights = records.map { it.weight },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    weights: List<Double>,
    modifier: Modifier = Modifier
) {
    WeightLineChart(
        weights = weights,
        modifier = modifier
    )
}

@Composable
private fun StatsGrid(
    maxWeight: Double,
    minWeight: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatBox(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.VerticalAlignTop,
            title = "最高体重",
            value = "${String.format("%.1f", maxWeight)} kg",
            backgroundColor = AppColors.Emerald50.copy(alpha = 0.5f)
        )

        StatBox(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.VerticalAlignBottom,
            title = "最低体重",
            value = "${String.format("%.1f", minWeight)} kg",
            backgroundColor = AppColors.Emerald50.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    backgroundColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Emerald600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecordItem(record: WeightRecord) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚖️", fontSize = 20.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${record.weight} kg",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${record.recordDate} ${record.recordTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (record.mood != null) "😊" else "",
                fontSize = 20.sp
            )
        }
    }
}
