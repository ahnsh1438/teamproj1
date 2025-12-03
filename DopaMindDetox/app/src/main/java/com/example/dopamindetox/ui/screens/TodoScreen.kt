package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.background
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
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import androidx.compose.material.icons.filled.Delete
import java.util.*

@Composable
fun TodoScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    // ⭐ ViewModel에서 날짜별 Todo Flow를 가져온다
    val todos by vm.todosByDate.collectAsState()
    val activities by vm.altActivities.collectAsState()

    // ⭐ 화면 상단 달력은 LocalDate 기반으로 유지
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 화면 진입 시 오늘 날짜로 초기화
    LaunchedEffect(Unit) {
        updateVmDate(vm, selectedDate)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {

        Column {
            TodoTopContent(
                selectedDate = selectedDate,
                onMonthChange = { year, month ->
                    selectedDate = LocalDate.of(year, month, 1)
                    updateVmDate(vm, selectedDate)
                },
                onDateSelected = {
                    selectedDate = it
                    updateVmDate(vm, selectedDate)
                }
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

        /* ------------------- Floating Buttons ------------------- */

        // 추천 활동
        FloatingActionButton(
            onClick = { navController.navigate(Screen.Recommend.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(Icons.Default.Star, contentDescription = "추천 활동")
        }

        // 목표 추가
        FloatingActionButton(
            onClick = {
                navController.navigate(Screen.AddGoal.route + "/${formatDateKey(selectedDate)}")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "목표 추가")
        }
    }
}

/* -------------------- 날짜 선택 UI -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopContent(
    selectedDate: LocalDate,
    onMonthChange: (Int, Int) -> Unit,
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
                    // 지난 해
                    DropdownMenuItem(
                        text = { Text("${selectedDate.year - 1}년") },
                        onClick = {
                            onMonthChange(selectedDate.year - 1, selectedDate.monthValue)
                            expanded = false
                        }
                    )

                    // 1~12월
                    (1..12).forEach { month ->
                        DropdownMenuItem(
                            text = { Text("${selectedDate.year}년 ${month}월") },
                            onClick = {
                                onMonthChange(selectedDate.year, month)
                                expanded = false
                            }
                        )
                    }

                    // 다음 해
                    DropdownMenuItem(
                        text = { Text("${selectedDate.year + 1}년") },
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

/* --------------------  날짜 가로 스크롤 -------------------- */

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
            val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            val day = date.dayOfMonth
            val selected = date == selectedDate

            val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
            val textColor =
                if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dow, color = textColor)
                Text("$day", fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

/* -------------------- 빈 화면 -------------------- */

@Composable
fun EmptyStateUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("등록된 목표가 없어요", color = Color.Gray)
        Text("+ 버튼을 눌러 새로운 목표를 추가하세요", color = Color.Gray)
    }
}

/* -------------------- Todo 리스트 -------------------- */

@Composable
fun TodoList(
    todos: List<com.example.dopamindetox.data.db.Todo>,
    activities: List<com.example.dopamindetox.data.db.AltActivity>,
    vm: MainViewModel
) {
    var editDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var editId by remember { mutableStateOf(0L) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        /* ---- Todo 목록 ---- */
        items(todos, key = { it.id }) { t ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(t.title)
                    Text(
                        if (t.completed) "완료 (${t.completedAt ?: ""})" else "미완료",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = {
                        editId = t.id
                        editText = t.title
                        editDialog = true
                    }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "수정",
                        tint = Color.Blue
                    )
                }

                // 🗑 삭제 버튼 (⭐ 새로 추가됨)
                IconButton(
                    onClick = {
                        vm.deleteTodo(t.id)
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color.Red
                    )
                }

                Checkbox(
                    checked = t.completed,
                    onCheckedChange = { vm.toggleTodo(t.id, it) }
                )
            }

            Divider()
        }

        /* ---- Activity 목록 ---- */
        items(activities, key = { it.id }) { a ->
            ListItem(
                headlineContent = { Text(a.title) }
            )
            Divider()
        }
    }

    /* ---- 수정 다이얼로그 ---- */

    if (editDialog) {
        AlertDialog(
            onDismissRequest = { editDialog = false },
            title = { Text("목표 수정") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editText.isNotBlank()) vm.renameTodo(editId, editText)
                        editDialog = false
                    }
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { editDialog = false }) { Text("취소") }
            }
        )
    }
}

/* -------------------- 날짜 키 변환 -------------------- */

fun updateVmDate(vm: MainViewModel, date: LocalDate) {
    vm.updateSelectedDate(formatDateKey(date))
}

fun formatDateKey(date: LocalDate): String {
    return DateTimeFormatter.ofPattern("yyyyMMdd").format(date)
}
