package com.example.dopamindetox

import android.app.Application
import com.example.dopamindetox.data.db.AppDatabase
import com.example.dopamindetox.data.repo.AppRepository

class DopaApp : Application() {
    // 'lateinit var' (즉시 폭발) 대신 'val ... by lazy' (안전하게 나중에) 사용
    val repository: AppRepository by lazy {
        // 이 코드는 'repository' 변수가 '처음' 호출될 때 딱 한 번 실행됩니다.
        val db = AppDatabase.get(this) // 학생이 준 AppDatabase.get() 함수 사용
        AppRepository(this, db)
    }

    override fun onCreate() {
        super.onCreate()
        // onCreate() 에서는 아무것도 부르지 않습니다.
    }
}