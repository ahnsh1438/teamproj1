package com.example.dopamindetox.overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dopamindetox.DopaApp
import com.example.dopamindetox.ui.theme.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class BlockOverlayActivity : ComponentActivity() {

    /** 오늘 날짜 yyyyMMdd 포맷으로 반환 */
    private fun getTodayKey(): String {
        val today = LocalDate.now()
        return "%04d%02d%02d".format(today.year, today.monthValue, today.dayOfMonth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = (application as DopaApp).repository

        // ⭐ 오늘 날짜 문자열 생성
        val todayKey = getTodayKey()

        // ⭐ 해당 날짜의 todo만 불러오기
        val todos = runBlocking {
            repo.todosByDate(todayKey).first()
        }

        setContent {
            AppTheme {
                Surface {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {

                        Text(
                            "블락 모드",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        Text("지금은 설정한 차단 시간입니다.")

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "해야 할 일",
                            fontSize = 26.sp,
                            fontWeight = MaterialTheme.typography.titleLarge.fontWeight
                        )

                        Spacer(Modifier.height(10.dp))

                        // ⭐ 오늘자 Todo만 표시됨
                        todos.take(5).forEach {
                            Text(
                                "• ${it.title}",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(40.dp))

                        Button(
                            onClick = { finish() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text(
                                "제로도파민으로 돌아가기",
                                fontSize = 18.sp,
                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                            )
                        }
                    }
                }
            }
        }
    }
}
