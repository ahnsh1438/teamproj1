package com.example.dopamindetox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dopamindetox.nav.AppNavHost
import com.example.dopamindetox.ui.theme.AppTheme
import com.example.dopamindetox.vm.MainViewModel
import com.example.dopamindetox.vm.MainViewModelFactory
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.os.Handler
import android.os.Looper
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // (주석 처리)

        val app = application as DopaApp

        // 1. 로딩 중" 깃발을 '임시로' 세웁니다
        var keepSplash = true

        // 2. 스플래시를 '설치'하고 '깃발'을 봅니다
        installSplashScreen().setKeepOnScreenCondition {
            keepSplash // 👈 이 깃발이 'true'면 로고가 안 꺼져요
        }

        // 3. 1초(1000) 뒤에 깃발을 'false'로 내립니다. (시간 지연)
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
        }, 1000) //

        setContent {
            AppTheme {
                val vm: MainViewModel = viewModel(factory = MainViewModelFactory(app, app.repository))

                Surface {
                    AppNavHost(vm)
                }
            }
        }
    }
}