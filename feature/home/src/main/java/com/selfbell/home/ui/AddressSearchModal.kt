package com.selfbell.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Typography 등 커스텀 텍스트 스타일이 따로 있다면 아래도 추가
import com.selfbell.core.ui.theme.Typography
// R 파일 import (ic_search, ic_sos_map, ic_crime_map 등)
import com.selfbell.feature.home.R


@Composable
fun AddressSearchModal(
    modifier: Modifier = Modifier,
    query: String = "",
    onSearch: (String) -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
            .shadow(elevation = 8.dp, spotColor = Color(0x14000000), ambientColor = Color(0x14000000))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x80FFFFFF))
            .border(
                width = 1.dp,
                color = Color(0x4DFFFFFF),
                shape = RoundedCornerShape(24.dp)
            ),
        color = Color.Transparent
    ) {
        var searchText by remember { mutableStateOf(query) }

        Column(modifier = Modifier.padding(16.dp)) {

            // ==== 검색 입력창 ====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        onSearch(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    placeholder = { Text("내 주변 탐색") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = Typography.bodyMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "검색",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            // ====== 이하 기존 리스트 UI ======
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sos_icon),
                    contentDescription = "SOS 아이콘",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "긴급신고-선유공원앞",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
                Text(
                    text = "358m",
                    style = Typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.criminal_icon),
                    contentDescription = "경고 아이콘",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "범죄자 위치정보",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
                Text(
                    text = "421m",
                    style = Typography.bodyMedium
                )
            }
        }
    }
}
