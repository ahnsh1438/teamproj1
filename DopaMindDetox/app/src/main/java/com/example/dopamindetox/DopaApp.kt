package com.example.dopamindetox

import android.app.Application
import com.example.dopamindetox.data.db.AppDatabase
import com.example.dopamindetox.data.repo.AppRepository

class DopaApp : Application() {
    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = AppRepository(this, db)
    }
}
