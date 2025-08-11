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

// Contact 데이터 클래스는 ViewModel 파일에 정의되어 있습니다.

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactRegistrationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ContactRegistrationViewModel = hiltViewModel()
) {
    val allContacts by viewModel.allContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    val filteredContacts = remember(searchQuery, allContacts) {
        if (searchQuery.isEmpty()) {
            allContacts
        } else {
            allContacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var selectedContacts by remember { mutableStateOf(setOf<Long>()) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val isButtonEnabled = selectedContacts.isNotEmpty()

    AgreeTermsBottomSheet(
        sheetState = sheetState,
        onAgreeAll = {
            coroutineScope.launch {
                sheetState.hide()
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
                Spacer(modifier = Modifier.height(20.dp))
                OnboardingProgressBar(currentStep = 4, totalSteps = 4)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "보호자 연락처를 추가해주세요.",
                    style = Typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "최대 3명까지 가능합니다.",
                    style = Typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("연락처 검색", style = Typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (allContacts.isEmpty() && searchQuery.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredContacts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "연락처를 찾을 수 없습니다.",
                            style = Typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredContacts, key = { it.id }) { contact ->
                            ContactListItem(
                                name = contact.name,
                                phoneNumber = contact.phoneNumber,
                                isSelected = selectedContacts.contains(contact.id),
                                onButtonClick = {
                                    selectedContacts = if (selectedContacts.contains(contact.id)) {
                                        selectedContacts - contact.id
                                    } else {
                                        if (selectedContacts.size < 3) {
                                            selectedContacts + contact.id
                                        } else {
                                            selectedContacts
                                        }
                                    }
                                }
                            )
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