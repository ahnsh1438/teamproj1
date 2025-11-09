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
        val acts = runBlocking { repo.activities().first() }

        setContent {
            AppTheme {
                Surface {
                    Column(Modifier.fillMaxSize().padding(24.dp)) {
                        Text("블락 모드", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("지금은 설정한 차단 시간입니다.")
                        Spacer(Modifier.height(16.dp))
                        Text("해야 할 일", style = MaterialTheme.typography.titleMedium)
                        todos.take(5).forEach { Text("• ${it.title}") }
                        Spacer(Modifier.height(16.dp))
                        Text("추천 활동", style = MaterialTheme.typography.titleMedium)
                        acts.take(5).forEach { Text("• ${it.title}") }
                        Spacer(Modifier.height(24.dp))
                        Text("목표 달성 시 트로피가 제공됩니다 🏆")
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { finish() }) { Text("돌아가기") }
                    }
                }
            }
        }
    }
}
