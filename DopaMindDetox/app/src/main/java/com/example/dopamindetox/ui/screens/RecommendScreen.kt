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
        "30분 산책하고 오기" to "스마트폰은 집에 두고 가볍게 동네 한 바퀴 걸어보세요.",
        "15분 스트레칭 루틴 하기" to "거북목, 허리풀기 중심 스트레칭으로 몸 회복하기.",
        "물 한 잔 마시고 10분 명상하기" to "짧게라도 스마트폰을 내려놓고 호흡에 집중해보세요.",
        "20분 책 읽기 도전" to "스마트폰 대신 종이책 또는 전자책(비SNS 앱) 읽기."
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
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),   // ⭐ 추가됨: 버튼 정렬 기준 고정
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
                                val today = java.time.LocalDate.now()
                                val dateKey = "%04d%02d%02d".format(
                                    today.year, today.monthValue, today.dayOfMonth
                                )

                                vm.addTodo(title, dateKey)
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

