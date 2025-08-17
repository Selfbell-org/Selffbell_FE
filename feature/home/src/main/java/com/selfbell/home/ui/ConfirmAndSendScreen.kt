package com.selfbell.home.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.selfbell.core.model.Contact
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.Typography
import com.selfbell.feature.home.R
import com.selfbell.core.ui.theme.Primary

@Composable
fun ConfirmAndSendScreen(
    selectedGuardians: List<Contact>,
    selectedMessage: String,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit
) {
    val context = LocalContext.current

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSendClick() // 권한 허용 후 전송 로직 실행
        } else {
            Toast.makeText(context, "문자 발송 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        ReportScreenHeader(
            title = "문자 신고",
            onCloseClick = { onBackClick() }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("다음과 같이 문자를 전송합니다.", style = Typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Text("마지막으로 다시 확인해 주세요.", style = Typography.bodyMedium, textAlign = TextAlign.Center, color = Color(0xFF797479), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        // 전송할 메시지
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "전송할 메시지",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "수정하기",
                color = Primary,
                style = Typography.labelSmall,
                modifier = Modifier.clickable { onBackClick() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(selectedMessage, style = Typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 수신인
        Text("수신인", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = selectedGuardians.joinToString(separator = ", ") { it.name + " (" + it.phoneNumber + ")" },
                style = Typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        // 하단 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelfBellButton(
                text = "이전",
                onClick = onBackClick,
                modifier = Modifier.weight(1f),
                buttonType = SelfBellButtonType.OUTLINED
            )
            SelfBellButton(
                text = "긴급 문자 전송",
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        onSendClick()
                    } else {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    }
                },
                modifier = Modifier.weight(1f),
                buttonType = SelfBellButtonType.PRIMARY_FILLED
            )
        }
    }
}