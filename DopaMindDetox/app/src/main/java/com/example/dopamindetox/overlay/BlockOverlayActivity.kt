package com.example.dopamindetox.overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dopamindetox.DopaApp
import com.example.dopamindetox.ui.theme.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BlockOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = (application as DopaApp).repository

        // ⭐ 오늘 날짜 키 생성
        val todayKey = java.time.LocalDate.now().let {
            "%04d%02d%02d".format(it.year, it.monthValue, it.dayOfMonth)
        }

        // ⭐ 오늘 Todo 목록 로드
        val todayTodos = runBlocking {
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

                        Text("제로도파민", fontSize = 20.sp)

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "오늘 해야 할 일",
                            fontSize = 26.sp,
                            fontWeight = MaterialTheme.typography.titleLarge.fontWeight
                        )

                        Spacer(Modifier.height(16.dp))

                        // ⭐ 아이콘을 오른쪽으로 이동한 버전
                        todayTodos.forEach { todo ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 왼쪽: 제목
                                Text(
                                    todo.title,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                // 오른쪽: 체크 또는 X
                                val icon = if (todo.completed) "✔️" else "❌"
                                val color =
                                    if (todo.completed) Color(0xFF4CAF50) else Color(0xFFE53935)

                                Text(
                                    icon,
                                    color = color,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(40.dp))

                        Button(
                            onClick = { finish() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = MaterialTheme.shapes.medium
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
