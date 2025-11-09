package com.example.dopamindetox.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kotlin.math.min
import androidx.compose.ui.graphics.Color

@Composable
fun UsageBarChart(data: List<Pair<String, Int>>) {
    val max = (data.maxOfOrNull { it.second } ?: 1).toFloat()
    Column(Modifier.fillMaxWidth()) {
        data.forEach { (name, v) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(name.take(12).padEnd(12), modifier = Modifier.width(120.dp))
                Canvas(Modifier.weight(1f).height(18.dp)) {
                    val w = size.width * (v / max)
                    drawRect(color = Color.Blue, topLeft = Offset.Zero, size = Size(100f, 100f))
                }
                Text(" ${v}m")
            }
        }
    }
}

@Composable
fun CategoryPieChart(data: Map<String, Int>) {
    val total = data.values.sum().coerceAtLeast(1)
    val entries = data.entries.sortedByDescending { it.value }

    // 간단 팔레트 (원하는 색 추가/변경 가능)
    val palette = listOf(
        Color(0xFF5C7CFA),
        Color(0xFF51CF66),
        Color(0xFFFF922B),
        Color(0xFFFF6B6B),
        Color(0xFF845EF7),
        Color(0xFF339AF0),
        Color(0xFF12B886),
        Color(0xFFF06595)
    )

    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val diameter = min(size.width, size.height)
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        var start = -90f // 12시 방향부터 시작
        entries.forEachIndexed { idx, (_, v) ->
            val sweep = 360f * (v.toFloat() / total.toFloat())
            drawArc(
                color = palette[idx % palette.size], // ✅ color 인자 추가
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )
            start += sweep
        }
    }
}
