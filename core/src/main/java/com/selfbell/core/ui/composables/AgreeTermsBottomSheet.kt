package com.selfbell.core.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AgreeTermsBottomSheet(
    sheetState: ModalBottomSheetState,
    onAgreeAll: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "약관에 동의해주세요",
                    style = Typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "여러분의 소중한 개인정보를 잘 지켜 드릴게요",
                    style = Typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                var agreeToTerms by remember { mutableStateOf(false) }
                var agreeToPersonalInfo by remember { mutableStateOf(false) }

                TermCheckboxItem(
                    text = "이용약관 동의",
                    isChecked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TermCheckboxItem(
                    text = "개인정보 수집 이용 동의",
                    isChecked = agreeToPersonalInfo,
                    onCheckedChange = { agreeToPersonalInfo = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                val isAllAgreed = agreeToTerms && agreeToPersonalInfo

                // SelfBellButton으로 대체
                SelfBellButton(
                    text = "모두 동의하고 시작하기",
                    onClick = {
                        if (isAllAgreed) {
                            onAgreeAll()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAllAgreed
                )
            }
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetElevation = 8.dp
    ) {
        content()
    }
}

@Composable
private fun TermCheckboxItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Primary
            )
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = Typography.bodyMedium
        )
        Text(
            text = "필수",
            color = Primary,
            style = Typography.labelSmall
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun AgreeTermsBottomSheetPreview() {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    AgreeTermsBottomSheet(
        sheetState = sheetState,
        onAgreeAll = {}
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SelfBellButton(text = "Show Bottom Sheet", onClick = { coroutineScope.launch { sheetState.show() } })
        }
    }
}