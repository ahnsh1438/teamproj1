package com.example.dopamindetox.service

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.dopamindetox.R
import com.example.dopamindetox.overlay.BlockOverlayActivity
import com.example.dopamindetox.data.db.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ForegroundMonitorService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var usage: UsageStatsManager
    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        usage = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        db = AppDatabase.get(this)
        startForegroundWithChannel()
        startLoop()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithChannel() {
        val chId = "dopa_monitor"
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            mgr.createNotificationChannel(NotificationChannel(chId, getString(R.string.notify_channel_name), NotificationManager.IMPORTANCE_LOW).apply {
                description = getString(R.string.notify_channel_desc)
            })
        }
        val noti = NotificationCompat.Builder(this, chId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("차단 모니터링 동작 중")
            .setOngoing(true)
            .build()
        startForeground(1, noti)
    }

    private fun startLoop() {
        scope.launch {
            while (isActive) {
                try {
                    checkTopAppAndBlockIfNeeded()
                } catch (_: Throwable) {}
                delay(500) // 0.5초 주기: 2초 이내 전환 보장
            }
        }
    }

    private suspend fun checkTopAppAndBlockIfNeeded() {
        val end = System.currentTimeMillis()
        val start = end - 3000
        val ev = usage.queryEvents(start, end)
        var top: String? = null
        val event = UsageEvents.Event()
        while (ev.hasNextEvent()) {
            ev.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                top = event.packageName
            }
        }
        val pkg = top ?: return
        val blocked = db.blockedApp().getAll().first()
        if (blocked.any { it.packageName == pkg }) {
            // 목표 시간 vs 현재 사용 시간은 심화 구현 가능
            val intent = Intent(this, BlockOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("targetPkg", pkg)
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun start(ctx: Context) {
            val i = Intent(ctx, ForegroundMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
        }
    }
}
