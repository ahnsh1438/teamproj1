package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.dopamindetox.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    vm: MainViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("추천 목표 보기") },
                // '뒤로가기' 버튼
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // 👈 누르면 뒤로 감
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // TODO: 여기에 '추천 목표' UI를 만듭니다
            Text("추천 목표 화면")
        }
    }
}