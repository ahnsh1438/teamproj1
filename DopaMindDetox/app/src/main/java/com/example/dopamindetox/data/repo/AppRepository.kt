package com.example.dopamindetox.data.repo

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.getSystemService
import com.example.dopamindetox.data.db.*
import com.example.dopamindetox.data.model.classify
import com.example.dopamindetox.data.structures.RedBlackTree
import com.example.dopamindetox.data.structures.StackList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(private val ctx: Context, private val db: AppDatabase) {
    private val usage: UsageStatsManager = ctx.getSystemService()!!
    private val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    val blocked: Flow<List<BlockedApp>> = db.blockedApp().getAll()
    fun goal(): Flow<Goal?> = db.goal().observe()
    fun todos(): Flow<List<Todo>> = db.todo().all()
    fun activities(): Flow<List<AltActivity>> = db.activity().all()

    suspend fun setGoal(m:Int) = db.goal().set(Goal(minutes = m))
    suspend fun addBlocked(pkg:String,label:String) = db.blockedApp().upsert(BlockedApp(pkg,label))
    suspend fun removeBlocked(pkg:String) = db.blockedApp().delete(pkg)

    suspend fun addTodo(title:String) = db.todo().add(Todo(title=title))
    suspend fun toggleTodo(id:Long, completed:Boolean) =
        db.todo().toggle(id, completed, if (completed) nowTime() else null)

    suspend fun addActivity(title:String) = db.activity().add(AltActivity(title=title))
    suspend fun renameActivity(id:Long, t:String) = db.activity().rename(id, t)
    suspend fun deleteActivity(id:Long) = db.activity().delete(id)

    suspend fun saveScreenOn() = db.screenEvent().add(ScreenEvent(type="SCREEN_ON", ts=System.currentTimeMillis()))
    suspend fun countScreenOnToday(): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
        }
        val start = cal.timeInMillis
        val end = start + 24*60*60*1000
        return db.screenEvent().countScreenOn(start,end)
    }

    suspend fun firstUnlockHourToday(): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
        }
        val start = cal.timeInMillis
        val events = db.screenEvent().countScreenOn(start, start+6*60*60*1000) // heuristic
        return if (events>0) Calendar.getInstance().get(Calendar.HOUR_OF_DAY) else 9
    }

    suspend fun weeklyUsage(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val end = System.currentTimeMillis()
        val start = end - 7L*24*60*60*1000
        val stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        // RBTree로 상위 앱 정렬/집계
        val tree = RedBlackTree<String, Int>()
        stats.forEach {
            val minutes = (it.totalTimeInForeground / 60000L).toInt()
            if (minutes>0) {
                val key = it.packageName
                val prev = tree.get(key) ?: 0
                tree.put(key, prev + minutes)
            }
        }
        tree.inorder().sortedByDescending { it.second }
    }

    suspend fun categoryUsage(labelMap: Map<String,String>): Map<String, Int> {
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
        // 지난주 평균 대비 20% 절감 권장
        return (avg * 0.8).toInt().coerceAtLeast(30)
    }

    fun openUsageAccessSettings() {
        ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun nowTime(): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

    fun startMonitoringService() {
        com.example.dopamindetox.service.ForegroundMonitorService.start(ctx)
    }
}
