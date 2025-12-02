package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dopamindetox.vm.MainViewModel

@Composable
fun AddGoalScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "새 목표 추가",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("목표 제목") },
            singleLine = true
        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                if (title.text.isNotBlank()) {
                    vm.addTodo(title.text)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.text.isNotBlank()
        ) {
            Text("추가하기")
        }
    }
}
