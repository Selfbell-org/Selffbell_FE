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
            Contact(1, "김민석", "010-1111-1111"),
            Contact(2, "김민준", "010-2222-2222"),
            Contact(3, "김민서", "010-3333-3333")
        )
    }
    val dummyMessageTemplates = remember {
        listOf(
            "위급 상황입니다.",
            "갇혔어요.",
            "위협 받고 있어요."
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
                    onGuardianSelect = { contact, isSelected ->
                        selectedGuardians = if (isSelected) {
                            selectedGuardians + contact
                        } else {
                            selectedGuardians - contact
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