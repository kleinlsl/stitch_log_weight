package com.weighttracker.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.weighttracker.app.presentation.theme.AppColors

@Composable
fun WeightChangeBadge(
    change: Double?,
    modifier: Modifier = Modifier
) {
    if (change == null) return

    val isDown = change < 0
    val color = if (isDown) AppColors.Emerald600 else MaterialTheme.colorScheme.error
    val icon = if (isDown) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp

    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.height(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "较上周 ${if (isDown) "" else "+"}${String.format("%.1f", change)} KG",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
fun MiniTrendChart(
    heights: List<Float>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEachIndexed { index, height ->
            val isLast = index == heights.lastIndex
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height((height * 100).dp)
                        .background(
                            if (isLast) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }
    }
}

@Composable
fun WeightLineChart(
    weights: List<Double>,
    dates: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
) {
    if (weights.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(120.dp)) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    val minWeight = weights.minOrNull() ?: 0.0
    val maxWeight = weights.maxOrNull() ?: 100.0
    val range = (maxWeight - minWeight).coerceAtLeast(1.0)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx()
        
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        
        val stepX = if (weights.size > 1) chartWidth / (weights.size - 1) else chartWidth
        
        val points = weights.mapIndexed { index, weight ->
            val x = padding + index * stepX
            val normalizedY = (weight - minWeight) / range
            val y = height - padding - (normalizedY * chartHeight).toFloat()
            Offset(x, y)
        }
        
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, height - padding)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, height - padding)
                close()
            }
            drawPath(fillPath, fillColor)
            
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                linePath,
                lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }
        
        val minY = height - padding
        val maxY = padding
        drawLine(
            color = lineColor.copy(alpha = 0.3f),
            start = Offset(padding, minY),
            end = Offset(padding, maxY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun ChartLabels(
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (labels.isEmpty()) return
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
