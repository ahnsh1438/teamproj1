package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dopamindetox.vm.MainViewModel

@Composable
fun TodoScreen(vm: MainViewModel) {
    val todos by vm.todos.collectAsState()
    val activities by vm.altActivities.collectAsState()

    var newTodo by remember { mutableStateOf("") }
    var newAct by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("할 일", style = MaterialTheme.typography.titleMedium)
        Row {
            OutlinedTextField(newTodo, { newTodo = it }, Modifier.weight(1f), placeholder={ Text("추가할 할 일") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (newTodo.isNotBlank()) { vm.addTodo(newTodo); newTodo="" } }) { Text("추가") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(todos, key = { it.id }) { t ->
                ListItem(
                    headlineContent = { Text(t.title) },
                    supportingContent = { Text(if (t.completed) "완료 (${t.completedAt ?: ""})" else "미완료") },
                    trailingContent = {
                        Checkbox(checked = t.completed, onCheckedChange = {
                            vm.toggleTodo(t.id, it)
                        })
                    }
                )
                Divider()
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("차단 시 추천받을 나만의 활동", style = MaterialTheme.typography.titleMedium)
        Row {
            OutlinedTextField(newAct, { newAct = it }, Modifier.weight(1f), placeholder={ Text("예: 산책, 물 1컵, 스트레칭") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (newAct.isNotBlank()) { vm.addActivity(newAct); newAct="" } }) { Text("추가") }
        }
        LazyColumn {
            items(activities, key = { it.id }) { a ->
                ListItem(
                    headlineContent = { Text(a.title) },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { vm.renameActivity(a.id, a.title + " ✨") }) { Text("수정") }
                            TextButton(onClick = { vm.deleteActivity(a.id) }) { Text("삭제") }
                        }
                    }
                )
                Divider()
            }
        }
    }
}
