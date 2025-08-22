package com.selfbell.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.R
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.auth.ui.AuthViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val profileName by viewModel.userName.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    val profileImageRes by remember { mutableStateOf(R.drawable.default_profile_icon2) }
    var alertEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        UserProfileSection(name = profileName ?: "로딩 중...", profileImageRes = profileImageRes)

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            item {
                Text(
                    text = "계정 설정",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
            }
            item {
                SettingsMenuItem(
                    label = "프로필 관리",
                    onClick = {
                        navController.navigate(AppRoute.PROFILE_REGISTER_ROUTE_WITH_ARGS)
                    }
                )
            }
            item {
                SettingsMenuItem(
                    label = "친구 관리",
                    onClick = {
                        navController.navigate(AppRoute.FRIENDS_ROUTE)
                    }
                )
            }
            item {
                SettingsMenuItem(
                    label = "긴급 연락처 관리",
                    onClick = {
                        navController.navigate(AppRoute.CONTACT_REGISTER_ROUTE)
                    }
                )
            }
            item {
                SettingsMenuItem(
                    label = "메인 주소 설정",
                    onClick = {
                        navController.navigate(AppRoute.ADDRESS_REGISTER_ROUTE)
                    }
                )
            }
            item {
                SettingsSwitchItem(
                    label = "긴급 알림 받기",
                    isChecked = alertEnabled,
                    onCheckedChange = { alertEnabled = it }
                )
            }
            item {
                SettingsMenuItem(
                    label = "권한 설정",
                    onClick = {
                        navController.navigate(AppRoute.PERMISSION_ROUTE)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        SelfBellButton(
            text = "로그아웃",
            onClick = {
                // TODO: 로그아웃 로직
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UserProfileSection(
    name: String,
    profileImageRes: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            //.shadow(4.dp, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = profileImageRes),
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun SettingsMenuItem(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = Typography.bodyMedium)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "이동",
            tint = Color.Gray
        )
    }
}

@Composable
fun SettingsSwitchItem(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = Typography.bodyMedium)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SelfBellTheme {
        SettingsScreen(navController = rememberNavController())
    }
}