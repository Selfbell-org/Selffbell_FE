package com.selfbell.core.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.R
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.composables.ButtonState // ✅ ButtonState import


@Composable
fun ContactRegistrationListItem(
    name: String,
    phoneNumber: String,
    buttonText: String,
    isEnabled: Boolean,
    onButtonClick: () -> Unit,
    buttonState: ButtonState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_icon2),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = Typography.titleMedium
                )
                Text(
                    text = phoneNumber,
                    style = Typography.bodyMedium,
                    color = Color.Gray
                )
                // 서버에 등록되지 않은 사용자에게 빨간색 문구 띄우기
                if (buttonState == ButtonState.INVITED) { // ✅ 상태에 따라 문구 표시
                    Text(
                        text = "서버에 등록되지 않은 사용자",
                        style = Typography.labelSmall,
                        color = Color.Red
                    )
                }
            }
        }

        // ✅ buttonState에 따라 buttonType을 결정하는 when 문
        val buttonType = when (buttonState) {
            ButtonState.SELECTED -> SelfBellButtonType.OUTLINED  // 선택 시: 빨간색 "해제"
            ButtonState.INVITED -> SelfBellButtonType.PRIMARY_FILLED // ✅ 초대 시: 주 색상 "초대"
            ButtonState.DEFAULT -> SelfBellButtonType.PRIMARY_FILLED  // 기본 상태: 주 색상 "선택"
        }

        SelfBellButton(
            text = buttonText,
            onClick = onButtonClick,
            modifier = Modifier.width(72.dp),
            // ✅ 결정된 buttonType을 적용
            buttonType = buttonType,
            isSmall = true,
            enabled = isEnabled
        )
    }
}