package com.example.dopamindetox.ui.screens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dopamindetox.vm.MainViewModel
import androidx.core.app.AppOpsManagerCompat

@Composable
fun FirstEntryScreen(onContinue: () -> Unit, vm: MainViewModel) {
    val ctx = LocalContext.current
    val hasOverlay = Settings.canDrawOverlays(ctx)
    val hasUsage = hasUsageAccess(ctx)

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("필수 권한 설정", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("• 다른 앱 위에 표시\n• 사용량 접근(Usage Access)\n이 필요합니다.")
        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            if (!Settings.canDrawOverlays(ctx)) {
                ctx.startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${ctx.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }) { Text(if (hasOverlay) "오버레이 권한 완료" else "오버레이 권한 열기") }

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }) { Text(if (hasUsage) "사용량 접근 완료" else "사용량 접근 열기") }

        Spacer(Modifier.height(24.dp))
        Button(
            enabled = hasOverlay && hasUsage,
            onClick = {
                vm.startMonitoringService()
                onContinue()
            }
        ) { Text("시작하기") }
    }
}

@Suppress("DEPRECATION")
private fun hasUsageAccess(context: Context): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        // ✅ Android 10(API 29)+
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(),
            context.packageName
        )
        mode == AppOpsManager.MODE_ALLOWED
    } else {
        // ✅ Android 9(API 28) 이하
        val mode = AppOpsManagerCompat.noteOpNoThrow(
            context,
            "android:get_usage_stats",
            android.os.Process.myUid(),
            context.packageName
        )
        mode == AppOpsManager.MODE_ALLOWED
    }
}