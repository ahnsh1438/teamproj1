package com.example.dopamindetox.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.dopamindetox.data.db.AppDatabase

class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_SCREEN_ON == intent.action) {
            val db = AppDatabase.get(context)
            CoroutineScope(Dispatchers.IO).launch { db.screenEvent().add(
                com.example.dopamindetox.data.db.ScreenEvent(type="SCREEN_ON", ts=System.currentTimeMillis())
            ) }
        }
    }

    companion object {
        fun register(context: Context): ScreenOnReceiver {
            val r = ScreenOnReceiver()
            context.registerReceiver(r, IntentFilter(Intent.ACTION_SCREEN_ON))
            return r
        }
    }
}
