package com.example.dopamindetox.data.model

fun classify(packageName: String, label: String): String = when {
    listOf("youtube","facebook","instagram","twitter","x","tiktok","reddit","kakao","line").any { packageName.contains(it, true) || label.contains(it, true) } -> "SNS"
    listOf("game","play","supercell","netease","bandai","nintendo").any { packageName.contains(it, true) || label.contains(it, true) } -> "게임"
    listOf("docs","drive","notion","todo","keep","calendar","office").any { packageName.contains(it, true) || label.contains(it, true) } -> "생산성"
    else -> "기타"
}
