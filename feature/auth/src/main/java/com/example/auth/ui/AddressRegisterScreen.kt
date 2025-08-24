package com.example.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Primary
import com.selfbell.domain.model.AddressModel
import com.example.auth.ui.AddressResultItem
import com.naver.maps.map.CameraUpdate
import kotlin.text.ifEmpty
import kotlin.text.isBlank

@Composable
fun AddressRegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    isFromSettings: Boolean,
    viewModel: AddressRegisterViewModel = hiltViewModel()
) {
    // ViewModel의 개별 StateFlow 변수들을 사용합니다.
    val searchAddress by viewModel.searchAddress.collectAsState()
    val addressResults by viewModel.addressResults.collectAsState()
    val isAddressSelected by viewModel.isAddressSelected.collectAsState()
    val selectedLatLng by viewModel.selectedLatLng.collectAsState()
    val selectedAddressDetail by viewModel.selectedAddressDetail.collectAsState()

    val totalOnboardingSteps = 4
    val currentOnboardingStep = 3

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. 네이버 지도 API 영역 (가장 아래 레이어)

        // 전체 UI를 담는 Box에서 Column으로 변경하여 요소들을 수직 정렬하고,
        // 키보드에 따라 UI가 밀리도록 imePadding() 추가
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .imePadding(), // 키보드에 따라 UI가 밀리도록
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. 상단 고정 영역 (온보딩 바, 타이틀) ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                OnboardingProgressBar(
                    currentStep = currentOnboardingStep,
                    totalSteps = totalOnboardingSteps
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "자주 이용하는 메인주소를\n등록해 주세요.",
                    style = Typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface // 테마 색상 사용
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- 2. 주소 검색 및 결과 표시 영역 (지도 표시는 다음 단계) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // 하단 버튼을 제외한 남은 공간 차지
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 주소 검색 TextField
                TextField(
                    value = searchAddress,
                    onValueChange = { viewModel.updateSearchAddress(it) },
                    label = { Text("건물명, 도로명, 지번으로 검색") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색 아이콘") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors( // Material 3 스타일의 TextField 색상
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        ),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent, // 밑줄 제거
                        unfocusedIndicatorColor = Color.Transparent, // 밑줄 제거
                        disabledIndicatorColor = Color.Transparent // 밑줄 제거
                    ),
                    shape = RoundedCornerShape(12.dp) // 모서리 둥글게
                )
                Spacer(modifier = Modifier.height(16.dp))

                // "현재 위치로 찾기" 버튼
                SelfBellButton(
                    text = "현재 위치로 찾기",
                    onClick = {
                        viewModel.getCurrentLocationAddress() // ViewModel 함수 호출
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp)) // 버튼과 결과 목록 사이 간격

                // 검색 결과 목록 UI 또는 검색 예시 안내
                if (isAddressSelected) {
                    // 주소가 선택된 후의 UI (다음 단계에서 지도와 함께 표시될 부분)
                    // 현재는 비워두거나, 선택된 주소 텍스트 정도만 간단히 표시 가능
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text("주소 선택 완료!", style = Typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            searchAddress,
                            style = Typography.bodyLarge
                        ) // 선택 시 TextField 값은 선택된 주소로 바뀜
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.resetSelection() }) {
                            Text("다시 검색", color = Primary)
                        }
                    }

                } else if (addressResults.isNotEmpty()) {
                    // 검색 결과가 있으면 LazyColumn 표시
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            // .heightIn(max = 200.dp) // 필요시 최대 높이 제한
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp), // 테마 색상
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 16.dp) // 항목 내부 상하 패딩
                    ) {
                        items(addressResults.take(5)) { address -> // 최대 5개 항목만 표시
                            AddressResultItem(address = address) { // AddressModel 전달
                                viewModel.selectAddress(address)
                            }
                            if (addressResults.take(5).last() != address) { // 마지막 항목이 아니면 구분선 추가
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else if (searchAddress.isBlank()) {
                    // 검색 결과도 없고, 검색창도 비어있을 때 (주소 선택 안 된 상태) 검색 예시 안내
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), // 테마 색상
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "이렇게 검색해 보세요",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "・ 도로명 + 건물번호 (예: 위례성대로 2)",
                            style = Typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            text = "・ 건물명 + 번지 (예: 방이동 44-2)",
                            style = Typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            text = "・ 건물명, 아파트명 (예: 반포자이)",
                            style = Typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                } else {
                    // 검색어를 입력했지만 결과가 없는 경우 (선택적 UI)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "검색 결과가 없습니다.",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "다른 검색어를 입력해보세요.",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // 지도 및 선택된 주소 상세 정보 (다음 단계에서 ReusableNaverMap과 함께 여기에 추가)
                // if (isAddressSelected && selectedAddressDetail != null && selectedLatLng != null) { ... }
            } // End of Middle Scrollable Column

            // --- 3. 하단 "다음으로" 버튼 영역 ---
            SelfBellButton(
                text = "다음으로",
                onClick = {
                    // 선택된 주소 정보를 파라미터로 전달
                    selectedLatLng?.let { latLng ->
                        selectedAddressDetail?.let { selectedAddress ->
                            val addressText = selectedAddress.roadAddress.ifEmpty { selectedAddress.jibunAddress }
                            navController.navigate(
                                AppRoute.mainAddressSetupRoute(
                                    address = addressText,
                                    lat = latLng.latitude.toFloat(),
                                    lng = latLng.longitude.toFloat()
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 20.dp) // 상단 간격 및 하단 시스템 네비게이션 바 고려
                    .navigationBarsPadding(), // 하단 시스템 네비게이션 바 영역 피하기
                enabled = isAddressSelected && selectedLatLng != null && selectedAddressDetail != null // 주소 선택 시에만 활성화
            )
        }
    }
}
