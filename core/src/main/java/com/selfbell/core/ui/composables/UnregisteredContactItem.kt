package com.selfbell.core.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.selfbell.core.R
import com.selfbell.core.ui.theme.Typography

@Composable
fun UnregisteredContactItem(
    name: String,
    phoneNumber: String,
    onInviteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_icon2),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = name, style = Typography.titleMedium)
                Text(text = phoneNumber, style = Typography.bodyMedium, color = Color.Gray)
                Text(text = "서버 미가입자", style = Typography.labelSmall, color = Color.Red)
            }
        }

        SelfBellButton(
            text = "초대",
            onClick = onInviteClick,
            modifier = Modifier.width(72.dp),
            buttonType = SelfBellButtonType.PRIMARY_FILLED,
            isSmall = true
        )
    }
}


