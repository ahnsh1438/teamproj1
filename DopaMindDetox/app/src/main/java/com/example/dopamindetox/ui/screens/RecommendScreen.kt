package com.example.dopamindetox.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.dopamindetox.vm.MainViewModel

// 📌 1. padding 파라미터 추가
@Composable
fun RecommendScreen(
    vm: MainViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    // 📌 2. 내부 Scaffold 제거하고 Box로 변경
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding), // 전달받은 패딩 적용
        contentAlignment = Alignment.Center
    ) {
        // TODO: 여기에 '추천 목표' UI를 만듭니다
        Text("추천 목표 화면")
    }
}
