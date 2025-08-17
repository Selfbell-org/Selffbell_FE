package com.selfbell.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.selfbell.core.model.Contact
import com.selfbell.core.ui.composables.ContactListItem
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.Gray50
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Typography
import com.selfbell.feature.home.R

@Composable
fun SelectRecipientScreen(
    guardians: List<Contact>,
    messageTemplates: List<String>,
    onCancelClick: () -> Unit,
    onNextClick: (selectedGuardians: List<Contact>, message: String) -> Unit,
    selectedGuardians: List<Contact>,
    onGuardianSelect: (Contact) -> Unit, // Boolean 매개변수 제거 (토글 로직은 상위에서 처리)
    selectedMessage: String,
    onMessageSelect: (String) -> Unit
) {
    val isNextButtonEnabled = selectedGuardians.isNotEmpty() && selectedMessage.isNotEmpty()

    // 이 Composable에서 상태를 직접 관리하는 대신, 매개변수로 받은 guardians를 활용합니다.
    // 이는 'ViewModel'에서 상태를 관리하고 UI는 그 상태를 표시하는 역할만 하도록 분리하는 Compose의 권장사항에 따릅니다.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // 아이콘을 포함한 상단 헤더
        ReportScreenHeader(
            title = "문자 신고",
            onCloseClick = onCancelClick
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 상단 안내 텍스트
        Text(
            "긴급상황 시 지인에게 도움을 요청합니다.",
            style = Typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color(0xFF797479),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 수신 보호자 선택 영역 (테두리 추가)
        Text(
            "문자 수신 보호자 선택",
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 로딩 또는 빈 화면 처리
        if (guardians.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // 높이 설정
                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "등록된 연락처가 없습니다.",
                    style = Typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    items(guardians) { contact ->
                        ContactListItem(
                            name = contact.name,
                            phoneNumber = contact.phoneNumber,
                            isSelected = contact in selectedGuardians,
                            onButtonClick = {
                                onGuardianSelect(contact)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 메시지 템플릿 선택 영역
        Text(
            "메시지 템플릿 선택",
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // "직접 입력" 필드 추가
        OutlinedTextField(
            value = selectedMessage,
            onValueChange = { newValue -> onMessageSelect(newValue) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("직접 입력...") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            items(messageTemplates) { message ->
                MessageTemplateItem(
                    message = message,
                    isSelected = message == selectedMessage,
                    onSelect = { onMessageSelect(it) }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        // 하단 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelfBellButton(
                text = "취소",
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                buttonType = SelfBellButtonType.OUTLINED
            )
            SelfBellButton(
                text = "다음",
                onClick = { onNextClick(selectedGuardians, selectedMessage) },
                modifier = Modifier.weight(1f),
                enabled = isNextButtonEnabled,
                buttonType = SelfBellButtonType.PRIMARY_FILLED
            )
        }
    }
}

// MessageTemplateItem 디자인 수정
@Composable
fun MessageTemplateItem(message: String, isSelected: Boolean, onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary else Gray50)
            .border(
                width = 1.dp,
                color = if (isSelected) Primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect(message) }
            .padding(vertical = 12.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = if (isSelected) Color.White else Color.Black,
            style = Typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}