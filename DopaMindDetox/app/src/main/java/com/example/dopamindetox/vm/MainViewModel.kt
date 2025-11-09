package com.example.dopamindetox.vm

import android.app.Application
import androidx.lifecycle.*
import com.example.dopamindetox.data.db.BlockedApp
import com.example.dopamindetox.data.repo.AppRepository
import com.example.dopamindetox.service.ForegroundMonitorService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repo: AppRepository) : ViewModel() {

    val blockedApps: StateFlow<List<BlockedApp>> = repo.blocked.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val goalMinutes: StateFlow<Int> = repo.goal().map { it?.minutes ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val recommendedGoal = MutableStateFlow(60)

    val weeklyUsage = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categoryUsage = MutableStateFlow<Map<String, Int>>(emptyMap())
    val screenOnCount = MutableStateFlow(0)
    val firstUnlockHour = MutableStateFlow(9)

    val todos = repo.todos().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val altActivities = repo.activities().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refreshAnalytics()
        viewModelScope.launch { recommendedGoal.value = repo.recommendedGoalMinutes() }
    }

    fun startMonitoringService() {
        // Service 시작
        ForegroundMonitorService.start(getApplication())
    }

    fun saveGoal(min:Int) = viewModelScope.launch { repo.setGoal(min) }
    fun addBlocked(pkg:String, label:String) = viewModelScope.launch { repo.addBlocked(pkg,label) }
    fun removeBlocked(pkg:String) = viewModelScope.launch { repo.removeBlocked(pkg) }

    fun addTodo(title:String) = viewModelScope.launch { repo.addTodo(title) }
    fun toggleTodo(id:Long, c:Boolean) = viewModelScope.launch { repo.toggleTodo(id, c) }

    fun addActivity(title:String) = viewModelScope.launch { repo.addActivity(title) }
    fun renameActivity(id:Long, t:String) = viewModelScope.launch { repo.renameActivity(id,t) }
    fun deleteActivity(id:Long) = viewModelScope.launch { repo.deleteActivity(id) }

    fun refreshAnalytics() = viewModelScope.launch {
        val weekly = repo.weeklyUsage()
        weeklyUsage.value = weekly
        // labelMap 은 실제 AppLabel 조회로 확장 가능. 여기선 패키지명 그대로.
        categoryUsage.value = repo.categoryUsage(weekly.associate { it.first to it.first })
        screenOnCount.value = repo.countScreenOnToday()
        firstUnlockHour.value = repo.firstUnlockHourToday()
    }

    private fun getApplication() = (getViewModelScopeApplication())

    // trick to get Application from ViewModel without AndroidViewModel
    private fun getViewModelScopeApplication(): Application {
        return checkNotNull(
            (this as ViewModelStoreOwner).javaClass
        ) as? Application ?: throw IllegalStateException("No application")
    }
}

class MainViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
