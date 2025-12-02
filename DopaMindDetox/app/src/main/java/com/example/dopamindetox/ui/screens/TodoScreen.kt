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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dopamindetox.nav.Screen
import com.example.dopamindetox.vm.MainViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun TodoScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    val todos by vm.todos.collectAsState()
    val activities by vm.altActivities.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 화면 재진입 시 오늘 날짜로 리셋
    LaunchedEffect(Unit) {
        selectedDate = LocalDate.now()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {

        Column {
            TodoTopContent(
                selectedDate = selectedDate,
                onMonthChange = { newYear, newMonth ->
                    selectedDate = LocalDate.of(newYear, newMonth, 1)
                },
                onDateSelected = { selectedDate = it }
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray.copy(alpha = 0.3f)
            )

            if (todos.isEmpty() && activities.isEmpty()) {
                EmptyStateUI()
            } else {
                TodoList(todos, activities, vm)
            }
        }

        /* ------------------- FAB 두 개: 추천 버튼 + 목표추가 버튼 ------------------- */

        // ★ 추천 목표 버튼 (위)
        FloatingActionButton(
            onClick = { navController.navigate(Screen.Recommend.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(Icons.Default.Star, contentDescription = "추천 목표 보기")
        }

        // + 새로운 목표 추가 버튼 (아래)
        FloatingActionButton(
            onClick = { navController.navigate(Screen.AddGoal.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "목표 추가")
        }
    }
}


/* -------------------------- 날짜 선택 상단 UI -------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopContent(
    selectedDate: LocalDate,
    onMonthChange: (year: Int, month: Int) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
                    DropdownMenuItem(
                        text = { Text("${selectedDate.year - 1}년 (작년)") },
                        onClick = {
                            onMonthChange(selectedDate.year - 1, selectedDate.monthValue)
                            expanded = false
                        }
                    )
                    (1..12).forEach { month ->
                        DropdownMenuItem(
                            text = { Text("${selectedDate.year}년 ${month}월") },
                            onClick = {
                                onMonthChange(selectedDate.year, month)
                                expanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("${selectedDate.year + 1}년 (내년)") },
                        onClick = {
                            onMonthChange(selectedDate.year + 1, selectedDate.monthValue)
                            expanded = false
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )

    HorizontalCalendarView(selectedDate = selectedDate, onDateSelected = onDateSelected)
}


/* -------------------------- 가로 달력 -------------------------- */

@Composable
fun HorizontalCalendarView(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val startDate = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
    val dates = List(21) { startDate.plusDays(it.toLong()) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dates) { date ->
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            val dayOfMonth = date.dayOfMonth
            val isSelected = date == selectedDate

            val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val textColor =
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dayOfWeek, color = textColor)
                Text("$dayOfMonth", fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}


/* -------------------------- 비어있는 화면 안내 -------------------------- */

@Composable
fun EmptyStateUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("등록된 목표가 없어요", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text("+ 버튼을 눌러 목표를 추가해 주세요", color = Color.Gray)
    }
}


/* -------------------------- Todo + Activity 리스트 -------------------------- */

@Composable
fun TodoList(
    todos: List<com.example.dopamindetox.data.db.Todo>,
    activities: List<com.example.dopamindetox.data.db.AltActivity>,
    vm: MainViewModel
) {
    var editDialogVisible by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var editId by remember { mutableStateOf(0L) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(todos, key = { it.id }) { t ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 제목 및 상태
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(t.title)
                    Text(
                        if (t.completed) "완료 (${t.completedAt ?: ""})"
                        else "미완료",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                // ✏️ 연필 아이콘 (수정)
                IconButton(
                    onClick = {
                        editId = t.id
                        editText = t.title
                        editDialogVisible = true
                    }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "수정",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { vm.deleteTodo(t.id) }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color.Red
                    )
                }

                // ☑ 체크박스
                Checkbox(
                    checked = t.completed,
                    onCheckedChange = {
                        vm.toggleTodo(t.id, it)
                    }
                )
            }

            Divider()
        }

        // 기존 Activity 영역 그대로 유지
        items(activities, key = { it.id }) { a ->
            ListItem(
                headlineContent = { Text(a.title) },
                trailingContent = {
                    Row {
                        TextButton(onClick = { vm.renameActivity(a.id, a.title + " ✨") }) {
                            Text("수정")
                        }
                        TextButton(onClick = { vm.deleteActivity(a.id) }) {
                            Text("삭제")
                        }
                    }
                }
            )
            Divider()
        }
    }

    /* -------------------------- Todo 수정 다이얼로그 -------------------------- */

    if (editDialogVisible) {
        AlertDialog(
            onDismissRequest = { editDialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editText.isNotBlank()) {
                            vm.renameTodo(editId, editText)
                        }
                        editDialogVisible = false
                    }
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { editDialogVisible = false }) {
                    Text("취소")
                }
            },
            title = { Text("목표 수정") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}
