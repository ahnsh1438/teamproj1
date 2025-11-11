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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun FirstEntryScreen(onContinue: () -> Unit, vm: MainViewModel) {
    val ctx = LocalContext.current

    // 🛑 중요: 앱이 켜질 때 권한 체크를 하지 않도록, 무조건 false로 시작합니다.
    var hasOverlay by remember { mutableStateOf(false) }
    var hasUsage by remember { mutableStateOf(false) }

    val allPermissionsGranted = hasOverlay && hasUsage
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        // 'ON_RESUME' (설정에서 돌아올 때) 감시자 정의
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 🛑 설정에서 돌아올 때만 권한을 체크합니다.
                hasOverlay = Settings.canDrawOverlays(ctx)
                hasUsage = hasUsageAccess(ctx)
            }
        }

        // 감시자 등록
        lifecycleOwner.lifecycle.addObserver(observer)

        // 화면 나갈 때 감시자 제거
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
        Text("• 다른 앱 위에 표시\n• 사용량 접근(Usage Access)\n이 필요합니다.")
        Spacer(Modifier.height(24.dp))

        // 오버레이 버튼
        Button(onClick = {
            ctx.startActivity(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${ctx.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }) { Text(if (hasOverlay) "오버레이 권한 완료" else "오버레이 권한 열기") }

        Spacer(Modifier.height(12.dp))

        // 사용량 접근 버튼
        Button(onClick = {
            ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }) { Text(if (hasUsage) "사용량 접근 완료" else "사용량 접근 열기") }

        Spacer(Modifier.height(24.dp))

        // '시작하기' 버튼: (무조건 비활성화 상태로 시작)
        Button(
            enabled = allPermissionsGranted,
            onClick = {
                onContinue() // 메인 화면으로 이동
            }
        ) { Text("시작하기") }
    }
}

// (ON_RESUME 시에만 호출되므로 안전합니다)
private fun hasUsageAccess(context: Context): Boolean {
    return try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        if (appOps == null) return false

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