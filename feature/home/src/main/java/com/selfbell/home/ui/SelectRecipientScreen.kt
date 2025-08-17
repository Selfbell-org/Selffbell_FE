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
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
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
    onGuardianSelect: (Contact, Boolean) -> Unit,
    selectedMessage: String,
    onMessageSelect: (String) -> Unit
) {
    val isNextButtonEnabled = selectedGuardians.isNotEmpty() && selectedMessage.isNotEmpty()

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(horizontal = 12.dp)
            ) {
                items(guardians) { contact ->
                    GuardianItem(
                        contact = contact,
                        isSelected = contact in selectedGuardians,
                        onSelect = { isSelected -> onGuardianSelect(contact, isSelected) }
                    )
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

// GuardianItem 디자인 수정
@Composable
fun GuardianItem(contact: Contact, isSelected: Boolean, onSelect: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = com.selfbell.core.R.drawable.default_profile_icon2), // 더미 프로필 아이콘
            contentDescription = "프로필",
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(999.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(text = contact.name, style = Typography.bodyMedium)
            Text(text = contact.phoneNumber, style = Typography.labelSmall)
        }
        Text(
            text = if (isSelected) "해제" else "선택",
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier
                .background(
                    color = if (isSelected) Color.Red else Color.Transparent,
                    shape = RoundedCornerShape(999.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color.Transparent else Color.LightGray,
                    shape = RoundedCornerShape(999.dp)
                )
                .clickable { onSelect(!isSelected) }
                .padding(vertical = 6.dp, horizontal = 16.dp)
        )
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
            .background(if (isSelected) Primary else Color(0xFFF0F0F0))
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