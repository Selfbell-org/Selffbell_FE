package com.example.auth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auth.R // Assuming this is correct for your project
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.AddressModel

/**
 * 주소 검색 결과 아이템을 표시하는 재사용 가능한 Composable입니다.
 *
 * @param address 표시할 주소 데이터 모델.
 * @param onClick 아이템 클릭 시 실행될 람다 함수.
 */
@Composable//
fun AddressResultItem(address: AddressModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = address.roadAddress,
            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = address.jibunAddress,
            style = Typography.bodySmall.copy(color = Color.Gray)
        )
    }
}

/**
 * AddressResultItem 컴포넌트의 프리뷰입니다.
 */
@Preview(showBackground = true)
@Composable
fun AddressResultItemPreview() {
    SelfBellTheme {
        val sampleAddress = AddressModel(
            roadAddress = "서울시 강남구 테헤란로 123",
            jibunAddress = "역삼동 123-45",
            x = "127.0",
            y = "37.5"
        )
        AddressResultItem(address = sampleAddress) {
            // 프리뷰용 클릭 액션
        }
    }
}