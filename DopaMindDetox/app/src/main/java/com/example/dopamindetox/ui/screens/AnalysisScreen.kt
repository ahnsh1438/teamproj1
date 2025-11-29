package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.example.dopamindetox.AppUsageInfo
import com.example.dopamindetox.UsageDataManager
import com.example.dopamindetox.vm.MainViewModel
import java.util.concurrent.TimeUnit

// 색상 정의
val SkyBlue = Color(0xFF6485FF)
val LightBlueBox = Color(0xFFEFF4FF)

@Composable
fun AnalysisScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    val context = LocalContext.current
    val usageManager = remember { UsageDataManager(context) }

    var hasPermission by remember { mutableStateOf(false) }
    var weeklyData by remember { mutableStateOf(listOf<Pair<String, Long>>()) }
    var topApps by remember { mutableStateOf(listOf<AppUsageInfo>()) }
    var screenOnCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        hasPermission = usageManager.hasPermission()
        if (hasPermission) {
            weeklyData = usageManager.getWeeklyUsage()
            val (apps, count) = usageManager.getTodayStats()
            topApps = apps
            screenOnCount = count
        }
    }

    // 전체 화면 구성을 잡는 박스
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding) // 하단 탭바 높이만큼 띄우기
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "분석 리포트",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        // 구분선 (선택사항: 상단바와 내용 사이 얇은 선)
        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)



        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 남은 공간을 다 차지하게 함
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (!hasPermission) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("데이터를 불러올 수 없습니다", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { usageManager.requestPermission() }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)) {
                            Text("권한 설정하러 가기")
                        }
                    }
                }
            } else {
                // 1. 차트
                Text("나의 앱 사용 차트 (최근 7일)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightBlueBox),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        DailyUsageChartInternal(weeklyData)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. 습관 패턴
                Text("접근 습관 패턴", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightBlueBox),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("오늘 화면 켠 횟수", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$screenOnCount 회", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SkyBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Top 5
                Text("오늘 최다 사용 앱 Top 5", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))

                if (topApps.isEmpty()) {
                    Text("오늘 사용 기록이 없습니다.", color = Color.Gray)
                } else {
                    topApps.forEachIndexed { index, app ->
                        AppUsageItemInternal(index + 1, app)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp)) // 하단 여백
        }
    }
}

// --- 내부 컴포넌트 ---
@Composable
fun DailyUsageChartInternal(data: List<Pair<String, Long>>) {
    val maxTime = data.maxOfOrNull { it.second } ?: 1L
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (day, time) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier
                    .width(16.dp)
                    .weight(if (time == 0L) 0.01f else (time.toFloat() / maxTime.toFloat()))
                    .background(if (day == "오늘") SkyBlue else Color(0xFFD0DDFE), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(day.takeLast(2), fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AppUsageItemInternal(rank: Int, app: AppUsageInfo) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$rank", fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
        if (app.icon != null) {
            Image(bitmap = app.icon.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp))
        } else {
            Box(modifier = Modifier.size(40.dp).background(Color.Gray))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.appName, fontWeight = FontWeight.Medium)
            val hours = TimeUnit.MILLISECONDS.toHours(app.usageTime)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(app.usageTime) % 60

            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(3.dp))) {
                Box(modifier = Modifier
                    .fillMaxWidth(minOf(1f, (app.usageTime.toFloat() / (3600000 * 3))))
                    .fillMaxHeight()
                    .background(SkyBlue, RoundedCornerShape(3.dp)))
            }
            Text("${hours}시간 ${minutes}분", fontSize = 12.sp, color = Color.Gray)
        }
    }
}