package com.example.dopamindetox.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dopamindetox.vm.MainViewModel

@Composable
fun MainScreen(vm: MainViewModel) {
    val ctx = LocalContext.current
    val blocked by vm.blockedApps.collectAsState()
    val goal by vm.goalMinutes.collectAsState()
    val recommended by vm.recommendedGoal.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("목표 시간 (분)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        var input by remember { mutableStateOf(goal.toString()) }
        OutlinedTextField(value = input, onValueChange = { input = it.filter(Char::isDigit) },
            suffix = { Text("분") })
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = { vm.saveGoal(input.toIntOrNull() ?: 0) }) { Text("저장") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.saveGoal(recommended) }) { Text("자동 추천 사용 (${recommended}분)") }
        }

        Spacer(Modifier.height(20.dp))
        Text("차단할 앱 선택", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val apps = remember {
            val pm = ctx.packageManager
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .sortedBy { pm.getApplicationLabel(it).toString() }
        }

        LazyColumn(Modifier.weight(1f)) {
            items(apps) { app ->
                val pkg = app.packageName
                val name = ctx.packageManager.getApplicationLabel(app).toString()
                val checked = blocked.any { it.packageName == pkg }
                ListItem(
                    headlineContent = { Text(name) },
                    supportingContent = { Text(pkg) },
                    trailingContent = {
                        Switch(checked = checked, onCheckedChange = {
                            if (it) vm.addBlocked(pkg, name) else vm.removeBlocked(pkg)
                        })
                    }
                )
                Divider()
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.startMonitoringService() }, modifier = Modifier.fillMaxWidth()) {
            Text("차단 서비스 시작/재시작")
        }
    }
}
