package com.selfbell.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfbell.core.model.Contact
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.theme.Primary

enum class MessageReportStep {
    SELECT_RECIPIENT,
    CONFIRM_SEND,
    SEND_COMPLETE
}

@Composable
fun MessageReportFlow(
    onDismissRequest: () -> Unit,
    sendSms: (guardians: List<Contact>, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(MessageReportStep.SELECT_RECIPIENT) }
    var selectedGuardians by remember { mutableStateOf(emptyList<Contact>()) }
    var selectedMessage by remember { mutableStateOf("") }

    val dummyGuardians = remember {
        listOf(
            Contact(1, 1,"김민석", "010-1111-1111"),
            Contact(2, 2, "김민준", "010-2222-2222"),
            Contact(3, 3, "김민서", "010-3333-3333")
        )
    }
    val dummyMessageTemplates = remember {
        listOf(
            "위급 상황입니다. 전화하지 마시고 위치를 확인해 주세요.",
            "갇혔어요. 제 위치를 확인해 주세요.",
            "위협 받고 있어요. 조용히 도움을 요청합니다.",
            "지금 바로 경찰에 신고해 주세요.",
            "다쳤습니다. 119를 불러주세요.",
            "위급 상황이니 제가 보낸 위치로 와주세요.",
            "도움이 필요합니다. 연락 부탁드립니다.",
            "괜찮아요. 걱정하지 마세요.",
            "잠시 후 다시 연락드릴게요.",
            "회의 중입니다. 문자로 용건 남겨주세요."
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        when (currentStep) {
            MessageReportStep.SELECT_RECIPIENT -> {
                SelectRecipientScreen(
                    guardians = dummyGuardians,
                    messageTemplates = dummyMessageTemplates,
                    onCancelClick = onDismissRequest,
                    onNextClick = { guardians, message ->
                        selectedGuardians = guardians
                        selectedMessage = message
                        currentStep = MessageReportStep.CONFIRM_SEND
                    },
                    selectedGuardians = selectedGuardians,
                    // onGuardianSelect 로직 수정: isSelected 매개변수 없이 토글 로직 구현
                    onGuardianSelect = { contact ->
                        selectedGuardians = if (selectedGuardians.contains(contact)) {
                            selectedGuardians - contact // 이미 선택된 경우 제거
                        } else {
                            // 3개까지만 선택 가능하도록 제약 추가
                            if (selectedGuardians.size < 3) {
                                selectedGuardians + contact
                            } else {
                                selectedGuardians // 3개 이상이면 변경 없음
                            }
                        }
                    },
                    selectedMessage = selectedMessage,
                    onMessageSelect = { message ->
                        selectedMessage = message
                    }
                )
            }
            MessageReportStep.CONFIRM_SEND -> {
                ConfirmAndSendScreen(
                    selectedGuardians = selectedGuardians,
                    selectedMessage = selectedMessage,
                    onBackClick = { currentStep = MessageReportStep.SELECT_RECIPIENT },
                    onSendClick = {
                        sendSms(selectedGuardians, selectedMessage)
                        currentStep = MessageReportStep.SEND_COMPLETE
                    }
                )
            }
            MessageReportStep.SEND_COMPLETE -> {
                SendCompleteScreen(
                    onDismissClick = onDismissRequest
                )
            }
        }
    }
}