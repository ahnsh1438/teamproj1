package com.example.dopamindetox.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface BlockedAppDao {
    @Query("SELECT * FROM BlockedApp") fun getAll(): Flow<List<BlockedApp>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(b: BlockedApp)
    @Query("DELETE FROM BlockedApp WHERE packageName = :pkg") suspend fun delete(pkg:String)
}

@Dao interface GoalDao {
    @Query("SELECT * FROM Goal WHERE id=0") fun observe(): Flow<Goal?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun set(g: Goal)
}

@Dao interface TodoDao {
    @Query("SELECT * FROM Todo ORDER BY id DESC") fun all(): Flow<List<Todo>>
    @Insert suspend fun add(t: Todo)
    @Query("UPDATE Todo SET completed=:completed, completedAt=:completedAt WHERE id=:id")
    suspend fun toggle(id:Long, completed:Boolean, completedAt:String?)
}

@Dao interface ActivityDao {
    @Query("SELECT * FROM AltActivity ORDER BY id DESC") fun all(): Flow<List<AltActivity>>
    @Insert suspend fun add(a: AltActivity)
    @Query("UPDATE AltActivity SET title=:title WHERE id=:id") suspend fun rename(id:Long, title:String)
    @Query("DELETE FROM AltActivity WHERE id=:id") suspend fun delete(id:Long)
}

@Dao interface UsageDao {
    @Query("SELECT * FROM UsageSample WHERE dayKey=:dayKey")
    suspend fun byDay(dayKey:String): List<UsageSample>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<UsageSample>)
}

@Dao interface ScreenEventDao {
    @Insert suspend fun add(e: ScreenEvent)
    @Query("SELECT COUNT(*) FROM ScreenEvent WHERE type='SCREEN_ON' AND ts BETWEEN :start AND :end")
    suspend fun countScreenOn(start:Long, end:Long): Int
}

@Dao interface RewardDao {
    @Query("SELECT * FROM Reward WHERE dayKey=:dayKey") suspend fun get(dayKey:String): Reward?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun set(r: Reward)
}
