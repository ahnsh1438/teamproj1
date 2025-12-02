package com.example.dopamindetox.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.dopamindetox.R
import com.example.dopamindetox.data.db.AppDatabase
import com.example.dopamindetox.overlay.BlockOverlayActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.math.ceil

class ForegroundMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var usage: UsageStatsManager
    private lateinit var db: AppDatabase
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    private var currentPackage: String? = null
    private var currentStartTime: Long = 0L
    private var baseUsedMillis: Long = 0L      // 오늘 하루, 이 세션 시작 전까지의 누적 사용 시간
    private var blockTriggered: Boolean = false

    private val LOOP_DELAY_MS = 1_000L
    private val NOTI_ID = 1001
    private val CHANNEL_ID = "dopa_monitor"

    override fun onCreate() {
        super.onCreate()
        _isRunning.value = true

        usage = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        db = AppDatabase.get(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel()
        val noti = buildNotification("모니터링 준비 중", "목표 시간과 차단 앱을 설정하세요.")
        startForeground(NOTI_ID, noti)

        startMonitorLoop()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        _isRunning.value = false
        serviceScope.cancel()
        super.onDestroy()
    }

    // ---------------------- Notification ----------------------

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DopaMindDetox 차단 모니터링",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        return builder.build()
    }

    private fun updateNotification(title: String, text: String) {
        builder.setContentTitle(title)
        builder.setContentText(text)
        notificationManager.notify(NOTI_ID, builder.build())
    }

    // ---------------------- Main loop ----------------------

    private fun startMonitorLoop() {
        serviceScope.launch {
            while (isActive) {
                try {
                    monitorOnce()
                } catch (e: Exception) {
                    updateNotification("모니터링 오류", e.message ?: "알 수 없는 오류")
                }
                delay(LOOP_DELAY_MS)
            }
        }
    }

    /**
     * 1회 모니터링:
     * - 현재 포그라운드 앱 패키지 구하기
     * - DB에서 차단 앱 및 목표 시간 가져오기
     * - 오늘 누적 사용 시간 + 현재 세션 시간으로 남은 시간 계산
     * - 0 이하면 오버레이 한 번만 실행
     */
    private suspend fun monitorOnce() {
        val topPackage = getTopPackageName()
        if (topPackage == null) {
            updateNotification("모니터링 중", "실행 중인 앱을 찾을 수 없습니다.")
            return
        }

        val blockedList = db.blockedApp().getAll().first()
        val goalMinutes = db.goal().observe().first()?.minutes ?: 0

        if (goalMinutes <= 0 || blockedList.isEmpty()) {
            // 설정 안 된 상태 → 상태 리셋
            currentPackage = null
            currentStartTime = 0L
            baseUsedMillis = 0L
            blockTriggered = false
            updateNotification("모니터링 중", "목표 시간 또는 차단 앱을 설정하세요.")
            return
        }

        val isBlocked = blockedList.any { it.packageName == topPackage }

        // 차단 대상이 아니면 상태 리셋
        if (!isBlocked) {
            currentPackage = null
            currentStartTime = 0L
            baseUsedMillis = 0L
            blockTriggered = false
            updateNotification("모니터링 중", "현재 앱: $topPackage (차단 대상 아님)")
            return
        }

        val goalMillis = goalMinutes * 60_000L

        // 새로 차단 대상 앱이 포착되었을 때
        if (currentPackage != topPackage || currentStartTime == 0L) {
            currentPackage = topPackage
            currentStartTime = System.currentTimeMillis()
            blockTriggered = false

            // 오늘 이 앱을 이미 얼마나 썼는지(세션 시작 전까지)를 계산
            baseUsedMillis = queryTodayUsageMillisForPackage(topPackage)
        }

        val now = System.currentTimeMillis()
        val elapsedThisSession = now - currentStartTime
        val totalUsed = baseUsedMillis + elapsedThisSession
        val remain = goalMillis - totalUsed

        if (remain > 0) {
            val remainMin = ceil(remain / 60_000.0).toInt()
            val label = blockedList.find { it.packageName == topPackage }?.label ?: topPackage
            updateNotification("$label", "차단까지 남은 시간: ${remainMin}분")
        } else {
            // ⚠ 여기서 오버레이는 '한 번만' 실행되도록 가드
            if (!blockTriggered) {
                blockTriggered = true
                val label = blockedList.find { it.packageName == topPackage }?.label ?: topPackage
                updateNotification("$label 차단됨", "설정한 목표 시간을 초과했습니다.")
                showOverlay(topPackage)
            }
        }
    }

    /**
     * 오늘 자정 ~ 지금까지의 totalTimeInForeground 를 합산
     * (하루 누적 사용 시간을 구하기 위함)
     */
    private fun queryTodayUsageMillisForPackage(pkg: String): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()

        val stats = usage.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end
        ) ?: return 0L

        var total = 0L
        for (s in stats) {
            if (s.packageName == pkg) {
                total += s.totalTimeInForeground
            }
        }
        return total
    }

    // ---------------------- Foreground App Detection ----------------------

    private var lastKnownApp: String? = null

    /**
     * 삼성 / 안드로이드 13+ 대응:
     * 1) UsageEvents 의 ACTIVITY_RESUMED 우선
     * 2) 없으면 가장 최근 이벤트의 패키지
     * 3) 그래도 없으면 UsageStats 로 fallback
     */
    private fun getTopPackageName(): String? {
        val now = System.currentTimeMillis()
        val begin = now - 30_000L
        val events = usage.queryEvents(begin, now)
        val e = UsageEvents.Event()

        var lastResumedPkg: String? = null
        var lastResumedTs = -1L

        var lastEventPkg: String? = null
        var lastEventTs = -1L

        while (events.hasNextEvent()) {
            events.getNextEvent(e)

            if (e.timeStamp > lastEventTs) {
                lastEventTs = e.timeStamp
                lastEventPkg = e.packageName
            }

            if (e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (e.timeStamp > lastResumedTs) {
                    lastResumedTs = e.timeStamp
                    lastResumedPkg = e.packageName
                }
            }
        }

        val self = packageName

        if (lastResumedPkg != null && lastResumedPkg != self) {
            lastKnownApp = lastResumedPkg
            return lastResumedPkg
        }

        // fallback: UsageStats에서 가장 최근에 사용된 앱
        val stats = usage.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 60 * 60 * 1000L,
            now
        )

        var candidate: String? = null
        var lastUsed = 0L
        if (stats != null) {
            for (s in stats) {
                val pkg = s.packageName
                if (pkg == self || pkg == "android" || pkg == "com.android.systemui") continue
                if (s.lastTimeUsed > lastUsed) {
                    lastUsed = s.lastTimeUsed
                    candidate = pkg
                }
            }
        }

        if (candidate != null) {
            lastKnownApp = candidate
            return candidate
        }

        return lastKnownApp
    }

    // ---------------------- Overlay ----------------------

    private fun showOverlay(pkg: String) {
        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )
            putExtra("targetPkg", pkg)
        }
        startActivity(intent)
    }

    // ---------------------- Companion ----------------------

    companion object {
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(ctx: Context) {
            val intent = Intent(ctx, ForegroundMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ForegroundMonitorService::class.java))
        }
    }
}
