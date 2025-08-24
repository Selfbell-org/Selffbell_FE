// core/src/main/java/com/selfbell/core/ui/composables/SelfBellButton.kt
package com.selfbell.core.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Success // ✅ Success 색상 import
import com.selfbell.core.ui.theme.Danger // ✅ Danger 색상 import

// ✅ SelfBellButtonType enum에 새로운 타입 추가
enum class SelfBellButtonType {
    PRIMARY_FILLED,
    OUTLINED,
    TEXT_ONLY,
    FAB,
    SUCCESS_FILLED, // ✅ 성공 상태를 위한 버튼 (초록색)
    DANGER_FILLED ,  // ✅ 위험 상태를 위한 버튼 (빨간색)
    LIGHTER_FILLED
}

@Composable
fun SelfBellButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: SelfBellButtonType = SelfBellButtonType.PRIMARY_FILLED,
    enabled: Boolean = true,
    // Add a new parameter to easily control the button's size
    isSmall: Boolean = false
) {
    val backgroundColor: Color
    val contentColor: Color
    val borderColor: Color?
    val cornerRadius: Int = 8 // Figma Corner radius

    // Set content padding based on size
    val contentPadding = if (isSmall) {
        PaddingValues(horizontal = 24.dp, vertical = 8.dp) // Smaller padding for "출발" button
    } else {
        PaddingValues(horizontal = 16.dp, vertical = 12.dp) // Standard padding
    }

    // ✅ when 구문에 새로운 타입 추가
    when (buttonType) {
        SelfBellButtonType.PRIMARY_FILLED -> {
            backgroundColor = Primary
            contentColor = Color.White
            borderColor = null
        }
        SelfBellButtonType.LIGHTER_FILLED -> {
            backgroundColor =  Color(0xFFEFF4FF)
            contentColor = Color.Black
            borderColor = null
        }
        SelfBellButtonType.OUTLINED -> {
            backgroundColor = Color.Transparent
            contentColor = Black
            borderColor = Black
        }
        SelfBellButtonType.TEXT_ONLY -> {
            backgroundColor = Color.Transparent
            contentColor = Primary
            borderColor = null
        }
        SelfBellButtonType.FAB -> {
            backgroundColor = Primary
            contentColor = Color.White
            borderColor = null
        }
        SelfBellButtonType.SUCCESS_FILLED -> { // ✅ 성공 상태 버튼 (초록색)
            backgroundColor = Success
            contentColor = Color.White
            borderColor = null
        }
        SelfBellButtonType.DANGER_FILLED -> { // ✅ 위험 상태 버튼 (빨간색)
            backgroundColor = Danger
            contentColor = Color.White
            borderColor = null
        }
    }

    Button(
        onClick = onClick,
        // Remove fixed height and width to allow wrapping
        modifier = modifier.wrapContentSize(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = GrayInactive,
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(cornerRadius.dp),
        border = borderColor?.let { BorderStroke(1.dp, it) },
        contentPadding = contentPadding // Use dynamic padding
    ) {
        Text(
            text = text,
            style = if (isSmall) Typography.bodyMedium else Typography.titleMedium, // Adjust text style if small
            color = contentColor
        )
    }
}