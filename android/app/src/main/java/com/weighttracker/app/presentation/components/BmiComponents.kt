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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weighttracker.app.presentation.theme.AppColors

enum class BmiCategory(val range: ClosedFloatingPointRange<Double>, val color: Color, val label: String) {
    UNDERWEIGHT(0.0..18.4, Color(0xFF3B82F6), "偏低"),
    NORMAL(18.5..23.9, Color(0xFF22C55E), "标准"),
    OVERWEIGHT(24.0..27.9, Color(0xFFF59E0B), "偏高"),
    OBESE(28.0..Double.MAX_VALUE, Color(0xFFEF4444), "过高")
}

data class BmiResult(
    val bmi: Double,
    val category: BmiCategory,
    val advice: String
)

fun calculateBmi(weightKg: Double, heightCm: Double): BmiResult {
    val heightM = heightCm / 100.0
    val bmi = weightKg / (heightM * heightM)
    
    val category = when {
        bmi < 18.5 -> BmiCategory.UNDERWEIGHT
        bmi < 24.0 -> BmiCategory.NORMAL
        bmi < 28.0 -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }
    
    val advice = when (category) {
        BmiCategory.UNDERWEIGHT -> "BMI偏低，建议增加营养摄入，适当进行力量训练增加肌肉量。保证每日蛋白质摄入，多吃优质蛋白如鸡蛋、鱼肉、豆制品等。"
        BmiCategory.NORMAL -> "BMI在正常范围内，请继续保持良好的生活习惯。建议每周保持150分钟中等强度有氧运动，均衡饮食。"
        BmiCategory.OVERWEIGHT -> "BMI偏高，建议每周坚持运动锻炼，增加有氧运动如跑步、跳绳等。与此同时控制饮食总摄入量，增加膳食纤维和优质蛋白的摄入，少吃油炸等高热量的食物。"
        BmiCategory.OBESE -> "BMI过高，建议尽快就医咨询专业意见。在医生指导下制定科学的减重计划，循序渐进地调整饮食结构，配合适当运动。"
    }
    
    return BmiResult(bmi = bmi, category = category, advice = advice)
}

@Composable
fun BmiCard(
    weightKg: Double,
    heightCm: Double,
    modifier: Modifier = Modifier
) {
    val bmiResult = calculateBmi(weightKg, heightCm)
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = AppColors.Emerald600,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "需关注指标",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // BMI Value and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", bmiResult.bmi),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "BMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(50),
                    color = bmiResult.category.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = bmiResult.category.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = bmiResult.category.color,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // BMI Bar
            BmiProgressBar(currentBmi = bmiResult.bmi)
            
            Spacer(Modifier.height(16.dp))
            
            // Health Advice
            Text(
                text = bmiResult.advice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun BmiProgressBar(
    currentBmi: Double,
    modifier: Modifier = Modifier
) {
    val minBmi = 14.0
    val maxBmi = 35.0
    val barHeight = 12.dp
    
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val width = size.width
            val height = size.height
            
            val underweightEnd = ((18.5 - minBmi) / (maxBmi - minBmi)).toFloat()
            val normalEnd = ((24.0 - minBmi) / (maxBmi - minBmi)).toFloat()
            val overweightEnd = ((28.0 - minBmi) / (maxBmi - minBmi)).toFloat()
            
            drawRect(
                color = BmiCategory.UNDERWEIGHT.color,
                topLeft = Offset.Zero,
                size = Size(width * underweightEnd, height),
                style = Fill
            )
            drawRect(
                color = BmiCategory.NORMAL.color,
                topLeft = Offset(width * underweightEnd, 0f),
                size = Size(width * (normalEnd - underweightEnd), height),
                style = Fill
            )
            drawRect(
                color = BmiCategory.OVERWEIGHT.color,
                topLeft = Offset(width * normalEnd, 0f),
                size = Size(width * (overweightEnd - normalEnd), height),
                style = Fill
            )
            drawRect(
                color = BmiCategory.OBESE.color,
                topLeft = Offset(width * overweightEnd, 0f),
                size = Size(width * (1f - overweightEnd), height),
                style = Fill
            )
            
            val indicatorPosition = ((currentBmi.coerceIn(minBmi, maxBmi) - minBmi) / (maxBmi - minBmi)).toFloat()
            val indicatorX = width * indicatorPosition
            
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(indicatorX, -2.dp.toPx())
                    lineTo(indicatorX - 6.dp.toPx(), -10.dp.toPx())
                    lineTo(indicatorX + 6.dp.toPx(), -10.dp.toPx())
                    close()
                },
                color = onSurfaceColor
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "18.5",
                style = MaterialTheme.typography.labelSmall,
                color = BmiCategory.UNDERWEIGHT.color
            )
            Text(
                text = "24.0",
                style = MaterialTheme.typography.labelSmall,
                color = BmiCategory.NORMAL.color
            )
            Text(
                text = "28.0",
                style = MaterialTheme.typography.labelSmall,
                color = BmiCategory.OVERWEIGHT.color
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BmiLegendItem(label = "偏低", color = BmiCategory.UNDERWEIGHT.color)
            BmiLegendItem(label = "标准", color = BmiCategory.NORMAL.color)
            BmiLegendItem(label = "偏高", color = BmiCategory.OVERWEIGHT.color)
            BmiLegendItem(label = "过高", color = BmiCategory.OBESE.color)
        }
    }
}

@Composable
private fun BmiLegendItem(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
