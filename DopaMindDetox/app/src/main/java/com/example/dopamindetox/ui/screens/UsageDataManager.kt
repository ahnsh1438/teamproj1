package com.example.dopamindetox

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import java.util.Calendar

// 앱 정보를 담을 데이터 클래스
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTime: Long,
    val icon: android.graphics.drawable.Drawable?
)

class UsageDataManager(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    // 권한 확인
    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    // 권한 설정창 열기
    fun requestPermission() {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    // 주간 데이터 (그래프용)
    fun getWeeklyUsage(): List<Pair<String, Long>> {
        val calendar = Calendar.getInstance()
        val result = mutableListOf<Pair<String, Long>>()

        for (i in 6 downTo 0) {
            calendar.time = java.util.Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            val start = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            val end = calendar.timeInMillis

            val dayLabel = if (i == 0) "오늘" else "${calendar.get(Calendar.DAY_OF_MONTH)}일"
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            val totalTime = stats.sumOf { it.totalTimeInForeground }
            result.add(Pair(dayLabel, totalTime))
        }
        return result
    }

    // 오늘 Top 5 앱 & 화면 켜짐 횟수
    fun getTodayStats(): Pair<List<AppUsageInfo>, Int> {
        val calendar = Calendar.getInstance()
        val start = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val end = System.currentTimeMillis()

        // 1. 앱 사용량
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val list = mutableListOf<AppUsageInfo>()
        for (usage in stats) {
            if (usage.totalTimeInForeground > 0) {
                try {
                    val appName = packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(usage.packageName, 0)
                    ).toString()
                    val icon = packageManager.getApplicationIcon(usage.packageName)
                    list.add(AppUsageInfo(usage.packageName, appName, usage.totalTimeInForeground, icon))
                } catch (e: Exception) {}
            }
        }
        val top5 = list.sortedByDescending { it.usageTime }.take(5)

        // 2. 화면 켜짐 횟수
        val events = usageStatsManager.queryEvents(start, end)
        val eventObj = UsageEvents.Event()
        var count = 0
        while (events.hasNextEvent()) {
            events.getNextEvent(eventObj)
            if (eventObj.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) count++
        }
        return Pair(top5, count)
    }
}