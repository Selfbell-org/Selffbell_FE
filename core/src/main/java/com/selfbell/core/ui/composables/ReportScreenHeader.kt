package com.selfbell.core.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.R
import com.selfbell.core.ui.theme.Typography

@Composable
fun ReportScreenHeader(
    title: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_search), // 닫기 아이콘
            contentDescription = "닫기",
            modifier = Modifier
                .size(24.dp)
                .clickable { onCloseClick() }
        )
        Text(
            text = title,
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 24.dp) // 아이콘 크기만큼 패딩을 주어 중앙 정렬
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenHeaderPreview() {
    ReportScreenHeader(
        title = "문자 신고",
        onCloseClick = {}
    )
}