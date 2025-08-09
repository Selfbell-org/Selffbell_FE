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
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.SelfBellTheme

@Composable
fun ContactListItem(
    name: String,
    phoneNumber: String,
    isSelected: Boolean,
    onButtonClick: () -> Unit,
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
            }
        }

        SelfBellButton(
            text = if (isSelected) "해제" else "선택",
            onClick = onButtonClick,
            modifier = Modifier.width(72.dp),
            buttonType = if (isSelected) SelfBellButtonType.PRIMARY_FILLED else SelfBellButtonType.OUTLINED,
            isSmall = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactListItemPreview() {
    SelfBellTheme {
        Column {
            ContactListItem(
                name = "엄마",
                phoneNumber = "010-1234-5678",
                isSelected = true,
                onButtonClick = {}
            )
            ContactListItem(
                name = "김민석",
                phoneNumber = "010-1111-1111",
                isSelected = false,
                onButtonClick = {}
            )
        }
    }
}