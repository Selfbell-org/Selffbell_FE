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

//// SelfBellButtonType에 필요한 상태
//enum class SelfBellButtonType {
//    PRIMARY_FILLED,
//    SUCCESS_FILLED,
//    DANGER_FILLED,
//    OUTLINED
//}

// ButtonState enum은 상위 컴포저블에 정의되어야 합니다.
// enum class ButtonState {
//     SELECTED,
//     INVITED,
//     DEFAULT
// }

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
                // ❌ 충돌하는 문구 로직을 제거
                // if (buttonState == ButtonState.INVITED) {
                //     Text(
                //         text = "서버에 등록되지 않은 사용자",
                //         style = Typography.labelSmall,
                //         color = Color.Red
                //     )
                // }
            }
        }

        val buttonType = when (buttonState) {
            ButtonState.SELECTED -> SelfBellButtonType.OUTLINED
            ButtonState.INVITED -> SelfBellButtonType.PRIMARY_FILLED
            ButtonState.DEFAULT -> SelfBellButtonType.PRIMARY_FILLED
        }

        SelfBellButton(
            text = buttonText,
            onClick = onButtonClick,
            modifier = Modifier.width(72.dp),
            buttonType = buttonType,
            isSmall = true,
            enabled = isEnabled
        )
    }
}