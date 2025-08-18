package com.example.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.R
import com.selfbell.auth.ui.AuthUiState
import com.selfbell.auth.ui.AuthViewModel
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Pretendard
import com.selfbell.core.ui.theme.White
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.SnackbarDuration // 📌 import 추가
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import com.selfbell.core.ui.theme.Primary

const val PIN_LENGTH = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateUp: () -> Unit = {},
    phoneNumber: String,
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var pinValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(pinValue.text) {
        if (pinValue.text.length > PIN_LENGTH) {
            pinValue = pinValue.copy(text = pinValue.text.substring(0, PIN_LENGTH))
        }
        pinValue = pinValue.copy(selection = TextRange(pinValue.text.length))

        if (pinValue.text.length == PIN_LENGTH) {
            focusManager.clearFocus()
            viewModel.login(phoneNumber, pinValue.text)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> {
                coroutineScope.launch {
                    val errorMessage = (uiState as AuthUiState.Error).message
                    snackbarHostState.showSnackbar(errorMessage, duration = SnackbarDuration.Short)
                    pinValue = TextFieldValue("")
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(R.drawable.backstack_icon),
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(70.dp))

                // 닉네임과 전화번호 표시 부분 (디자인에 맞게 추가)
                Text(text = "SafeBell 비밀번호를 입력해주세요.", style = TextStyle(fontSize = 18.sp, fontFamily = Pretendard, fontWeight = FontWeight.Medium))
                Spacer(modifier = Modifier.height(32.dp))

                BasicTextField(
                    value = pinValue,
                    onValueChange = {
                        if (it.text.length <= PIN_LENGTH && it.text.all { char -> char.isDigit() }) {
                            pinValue = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .size(0.dp)
                        .focusRequester(focusRequester),
                    decorationBox = {}
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(PIN_LENGTH) { index ->
                        val char = pinValue.text.getOrNull(index)
                        PinBox(
                            hasChar = char != null,
                            isFocused = index == pinValue.text.length && pinValue.text.length < PIN_LENGTH
                        )
                    }
                }
            }

            if (uiState is AuthUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// 📌 PinBox 컴포저블 추가
@Composable
fun PinBox(
    hasChar: Boolean,
    isFocused: Boolean
) {
    val borderColor = if (isFocused) Primary else GrayInactive
    val backgroundColor = if (hasChar) MaterialTheme.colorScheme.primary else Color.White

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (hasChar) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White, shape = CircleShape)
            )
        }
    }
}