package com.example.dopamindetox.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavController
import com.example.dopamindetox.vm.MainViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalContext

data class AppInfoItem(
    val packageName: String,
    val label: String,
    val category: String,
    val icon: Drawable?
)

@Composable
fun MainScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues // MainActivity의 Scaffold로부터 패딩을 전달받음
) {
    val ctx = LocalContext.current
    val blocked by vm.blockedApps.collectAsState()
    val goal by vm.goalMinutes.collectAsState()
    val isServiceRunning by vm.isServiceRunning.collectAsState()

    var search by remember { mutableStateOf(TextFieldValue("")) }
    var input by remember { mutableStateOf(goal.toString()) }

    val apps by remember {
        mutableStateOf(loadAppList(ctx))
    }

    // goal 값이 변경될 때 input을 업데이트합니다.
    LaunchedEffect(goal) {
        input = goal.toString()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding) // 전달받은 패딩 적용
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text("목표 시간 (분)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))


        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter(Char::isDigit) },
            suffix = { Text("분") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row {
            Button(onClick = { vm.saveGoal(input.toIntOrNull() ?: 0) }) { Text("저장") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                vm.saveGoal(60)
                input = "60"
            }) {
                Text("추천 실행 (60분)")
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("차단할 앱 선택", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("앱 검색…") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        val filteredApps = apps.filter {
            it.label.contains(search.text, ignoreCase = true) ||
                    it.packageName.contains(search.text, ignoreCase = true)
        }

        val grouped = filteredApps.groupBy { it.category }
        val categories = listOf("SNS", "영상 / 스트리밍", "게임", "생산성", "기타")

        LazyColumn(Modifier.weight(1f)) {
            categories.forEach { category ->
                val itemsInCategory = grouped[category] ?: emptyList()
                if (itemsInCategory.isNotEmpty()) {
                    item {
                        Text(
                            text = category,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(itemsInCategory, key = { app -> app.packageName }) { app ->
                        val checked = blocked.any { it.packageName == app.packageName }
                        ListItem(
                            leadingContent = {
                                Image(
                                    painter = rememberAsyncImagePainter(model = app.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp)
                                )
                            },
                            headlineContent = { Text(app.label) },
                            supportingContent = { Text(app.packageName) },
                            trailingContent = {
                                Switch(
                                    checked = checked,
                                    onCheckedChange = {
                                        if (it) vm.addBlocked(app.packageName, app.label)
                                        else vm.removeBlocked(app.packageName)
                                    }
                                )
                            }
                        )
                        Divider()
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val buttonText = if (isServiceRunning) "차단 서비스 종료" else "차단 서비스 시작"
        val buttonColors = if (isServiceRunning) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        else ButtonDefaults.buttonColors()

        Button(
            onClick = {
                if (isServiceRunning) vm.stopMonitoringService()
                else vm.startMonitoringService()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = buttonColors
        ) {
            Text(buttonText)
        }
        Spacer(Modifier.height(16.dp))
    }
}

fun loadAppList(ctx: Context): List<AppInfoItem> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(intent, 0)

    return resolveInfos.map { info ->
        val appInfo = info.activityInfo.applicationInfo
        AppInfoItem(
            packageName = info.activityInfo.packageName,
            label = info.loadLabel(pm).toString(),
            category = categorizeApp(appInfo),
            icon = try {
                pm.getApplicationIcon(appInfo)
            } catch (e: Exception) {
                null
            }
        )
    }.sortedBy { it.label }
}

fun categorizeApp(appInfo: ApplicationInfo): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo.category != ApplicationInfo.CATEGORY_UNDEFINED) {
        return when (appInfo.category) {
            ApplicationInfo.CATEGORY_GAME -> "게임"
            ApplicationInfo.CATEGORY_SOCIAL -> "SNS"
            ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE -> "영상 / 스트리밍"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "생산성"
            else -> "기타"
        }
    }

    val pkg = appInfo.packageName
    val table = mapOf(
        "com.instagram" to "SNS", "com.facebook" to "SNS", "com.kakao.talk" to "SNS",
        "com.snapchat" to "SNS", "com.twitter" to "SNS", "com.xingin.xhs" to "SNS",
        "com.google.android.youtube" to "영상 / 스트리밍", "com.netflix.mediaclient" to "영상 / 스트리밍",
        "tv.twitch.android.app" to "영상 / 스트리밍", "com.disney.disneyplus" to "영상 / 스트리밍",
        "com.wavve" to "영상 / 스트리밍",
        "com.supercell" to "게임", "com.tencent" to "게임", "com.nexon" to "게임", "com.netmarble" to "게임",
        "com.google.android.apps.docs" to "생산성", "com.google.android.keep" to "생산성",
        "com.microsoft.office" to "생산성", "notion.id" to "생산성", "com.todoist" to "생산성"
    )

    for ((key, value) in table) if (pkg.startsWith(key)) return value

    return when {
        pkg.contains("youtube", true) -> "영상 / 스트리밍"
        pkg.contains("kakao", true) -> "SNS"
        pkg.contains("insta", true) -> "SNS"
        pkg.contains("game", true) -> "게임"
        pkg.contains("office", true) -> "생산성"
        else -> "기타"
    }
}
