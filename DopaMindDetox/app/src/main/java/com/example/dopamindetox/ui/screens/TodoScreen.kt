package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dopamindetox.nav.Screen
import com.example.dopamindetox.vm.MainViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// 📌 1. TodoScreen이 padding을 파라미터로 받도록 수정
@Composable
fun TodoScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    val todos by vm.todos.collectAsState()
    val activities by vm.altActivities.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 📌 2. 내부 Scaffold를 제거하고 Column으로 변경
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding) // NavGraph로부터 전달받은 패딩 적용
    ) {
        TodoTopContent(
            selectedDate = selectedDate,
            onMonthChange = { newYear, newMonth ->
                selectedDate = LocalDate.of(newYear, newMonth, 1)
            },
            onDateSelected = { selectedDate = it }
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.Gray.copy(alpha = 0.3f))

        if (todos.isEmpty() && activities.isEmpty()) {
            EmptyStateUI(navController)
        } else {
            TodoList(todos, activities, vm)
        }
    }
}

// 상단 날짜 선택 영역
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopContent(
    selectedDate: LocalDate,
    onMonthChange: (year: Int, month: Int) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // TopAppBar와 유사한 UI 구성
    TopAppBar(
        title = {
            Box {
                Row(
                    modifier = Modifier.clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedDate.year}년 ${selectedDate.monthValue}월",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "날짜 선택")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("${selectedDate.year - 1}년 (작년)") }, onClick = { onMonthChange(selectedDate.year - 1, selectedDate.monthValue); expanded = false })
                    (1..12).forEach { month ->
                        DropdownMenuItem(text = { Text("${selectedDate.year}년 ${month}월") }, onClick = { onMonthChange(selectedDate.year, month); expanded = false })
                    }
                    DropdownMenuItem(text = { Text("${selectedDate.year + 1}년 (내년)") }, onClick = { onMonthChange(selectedDate.year + 1, selectedDate.monthValue); expanded = false })
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
    HorizontalCalendarView(selectedDate = selectedDate, onDateSelected = onDateSelected)
}

// 가로 캘린더
@Composable
fun HorizontalCalendarView(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val startDate = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
    val dates = List(21) { startDate.plusDays(it.toLong()) }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dates) { date ->
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            val dayOfMonth = date.dayOfMonth.toString()
            val isSelected = date == selectedDate
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            Column(
                modifier = Modifier.clip(CircleShape).background(containerColor).clickable { onDateSelected(date) }.padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dayOfWeek, fontSize = 12.sp, color = contentColor)
                Text(dayOfMonth, fontWeight = FontWeight.Bold, color = contentColor)
            }
        }
    }
}

// 할 일 목록이 비었을 때의 UI
@Composable
fun EmptyStateUI(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("등록된 목표가 없어요", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text("+ 버튼을 눌러 목표를 추가해 주세요", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { navController.navigate(Screen.Recommend.route) }) { Text("🌟 추천 목표 보기") }
            IconButton(
                onClick = { navController.navigate(Screen.AddGoal.route) },
                modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape).size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "목표 추가", modifier = Modifier.size(24.dp))
            }
        }
    }
}

// 할 일 목록 UI
@Composable
fun TodoList(todos: List<com.example.dopamindetox.data.db.Todo>, activities: List<com.example.dopamindetox.data.db.AltActivity>, vm: MainViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(todos, key = { it.id }) { t ->
            ListItem(
                headlineContent = { Text(t.title) },
                supportingContent = { Text(if (t.completed) "완료 (${t.completedAt ?: ""})" else "미완료") },
                trailingContent = { Checkbox(checked = t.completed, onCheckedChange = { vm.toggleTodo(t.id, it) }) }
            )
            Divider()
        }
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
