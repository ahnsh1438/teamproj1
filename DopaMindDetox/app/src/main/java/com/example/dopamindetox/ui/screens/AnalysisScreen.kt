package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dopamindetox.ui.components.CategoryPieChart
import com.example.dopamindetox.ui.components.UsageBarChart
import com.example.dopamindetox.vm.MainViewModel
import androidx.compose.ui.graphics.Color

@Composable
fun AnalysisScreen(vm: MainViewModel) {
    val weekly by vm.weeklyUsage.collectAsState() // package->minutes
    val byCategory by vm.categoryUsage.collectAsState() // category->minutes
    val screenOnCount by vm.screenOnCount.collectAsState()
    val firstUnlockHour by vm.firstUnlockHour.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("나의 앱 사용 차트", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        UsageBarChart(data = weekly.take(7)) // 상위 7개 앱

        Spacer(Modifier.height(16.dp))
        Text("카테고리별 사용 비중", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        CategoryPieChart(data = byCategory)

        Spacer(Modifier.height(16.dp))
        Text("접근 습관 패턴", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("• 화면 켜짐 횟수(오늘): $screenOnCount 회")
        Text("• 오늘 최초 앱 진입 시각: ${String.format("%02d:00", firstUnlockHour)}")
    }
}
