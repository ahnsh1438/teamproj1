package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dopamindetox.vm.MainViewModel
import com.example.dopamindetox.nav.Screen

@Composable
fun RecommendScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    val recommendations = listOf(
        "SNS 줄이기 챌린지" to "SNS 앱 대신 산책/독서 하기",
        "영상 시청 줄이기" to "유튜브 대신 30분 산책하기",
        "아침 집중 루틴" to "하루 목표 적고 20분 집중하기",
        "습관 개선 미션" to "취침 전 폰 down 후 책 읽기"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text("추천 목표", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            recommendations.forEach { (title, desc) ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Button(
                            onClick = {
                                // ⭐ 오늘 날짜 생성
                                val today = java.time.LocalDate.now()
                                val dateKey = "%04d%02d%02d".format(
                                    today.year, today.monthValue, today.dayOfMonth
                                )

                                // ⭐ 해당 날짜에 Todo 저장
                                vm.addTodo(title, dateKey)

                                // 뒤로 이동
                                navController.popBackStack()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("추가하기")
                        }
                    }
                }
            }
        }
    }
}
