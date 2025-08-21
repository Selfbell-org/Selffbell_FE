// feature/escort/ui/ArrivalTimerSection.kt
package com.selfbell.escort.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.SelfBellTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.selfbell.escort.ui.EscortViewModel.ArrivalMode

// EscortViewModel에 필요한 데이터 클래스와 Enum (이 파일에 추가)


@Composable
fun TimePickerWheel(
    selectedTime: Int,
    onTimeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val times = (15..360 step 15).toList()
    val state = rememberLazyListState() // 스크롤 상태를 기억

    // selectedTime이 변경될 때마다 스크롤을 가운데로 이동
    LaunchedEffect(selectedTime) {
        val index = times.indexOf(selectedTime)
        if (index != -1) {
            // 스크롤 위치를 가운데로 조정
            state.animateScrollToItem(index, scrollOffset = -20)
        }
    }

    val formattedTime: (Int) -> String = { minutes ->
        val hours = minutes / 60
        val mins = minutes % 60
        when {
            hours > 0 && mins > 0 -> "${hours}시간 ${mins}분"
            hours > 0 -> "${hours}시간"
            else -> "${mins}분"
        }
    }

    Box(
        modifier = modifier
            .height(80.dp) // 휠의 높이 설정 (스크롤 영역이 보여야 함)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state, // LazyColumn에 상태 전달
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 0.dp)
        ) {
            items(times) { time ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { onTimeChange(time) }
                        .background(if (selectedTime == time) Primary else Color.Transparent, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedTime(time),
                        color = if (selectedTime == time) Color.White else Black,
                        style = Typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

// Material3 기반의 TimePicker 대화 상자 컴포저블
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("취소")
            }
        },
        title = {
            Text("도착 예정 시간 선택", style = Typography.titleMedium)
        },
        text = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledTimePicker(
    modifier: Modifier = Modifier,
    selectedTime: LocalTime?,
    onTimeChange: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("HH시 mm분")

    Box(
        modifier = modifier
            .height(80.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .clickable { showTimePicker = true },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedTime?.format(formatter) ?: "시간을 선택해주세요",
            style = Typography.titleMedium,
            textAlign = TextAlign.Center,
            color = if (selectedTime != null) Black else Color.Gray
        )
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.hour ?: LocalTime.now().hour,
            initialMinute = selectedTime?.minute ?: LocalTime.now().minute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirmClick = {
                val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onTimeChange(newTime)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun ArrivalTimerSection(
    arrivalMode: ArrivalMode,
    timerMinutes: Int,
    expectedArrivalTime: LocalTime?,
    onModeChange: (ArrivalMode) -> Unit,
    onTimerChange: (Int) -> Unit,
    onExpectedArrivalTimeChange: (LocalTime) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "도착 시간", style = Typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onModeChange(ArrivalMode.TIMER) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (arrivalMode == ArrivalMode.TIMER) Primary else Color.LightGray
                )
            ) {
                Text(text = "타이머", color = if (arrivalMode == ArrivalMode.TIMER) Color.White else Black)
            }
            Button(
                onClick = { onModeChange(ArrivalMode.SCHEDULED_TIME) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (arrivalMode == ArrivalMode.SCHEDULED_TIME) Primary else Color.LightGray
                )
            ) {
                Text(text = "도착 예정 시간", color = if (arrivalMode == ArrivalMode.SCHEDULED_TIME) Color.White else Black)
            }
        }

        when (arrivalMode) {
            ArrivalMode.TIMER -> {
                Spacer(modifier = Modifier.height(16.dp))
                TimePickerWheel(
                    selectedTime = timerMinutes,
                    onTimeChange = onTimerChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            ArrivalMode.SCHEDULED_TIME -> {
                Spacer(modifier = Modifier.height(16.dp))
                ScheduledTimePicker(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTime = expectedArrivalTime,
                    onTimeChange = onExpectedArrivalTimeChange
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArrivalTimerSectionPreview() {
    SelfBellTheme {
        var arrivalMode by remember { mutableStateOf(ArrivalMode.TIMER) }
        var timerMinutes by remember { mutableStateOf(30) }
        var expectedArrivalTime by remember { mutableStateOf<LocalTime?>(null) }

        ArrivalTimerSection(
            arrivalMode = arrivalMode,
            timerMinutes = timerMinutes,
            expectedArrivalTime = expectedArrivalTime,
            onModeChange = { arrivalMode = it },
            onTimerChange = { timerMinutes = it },
            onExpectedArrivalTimeChange = { expectedArrivalTime = it }
        )
    }
}