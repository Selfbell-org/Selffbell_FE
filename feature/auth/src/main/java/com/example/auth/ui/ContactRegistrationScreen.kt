package com.example.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.ContactListItem
import com.selfbell.core.ui.composables.AgreeTermsBottomSheet
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.Typography
import kotlinx.coroutines.launch
import com.selfbell.domain.model.ContactUser
import com.selfbell.domain.model.ContactRelationshipStatus

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactRegistrationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ContactRegistrationViewModel = hiltViewModel()
) {
    // 📌 ViewModel의 상태를 관찰합니다.
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // 📌 화면이 처음 나타날 때 연락처를 불러옵니다.
    LaunchedEffect(Unit) {
        viewModel.loadContactsWithUserCheck()
    }

    // 📌 UI 상태에 따라 표시할 연락처 목록을 결정합니다.
    val contactList = when (uiState) {
        is ContactUiState.Success -> (uiState as ContactUiState.Success).contacts
        else -> emptyList()
    }

    val filteredContacts = remember(searchQuery, contactList) {
        if (searchQuery.isEmpty()) {
            contactList
        } else {
            contactList.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var selectedContacts by remember { mutableStateOf(setOf<String>()) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    
    // ✅ 서버에 등록된 사용자만 선택 가능하도록 수정
    val availableContacts = filteredContacts.filter { it.isExists }
    val isButtonEnabled = selectedContacts.isNotEmpty() && selectedContacts.all { phoneNumber ->
        availableContacts.any { it.phoneNumber == phoneNumber }
    }

    // 📌 선택된 연락처의 전화번호 목록을 가져옵니다.
    val selectedPhoneNumbers = remember(selectedContacts, contactList) {
        contactList.filter { selectedContacts.contains(it.phoneNumber) }.map { it.phoneNumber }
    }

    AgreeTermsBottomSheet(
        sheetState = sheetState,
        onAgreeAll = {
            coroutineScope.launch {
                sheetState.hide()
                // ✅ 선택된 모든 연락처에 대해 보호자 요청을 보냅니다.
                selectedPhoneNumbers.forEach { phoneNumber ->
                    viewModel.sendContactRequest(phoneNumber)
                }
                navController.navigate(AppRoute.ONBOARDING_COMPLETE_ROUTE)
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnboardingProgressBar(
                    currentStep = 4,
                    totalSteps = 5,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "보호자 연락처를 선택해주세요",
                    style = Typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "최대 3명까지 선택할 수 있습니다",
                    style = Typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 📌 검색 입력 필드
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("연락처 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // 📌 ViewModel 상태에 따라 UI 표시
                when (uiState) {
                    is ContactUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ContactUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = (uiState as ContactUiState.Error).message,
                                style = Typography.bodyMedium,
                                color = Color.Red
                            )
                        }
                    }
                    else -> {
                        if (filteredContacts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "연락처를 찾을 수 없습니다.",
                                    style = Typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(filteredContacts, key = { it.phoneNumber }) { contact ->
                                    ContactListItem(
                                        name = contact.name,
                                        phoneNumber = contact.phoneNumber,
                                        isSelected = selectedContacts.contains(contact.phoneNumber),
                                        isEnabled = contact.isExists, // ✅ 서버 등록 여부에 따라 활성화/비활성화
                                        onButtonClick = {
                                            if (contact.isExists) {
                                                selectedContacts = if (selectedContacts.contains(contact.phoneNumber)) {
                                                    selectedContacts - contact.phoneNumber
                                                } else {
                                                    if (selectedContacts.size < 3) {
                                                        selectedContacts + contact.phoneNumber
                                                    } else {
                                                        selectedContacts
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SelfBellButton(
                text = "시작하기",
                onClick = {
                    if (isButtonEnabled) {
                        coroutineScope.launch { sheetState.show() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp),
                enabled = isButtonEnabled
            )
        }
    }
}