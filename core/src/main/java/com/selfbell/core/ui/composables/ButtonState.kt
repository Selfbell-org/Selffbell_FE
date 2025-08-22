// core/ui/composables/ButtonState.kt
package com.selfbell.core.ui.composables

// 버튼의 상태를 정의하는 enum 클래스
enum class ButtonState {
    SELECTED, // 해제 (빨간색)
    INVITED,  // 초대 (초록색)
    DEFAULT   // 선택 (기본색)
}