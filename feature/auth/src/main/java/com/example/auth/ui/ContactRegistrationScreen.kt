package com.example.auth.ui

import android.util.Log
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
import com.selfbell.core.ui.composables.ContactRegistrationListItem
import com.selfbell.core.ui.composables.AgreeTermsBottomSheet
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.Typography
import kotlinx.coroutines.launch
import com.selfbell.domain.model.ContactUser
import com.selfbell.domain.model.ContactRelationshipStatus
import com.selfbell.core.ui.composables.ButtonState
import com.selfbell.core.ui.composables.SelfBellButtonType

// ✅ ButtonState enum 클래스 정의 (ContactRegistrationListItem에서도 사용)
enum class ButtonState {
    SELECTED, // 해제 (빨간색)
    INVITED,  // 초대 (초록색)
    DEFAULT   // 선택 (기본색)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactRegistrationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    isFromSettings: Boolean,
    viewModel: ContactRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDeviceContactsOnly()
    }

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
    var inviteContacts by remember { mutableStateOf(setOf<String>()) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val isButtonEnabled = selectedContacts.isNotEmpty()

    val selectedPhoneNumbers = remember(selectedContacts, contactList) {
        contactList.filter { selectedContacts.contains(it.phoneNumber) }.map { it.phoneNumber }
    }

    AgreeTermsBottomSheet(
        sheetState = sheetState,
        onAgreeAll = {
            coroutineScope.launch {
                sheetState.hide()
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
                    totalSteps = 4,
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

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("연락처 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

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
                                    val isInvited = inviteContacts.contains(contact.phoneNumber)
                                    val isSelected = selectedContacts.contains(contact.phoneNumber)

                                    val buttonState = when {
                                        isSelected -> ButtonState.SELECTED
                                        isInvited -> ButtonState.INVITED
                                        else -> ButtonState.DEFAULT
                                    }

                                    val buttonText = when (buttonState) {
                                        ButtonState.SELECTED -> "해제"
                                        ButtonState.INVITED -> "초대"
                                        ButtonState.DEFAULT -> "선택"
                                    }

                                    val isButtonEnabled = true

                                    Column { // ✅ ContactRegistrationListItem을 Column으로 감싸기
                                        ContactRegistrationListItem(
                                            name = contact.name,
                                            phoneNumber = contact.phoneNumber,
                                            buttonText = buttonText,
                                            isEnabled = isButtonEnabled,
                                            buttonState = buttonState,
                                            onButtonClick = {
                                                if (isSelected) {
                                                    selectedContacts = selectedContacts - contact.phoneNumber
                                                } else if (isInvited) {
                                                    viewModel.inviteContact(contact.phoneNumber)
                                                } else {
                                                    viewModel.checkUserExists(contact.phoneNumber) { exists ->
                                                        if (exists) {
                                                            if (selectedContacts.size < 3) {
                                                                selectedContacts = selectedContacts + contact.phoneNumber
                                                            } else {
                                                                // TODO: 최대 선택 개수 초과 알림
                                                            }
                                                        } else {
                                                            inviteContacts = inviteContacts + contact.phoneNumber
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                        // ✅ 상태 라벨을 ContactRegistrationListItem 아래에 조건부로 표시
                                        if (isInvited) {
                                            Text(
                                                text = "서버에 등록되지 않은 사용자",
                                                style = Typography.labelSmall,
                                                color = Color.Red,
                                                modifier = Modifier.padding(start = 64.dp) // 아이콘과 정렬 맞추기
                                            )
                                        }
                                        Divider()
                                    }
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