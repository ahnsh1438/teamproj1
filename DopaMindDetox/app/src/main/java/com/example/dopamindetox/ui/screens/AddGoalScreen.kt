package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dopamindetox.vm.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues,
    dateKey: String   // ⭐ TodoScreen에서 선택된 날짜가 전달됨 (yyyyMMdd)
) {
    var title by remember { mutableStateOf("") }

    // ⭐ dateKey → LocalDate 변환
    val parsedDate =
        runCatching { LocalDate.parse(dateKey, DateTimeFormatter.ofPattern("yyyyMMdd")) }
            .getOrNull() ?: LocalDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 목표 추가") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        modifier = Modifier.padding(padding)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("목표 제목", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 운동 30분, 독서 20분") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "선택된 날짜: ${parsedDate.year}년 ${parsedDate.monthValue}월 ${parsedDate.dayOfMonth}일",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        vm.addTodo(title, dateKey)   // ⭐ 날짜 포함 저장
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("목표 추가")
            }
        }
    }
}
