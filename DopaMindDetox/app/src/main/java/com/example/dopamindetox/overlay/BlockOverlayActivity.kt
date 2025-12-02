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

class BlockOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as DopaApp).repository
        val todos = runBlocking { repo.todos().first() }

        setContent {
            AppTheme {
                Surface {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {

                        // 상단 제목
                        Text(
                            "블락 모드",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        Text("지금은 설정한 차단 시간입니다.")

                        Spacer(Modifier.height(20.dp))

                        // 🟣 해야 할 일 — 글자 크게!
                        Text(
                            "해야 할 일",
                            fontSize = 26.sp,      // 글자 크기 증가
                            fontWeight = MaterialTheme.typography.titleLarge.fontWeight
                        )

                        Spacer(Modifier.height(10.dp))

                        // 해야 할 일 목록
                        todos.take(5).forEach {
                            Text(
                                "• ${it.title}",
                                fontSize = 18.sp,      // 항목도 크게
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(40.dp))


                        Button(
                            onClick = { finish() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),       // 버튼 키움
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "제로도파민으로 돌아가기",
                                fontSize = 18.sp,     // 버튼 글씨 크게
                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                            )
                        }
                    }
                }
            }
        }
    }
}
