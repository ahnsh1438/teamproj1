package com.example.dopamindetox.data.repo

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.getSystemService
import com.example.dopamindetox.data.db.*
import com.example.dopamindetox.data.model.classify
import com.example.dopamindetox.data.structures.RedBlackTree
import com.example.dopamindetox.data.structures.StackList
import com.example.dopamindetox.service.ForegroundMonitorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(private val ctx: Context, private val db: AppDatabase) {

    private val usage: UsageStatsManager = ctx.getSystemService()!!
    private val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    /* ------------------------------ 기본 DB Flow ------------------------------ */

    val blocked: Flow<List<BlockedApp>> = db.blockedApp().getAll()
    fun goal(): Flow<Goal?> = db.goal().observe()

    // ⭐ 기존 전체 조회는 유지
    fun todos(): Flow<List<Todo>> = db.todo().all()

    // ⭐ 특정 날짜만 조회하는 기능 추가됨
    fun todosByDate(date: String): Flow<List<Todo>> = db.todo().todosByDate(date)

    fun activities(): Flow<List<AltActivity>> = db.activity().all()

    /* ------------------------------ Goal / Blocked App ------------------------------ */

    suspend fun setGoal(m: Int) = db.goal().set(Goal(minutes = m))
    suspend fun addBlocked(pkg: String, label: String) = db.blockedApp().upsert(BlockedApp(pkg, label))
    suspend fun removeBlocked(pkg: String) = db.blockedApp().delete(pkg)

    /* ------------------------------ Todo 기능 ------------------------------ */

    // ⭐ 날짜 기반 Todo 저장
    suspend fun addTodo(title: String, date: String) {
        db.todo().add(
            Todo(
                title = title,
                date = date,   // 새로 추가된 필드
                completed = false
            )
        )
    }

    suspend fun toggleTodo(id: Long, completed: Boolean) =
        db.todo().toggle(id, completed, if (completed) nowTime() else null)

    suspend fun renameTodo(id: Long, newTitle: String) =
        db.todo().rename(id, newTitle)

    suspend fun deleteTodo(id: Long) =
        db.todo().delete(id)

    /* ------------------------------ Activity 기능 ------------------------------ */

    suspend fun addActivity(title: String) = db.activity().add(AltActivity(title = title))
    suspend fun renameActivity(id: Long, t: String) = db.activity().rename(id, t)
    suspend fun deleteActivity(id: Long) = db.activity().delete(id)

    /* ------------------------------ Screen / Usage 분석 기능 ------------------------------ */

    suspend fun saveScreenOn() =
        db.screenEvent().add(ScreenEvent(type = "SCREEN_ON", ts = System.currentTimeMillis()))

    suspend fun countScreenOnToday(): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 24 * 60 * 60 * 1000
        return db.screenEvent().countScreenOn(start, end)
    }

    suspend fun firstUnlockHourToday(): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val events = db.screenEvent().countScreenOn(start, start + 6 * 60 * 60 * 1000)
        return if (events > 0) Calendar.getInstance().get(Calendar.HOUR_OF_DAY) else 9
    }

    suspend fun weeklyUsage(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val end = System.currentTimeMillis()
        val start = end - 7L * 24 * 60 * 60 * 1000
        val stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)

        val map = mutableMapOf<String, Int>()
        stats.forEach {
            val minutes = (it.totalTimeInForeground / 60000L).toInt()
            if (minutes > 0) {
                map[it.packageName] = (map[it.packageName] ?: 0) + minutes
            }
        }

        map.toList().sortedByDescending { it.second }
    }

    suspend fun categoryUsage(labelMap: Map<String, String>): Map<String, Int> {
        val list = weeklyUsage()
        val agg = mutableMapOf<String, Int>()
        list.forEach { (pkg, minutes) ->
            val cat = classify(pkg, labelMap[pkg] ?: "")
            agg[cat] = (agg[cat] ?: 0) + minutes
        }
        return agg
    }

    suspend fun recommendedGoalMinutes(): Int {
        val list = weeklyUsage()
        val avg = list.sumOf { it.second } / 7
        return (avg * 0.8).toInt().coerceAtLeast(30)
    }

    /* ------------------------------ 기타 ------------------------------ */

    fun openUsageAccessSettings() {
        ctx.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun nowTime(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

    fun startMonitoringService() {
        ForegroundMonitorService.start(ctx)
    }

    fun stopMonitoringService() {
        ForegroundMonitorService.stop(ctx)
    }
}
