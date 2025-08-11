package com.selfbell.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.naver.maps.geometry.LatLng
import com.selfbell.core.ui.theme.Typography

@Composable
fun MapInfoBalloon(
    modifier: Modifier = Modifier,
    address: String,
    latLng: LatLng, // 마커 위치 정보 (필요시 사용)
    onDismissRequest: () -> Unit
) {
    // Popup을 사용하여 지도 위에 오버레이 형태로 표시
    // Popup의 위치는 직접 계산하거나, Alignment를 사용할 수 있습니다.
    // 여기서는 간단하게 화면 중앙 근처에 나타나도록 합니다. (실제로는 마커 위치 기반으로 조정 필요)
    Popup(
        alignment = Alignment.Center, // 또는 다른 정렬, offset 사용 가능
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = modifier
                .wrapContentSize()
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable(onClick = onDismissRequest), // 말풍선 클릭 시 닫기
            color = Color.White // Surface 자체 색상
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "주소", style = Typography.labelSmall)
                Text(text = address, style = Typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                // 필요시 추가 정보 (예: "상세보기 버튼 등")
            }
        }
    }
}