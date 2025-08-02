package com.selfbell.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.composables.SelfBellButton // SelfBellButton 임포트
import com.selfbell.core.ui.composables.SelfBellButtonType // SelfBellButtonType 임포트
import com.selfbell.core.ui.theme.SelfBellTheme // 테마 임포트


@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "여기는 홈 화면입니다!")

        // 테스트용 SelfBellButton 배치
        SelfBellButton(
            text = "메인 버튼 테스트",
            onClick = { /* TODO: 클릭 시 동작 정의 */ println("메인 버튼 클릭!") },
            buttonType = SelfBellButtonType.PRIMARY_FILLED,
            modifier = Modifier.padding(top = 16.dp)
        )
        SelfBellButton(
            text = "외곽선 버튼 테스트",
            onClick = { /* TODO: 클릭 시 동작 정의 */ println("외곽선 버튼 클릭!") },
            buttonType = SelfBellButtonType.OUTLINED,
            modifier = Modifier.padding(top = 8.dp)
        )
        SelfBellButton(
            text = "비활성화 버튼",
            onClick = { /* TODO */ },
            enabled = false,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SelfBellTheme {
        HomeScreen()
    }
}