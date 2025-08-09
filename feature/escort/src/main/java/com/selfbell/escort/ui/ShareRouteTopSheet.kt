// feature/escort/ui/ShareRouteTopSheet.kt
package com.selfbell.escort.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.model.Contact
import com.selfbell.core.R
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Black
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ShareRouteTopSheet(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredContacts: List<Contact>,
    // onShareClick 파라미터를 Contact를 받는 콜백으로 다시 수정합니다.
    onShareClick: (Contact) -> Unit,
    onCloseClick: () -> Unit
) {
    var sharedContacts by remember { mutableStateOf(setOf<Long>()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 50.dp)
            .shadow(10.dp, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "동선을 공유할 친구를 선택해주세요.",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCloseClick) {
                    Icon(Icons.Default.Close, contentDescription = "닫기")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("친구 이름 검색..") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
            ) {
                if (filteredContacts.isEmpty()) {
                    item {
                        Text(
                            text = "연락처를 찾을 수 없습니다.",
                            style = Typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(filteredContacts) { contact ->
                        val isShared = sharedContacts.contains(contact.id)
                        FriendContactItem(
                            contact = contact,
                            isShared = isShared,
                            onShareClick = {
                                if (!isShared) {
                                    onShareClick(contact)
                                    sharedContacts = sharedContacts + contact.id
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendContactItem(contact: Contact, isShared: Boolean, onShareClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_icon2),
                contentDescription = "프로필",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(contact.name, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("Information | Friend", style = Typography.labelSmall, color = Color.Gray)
            }
        }
        SelfBellButton(
            text = if (isShared) "공유 완료" else "공유",
            onClick = onShareClick,
            modifier = Modifier.width(140.dp), // <-- 여기서 width를 140dp로 고정
            enabled = !isShared,
            buttonType = if (isShared) SelfBellButtonType.OUTLINED else SelfBellButtonType.PRIMARY_FILLED
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShareRouteTopSheetPreview() {
    SelfBellTheme {
        val sampleContacts = listOf(
            Contact(1, "나는돌맹이", "010-1234-5678"),
            Contact(2, "고앵이", "010-9876-5432")
        )
        var searchQuery by remember { mutableStateOf("") }
        val filtered = remember(searchQuery) {
            sampleContacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        ShareRouteTopSheet(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            filteredContacts = filtered,
            onShareClick = { contact -> /* preview */ },
            onCloseClick = { /* preview */ }
        )
    }
}