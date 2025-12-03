package com.example.dopamindetox.vm

import android.app.Application
import androidx.lifecycle.*
import com.example.dopamindetox.data.db.BlockedApp
import com.example.dopamindetox.data.db.Todo
import com.example.dopamindetox.data.repo.AppRepository
import com.example.dopamindetox.service.ForegroundMonitorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(app: Application, private val repo: AppRepository) : AndroidViewModel(app) {

    /* ---------------------------- Snackbar 메시지 ---------------------------- */

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    /* ---------------------------- 기존 Flow ---------------------------- */

    val blockedApps: StateFlow<List<BlockedApp>> = repo.blocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalMinutes: StateFlow<Int> = repo.goal().map { it?.minutes ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isServiceRunning: StateFlow<Boolean> = ForegroundMonitorService.isRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val altActivities = repo.activities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /* ---------------------------- 날짜 기반 Todo Flow ---------------------------- */

    // ⭐ 현재 선택된 날짜
    private val _selectedDate = MutableStateFlow(getTodayKey())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // ⭐ 특정 날짜 Todo 자동 조회
    val todosByDate: StateFlow<List<Todo>> =
        _selectedDate
            .flatMapLatest { dateKey ->
                repo.todosByDate(dateKey) // ← ★ 날짜 기반으로 DB 조회
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ⭐ 전체 Todo Flow (필요할 때 사용)
    val todosAll = repo.todos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /* ---------------------------- 날짜 변경 함수 ---------------------------- */

    fun updateSelectedDate(newDateKey: String) {
        _selectedDate.value = newDateKey
    }

    /* ---------------------------- Todo 기능 ---------------------------- */

    fun addTodo(title: String, dateKey: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.addTodo(title, dateKey)
        _snackbarMessage.emit("목표가 추가되었습니다.")
    }

    fun toggleTodo(id: Long, completed: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repo.toggleTodo(id, completed)
    }

    fun renameTodo(id: Long, newTitle: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.renameTodo(id, newTitle)
    }

    fun deleteTodo(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteTodo(id)
    }

    /* ---------------------------- Activity 기능 ---------------------------- */

    fun addActivity(title: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.addActivity(title)
    }

    fun renameActivity(id: Long, t: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.renameActivity(id, t)
    }

    fun deleteActivity(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteActivity(id)
    }

    /* ---------------------------- Goal / Service ---------------------------- */

    fun saveGoal(min: Int) = viewModelScope.launch {
        repo.setGoal(min)
        _snackbarMessage.emit("목표 시간이 저장되었습니다.")
    }

    fun startMonitoringService() = viewModelScope.launch {
        repo.startMonitoringService()
        _snackbarMessage.emit("차단 서비스가 시작되었습니다.")
    }

    fun stopMonitoringService() = viewModelScope.launch {
        repo.stopMonitoringService()
        _snackbarMessage.emit("차단 서비스가 종료되었습니다.")
    }

    fun addBlocked(pkg: String, label: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.addBlocked(pkg, label)
    }

    fun removeBlocked(pkg: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.removeBlocked(pkg)
    }

    fun getTodosForToday(): StateFlow<List<Todo>> {
        val todayKey = getTodayKey()
        return repo.todosByDate(todayKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /* ---------------------------- 분석 관련 ---------------------------- */

    val recommendedGoal = MutableStateFlow(60)
    val weeklyUsage = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categoryUsage = MutableStateFlow<Map<String, Int>>(emptyMap())
    val screenOnCount = MutableStateFlow(0)
    val firstUnlockHour = MutableStateFlow(9)

    fun refreshAnalytics() = viewModelScope.launch(Dispatchers.IO) {
        val weekly = repo.weeklyUsage()
        weeklyUsage.value = weekly
        categoryUsage.value = repo.categoryUsage(weekly.associate { it.first to it.first })
        screenOnCount.value = repo.countScreenOnToday()
        firstUnlockHour.value = repo.firstUnlockHourToday()
    }

    /* ---------------------------- 날짜 포맷 헬퍼 ---------------------------- */

    private fun getTodayKey(): String {
        val now = java.time.LocalDate.now()
        return "%04d%02d%02d".format(now.year, now.monthValue, now.dayOfMonth)
    }
}

class MainViewModelFactory(
    private val app: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(app, repository) as T
    }
}
