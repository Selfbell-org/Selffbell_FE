import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.home.ui.AddressSearchModal
import com.selfbell.home.model.MapMarkerData
import com.selfbell.home.model.MarkerType
import com.selfbell.home.ui.MapInfoBalloon

@Composable
fun HomeScreen(
    userLatLng: LatLng,
    userAddress: String,
    criminalMarkers: List<MapMarkerData>,   // type=CRIMINAL
    safetyBellMarkers: List<MapMarkerData>, // type=SAFETY_BELL
    onAddressSearch: (String) -> Unit,
    searchedLatLng: LatLng?
) {
    var cameraPosition by remember { mutableStateOf(searchedLatLng ?: userLatLng) }
    var infoWindowData by remember { mutableStateOf<Pair<LatLng, String>?>(null) }

    Box(Modifier.fillMaxSize()) {
        // 지도
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPosition = cameraPosition,
            // 마커, 마커 클릭 콜백, 말풍선 상태 전달 ↓↓
            onMapReady = { naverMap ->
                // 내 위치 마커
                addOrUpdateMarker(naverMap, userLatLng, MapMarkerData(userLatLng, userAddress, MarkerType.USER)) { pairData -> // 람다 파라미터를 하나(Pair)로 받음
                    infoWindowData = pairData
                }
                // 범죄자 마커들
                criminalMarkers.forEach { data ->
                    addOrUpdateMarker(naverMap, data.latLng, data) { pairData -> // 람다 파라미터를 하나(Pair)로 받음
                        infoWindowData = pairData}
                }
                // 안심벨 마커들
                safetyBellMarkers.forEach { data ->
                    addOrUpdateMarker(naverMap, data.latLng, data) { pairData -> // 람다 파라미터를 하나(Pair)로 받음
                        infoWindowData = pairData}
                }
            }
        )

        // 마커 말풍선
        infoWindowData?.let { (latLngValue, addressValue) ->
            MapInfoBalloon(
                address = addressValue,
                latLng = latLngValue,
                onDismissRequest = { infoWindowData = null } // 바깥 클릭 또는 백 버튼 시 null로 만들어 숨김
                // modifier = Modifier.align(Alignment.Center) // 필요시 Popup 위치 조정
            )
        }

        // 상단 주소검색 모달
        AddressSearchModal(
            onSearch = { query ->
                // (1) Geocode API 등으로 query → LatLng 변환 후 cameraPosition 갱신
                onAddressSearch(query)
            }
        )
    }
}

fun addOrUpdateMarker(
    naverMap: NaverMap,
    latLng: LatLng,
    data: MapMarkerData,
    onClick: (Pair<LatLng, String>) -> Unit
) {
    val marker = Marker().apply {
        position = latLng
        map = naverMap
        // 마커 색상/아이콘 타입 구분
        iconTintColor = when (data.type) {
            MarkerType.USER -> 0xFF2962FF.toInt() // 파랑
            MarkerType.CRIMINAL -> 0xFFD32F2F.toInt() // 빨강
            MarkerType.SAFETY_BELL -> 0xFF43A047.toInt() // 초록
        }
        setOnClickListener {
            onClick(latLng to data.address)
            true
        }
    }
}

