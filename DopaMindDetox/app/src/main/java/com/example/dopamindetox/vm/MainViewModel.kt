package com.example.dopamindetox.vm

import android.app.Application
import androidx.lifecycle.*
import com.example.dopamindetox.data.db.BlockedApp
import com.example.dopamindetox.data.repo.AppRepository
import com.example.dopamindetox.service.ForegroundMonitorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(app: Application, private val repo: AppRepository) : AndroidViewModel(app) {

    // (DB '읽기' 준비 - '백그라운드'에서)
    val blockedApps: StateFlow<List<BlockedApp>> = repo.blocked
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalMinutes: StateFlow<Int> = repo.goal().map { it?.minutes ?: 0 }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recommendedGoal = MutableStateFlow(60)
    val weeklyUsage = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categoryUsage = MutableStateFlow<Map<String, Int>>(emptyMap())
    val screenOnCount = MutableStateFlow(0)
    val firstUnlockHour = MutableStateFlow(9)

    val todos = repo.todos()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val altActivities = repo.activities()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun startMonitoringService() {
        ForegroundMonitorService.start(getApplication())
    }
    fun saveGoal(min:Int) = viewModelScope.launch(Dispatchers.IO) { repo.setGoal(min) }
    fun addBlocked(pkg:String, label:String) = viewModelScope.launch(Dispatchers.IO) { repo.addBlocked(pkg,label) }
    fun removeBlocked(pkg:String) = viewModelScope.launch(Dispatchers.IO) { repo.removeBlocked(pkg) }
    fun addTodo(title:String) = viewModelScope.launch(Dispatchers.IO) { repo.addTodo(title) }
    fun toggleTodo(id:Long, c:Boolean) = viewModelScope.launch(Dispatchers.IO) { repo.toggleTodo(id, c) }
    fun addActivity(title:String) = viewModelScope.launch(Dispatchers.IO) { repo.addActivity(title) }
    fun renameActivity(id:Long, t:String) = viewModelScope.launch(Dispatchers.IO) { repo.renameActivity(id,t) }
    fun deleteActivity(id:Long) = viewModelScope.launch(Dispatchers.IO) { repo.deleteActivity(id) }

    // (데이터 분석 - '나중에' 부를 때만 '백그라운드'에서)
    fun refreshAnalytics() = viewModelScope.launch(Dispatchers.IO) {
        val weekly = repo.weeklyUsage()
        weeklyUsage.value = weekly
        categoryUsage.value = repo.categoryUsage(weekly.associate { it.first to it.first })
        screenOnCount.value = repo.countScreenOnToday()
        firstUnlockHour.value = repo.firstUnlockHourToday()
    }
}

class MainViewModelFactory(
    private val app: Application,
    private val repository: AppRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(app, repository) as T
    }
}