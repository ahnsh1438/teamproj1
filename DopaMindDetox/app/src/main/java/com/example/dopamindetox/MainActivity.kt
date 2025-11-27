package com.example.dopamindetox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dopamindetox.nav.AppNavHost
import com.example.dopamindetox.ui.theme.AppTheme
import com.example.dopamindetox.vm.MainViewModel
import com.example.dopamindetox.vm.MainViewModelFactory
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as DopaApp

        var keepSplash = true
        installSplashScreen().setKeepOnScreenCondition { keepSplash }
        Handler(Looper.getMainLooper()).postDelayed({ keepSplash = false }, 1000)

        setContent {
            AppTheme {
                val vm: MainViewModel = viewModel(factory = MainViewModelFactory(app, app.repository))

                // 📌 1. Scaffold와 SnackbarHostState를 MainActivity에 생성
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // 📌 2. ViewModel의 snackbarMessage Flow를 구독하고 메시지 표시
                LaunchedEffect(Unit) {
                    vm.snackbarMessage.collectLatest {
                        scope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }

                // 📌 3. Scaffold를 최상위 레이아웃으로 설정
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) {
                    // AppNavHost에 Scaffold가 제공하는 패딩(it)을 전달
                    AppNavHost(vm, it)
                }
            }
        }
    }
}
