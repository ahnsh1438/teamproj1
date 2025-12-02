package com.example.dopamindetox.ui.screens

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.dopamindetox.vm.MainViewModel

@Composable
fun FirstEntryScreen(onContinue: () -> Unit, vm: MainViewModel) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 권한 상태
    var hasOverlay by remember { mutableStateOf(false) }
    var hasUsage by remember { mutableStateOf(false) }
    var hasNoti by remember { mutableStateOf(false) }

    // 모든 권한이 허용되었을 때만 '시작하기' 활성화
    val allPermissionsGranted = hasOverlay && hasUsage && hasNoti

    // 앱 돌아올 때 권한 체크
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlay = Settings.canDrawOverlays(ctx)
                hasUsage = hasUsageAccess(ctx)
                hasNoti = checkNotificationPermission(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("필수 권한 설정", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("• 알림(Notifications)\n• 다른 앱 위에 표시\n• 사용량 접근(Usage Access)")
        Spacer(Modifier.height(24.dp))

        // 🔔 알림 권한
        Button(
            onClick = { requestNotificationPermission(ctx) }
        ) { Text(if (hasNoti) "알림 권한 완료" else "알림 권한 허용") }

        Spacer(Modifier.height(12.dp))

        // 🪟 오버레이 권한
        Button(onClick = {
            ctx.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${ctx.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }) { Text(if (hasOverlay) "오버레이 권한 완료" else "오버레이 권한 열기") }

        Spacer(Modifier.height(12.dp))

        // 📊 사용량 접근
        Button(onClick = {
            ctx.startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }) { Text(if (hasUsage) "사용량 접근 완료" else "사용량 접근 열기") }

        Spacer(Modifier.height(24.dp))

        // ▶️ 시작하기
        Button(
            enabled = allPermissionsGranted,
            onClick = {
                onContinue()
            }
        ) { Text("시작하기") }
    }
}

private fun hasUsageAccess(context: Context): Boolean {
    return try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false

        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        mode == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }
}

private fun checkNotificationPermission(ctx: Context): Boolean {
    return if (Build.VERSION.SDK_INT < 33) {
        true // 하위 버전은 자동 허용
    } else {
        ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

private fun requestNotificationPermission(ctx: Context) {
    if (Build.VERSION.SDK_INT >= 33) {
        val act = ctx as? android.app.Activity ?: return
        ActivityCompat.requestPermissions(
            act,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }
}
