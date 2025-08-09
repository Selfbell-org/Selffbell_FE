package com.selfbell.core.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.model.Contact // 보호자 연락처 데이터 클래스 (ViewModel과 동일)
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.R
import androidx.compose.ui.graphics.Color

@Composable
fun MessageReportBottomSheet(
    modifier: Modifier = Modifier,
    // ViewModel에서 가져온 보호자 목록
    selectedGuardians: List<Contact>,
    // 선택 가능한 메시지 템플릿 목록
    messageTemplates: List<String>,
    onSendClick: (List<Contact>, String) -> Unit,
    onCancelClick: () -> Unit
) {
    // 메시지 신고 팝업의 전체 레이아웃
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 아이콘 및 제목
        Image(
            painter = painterResource(id = R.drawable.msg_report_icon), // 아이콘 리소스
            contentDescription = "문자 신고하기",
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "문자 신고하기",
            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(24.dp))

        // 보호자 목록
        Text("문자 수신 번호 선택 ▼", style = Typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // 높이 제한
                .border(1.dp, GrayInactive, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(selectedGuardians) { contact ->
                GuardianContactItem(contact = contact)
            }
        }
        Spacer(Modifier.height(16.dp))

        // 메시지 템플릿
        Text("메시지 템플릿 선택 ▼", style = Typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // 높이 제한
                .border(1.dp, GrayInactive, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(messageTemplates) { template ->
                MessageTemplateItem(template = template)
            }
        }
        Spacer(Modifier.height(24.dp))

        // 버튼 영역
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GrayInactive)
            ) {
                Text("취소하기", color = Color.Black)
            }
            Button(
                onClick = {
                    val message = "위급 상황입니다." // TODO: 선택된 템플릿으로 변경
                    onSendClick(selectedGuardians, message)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("보내기", color = Color.White)
            }
        }
    }
}

// 보호자 연락처 한 줄 UI
@Composable
fun GuardianContactItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 선택 로직 추가 */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Image(
            painter = painterResource(id = R.drawable.msg_report_icon), // TODO: 실제 프로필 이미지로 교체
            contentDescription = "보호자 프로필",
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(50))
        )
        Spacer(Modifier.width(16.dp))
        // 이름 및 정보
        Column {
            Text(contact.name, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(contact.phoneNumber, style = Typography.labelSmall)
        }
    }
}

// 메시지 템플릿 한 줄 UI
@Composable
fun MessageTemplateItem(template: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 선택 로직 추가 */ }
            .padding(vertical = 8.dp)
    ) {
        Text(template, style = Typography.bodyMedium)
    }
}