// AuthFlow.kt (수정된 전체 코드)

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.ui.PasswordScreen
import com.selfbell.auth.ui.AuthUiState
import com.selfbell.auth.ui.AuthViewModel

@Composable
fun AuthFlow(
    onAuthComplete: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // 뷰모델의 상태를 관찰합니다.
    val uiState by viewModel.uiState.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()

    // 상태에 따른 화면 전환 및 이벤트 처리
    when (uiState) {
        AuthUiState.Success -> {
            // 성공 상태일 때만 화면 전환
            LaunchedEffect(Unit) {
                onAuthComplete()
            }
        }
        AuthUiState.Loading -> {
            // 로딩 중에는 다른 UI 위에 프로그레스 인디케이터 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthUiState.Error -> {
            // 에러 상태일 때 에러 메시지를 표시
            // (예: 스낵바 또는 다이얼로그)
            // 에러 메시지: (uiState as AuthUiState.Error).message
            PasswordScreen(
                phoneNumber = phoneNumber,
                onConfirmClick = { password ->
                    viewModel.signUp(password)
                },
                // 에러 상태일 때 버튼 비활성화 (선택 사항)
                //enabled = false
            )
        }
        AuthUiState.Idle -> {
            // 초기 상태일 때 PasswordScreen 표시
            PasswordScreen(
                phoneNumber = phoneNumber,
                onConfirmClick = { password ->
                    viewModel.signUp(password)
                }
            )
        }
    }
}