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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.ContactListItem
import com.selfbell.core.ui.composables.AgreeTermsBottomSheet
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Typography
import kotlinx.coroutines.launch

data class Contact(
    val id: Int,
    val name: String,
    val phoneNumber: String
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactRegistrationScreen(navController: NavController) {
    val allContacts = remember {
        listOf(
            Contact(1, "엄마", "010-1234-5678"),
            Contact(2, "아빠", "010-1234-1234"),
            Contact(3, "누나", "010-5678-5678"),
            Contact(4, "김민석", "010-1111-1111"),
            Contact(5, "카리나", "010-5673-2542"),
            Contact(6, "김철수", "010-9876-5432")
        )
    }

    var selectedContacts by remember { mutableStateOf(setOf<Int>()) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val isButtonEnabled = selectedContacts.isNotEmpty()

    AgreeTermsBottomSheet(
        sheetState = sheetState,
        onAgreeAll = {
            coroutineScope.launch { sheetState.hide() }
            navController.navigate(AppRoute.HOME_ROUTE) // 약관 동의 후 홈 화면으로 이동
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                value = "",
                onValueChange = {},
                placeholder = { Text("연락처 검색", style = Typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allContacts) { contact ->
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

            // 하단 버튼
            SelfBellButton(
                text = "시작하기",
                onClick = {
                    if (isButtonEnabled) {
                        coroutineScope.launch { sheetState.show() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled
            )
        }
    }
}