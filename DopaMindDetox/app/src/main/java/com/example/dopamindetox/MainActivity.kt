package com.example.dopamindetox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dopamindetox.nav.AppNavHost
import com.example.dopamindetox.ui.theme.AppTheme
import com.example.dopamindetox.vm.MainViewModel
import com.example.dopamindetox.vm.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as DopaApp
        setContent {
            AppTheme {
                val vm: MainViewModel = viewModel(factory = MainViewModelFactory(app.repository))
                Surface { AppNavHost(vm) }
            }
        }
    }
}
