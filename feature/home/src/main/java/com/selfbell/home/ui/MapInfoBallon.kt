package com.selfbell.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup // Popup import
import androidx.compose.ui.window.PopupProperties // PopupProperties import
import com.naver.maps.geometry.LatLng

@Composable
fun MapInfoBalloon(
    address: String,
    latLng: LatLng, // latLng은 Popup 위치 지정에 직접 사용되진 않지만, 정보 전달용으로 유지
    onDismissRequest: () -> Unit, // Popup의 onDismissRequest와 이름 맞춤
    modifier: Modifier = Modifier, // 외부에서 Popup의 Modifier를 커스터마이징할 수 있도록 추가
    alignment: Alignment = Alignment.Center, // Popup의 정렬 위치
) {
    // Popup을 사용하여 화면 중앙 또는 지정된 위치에 풍선을 띄웁니다.
    Popup(
        alignment = alignment, // 화면 내에서의 Popup 정렬 (기본은 TopStart, 여기서는 Center로)
        onDismissRequest = onDismissRequest, // 바깥 영역 클릭 또는 백 버튼 클릭 시 호출
        properties = PopupProperties(
            focusable = true, // 팝업이 포커스를 받을 수 있도록 설정 (백 버튼으로 닫기 등)
            dismissOnClickOutside = true // 바깥 영역 클릭 시 닫힘
        )
    ) {
        // Surface가 Popup의 내용물이 됩니다.
        Surface(
            shadowElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = modifier // 외부에서 전달된 modifier 적용 (예: padding, size 등)
                .defaultMinSize(minWidth = 120.dp)
                .padding(16.dp) // 풍선 내부 여백 (기존 Row의 padding을 Surface로 이동)
        ) {
            // 이제 Row는 IconButton을 포함하지 않아도 됩니다.
            // 필요하다면 Row로 내용을 더 구성할 수 있습니다.
            Text(
                text = address, // address 표시
                style = MaterialTheme.typography.bodyMedium
            )
            // 만약 주소 외에 다른 정보나 버튼이 필요하다면 여기에 추가
            // 예:
            // Column {
            // Text(text = address, style = MaterialTheme.typography.bodyMedium)
            // Text(text = "위도: ${latLng.latitude}", style = MaterialTheme.typography.caption)
            // Text(text = "경도: ${latLng.longitude}", style = MaterialTheme.typography.caption)
            // }
        }
    }
}

