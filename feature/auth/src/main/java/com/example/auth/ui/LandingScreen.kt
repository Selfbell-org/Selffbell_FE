package com.example.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.R
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.White

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onSigninClick: () -> Unit,
    modifier: Modifier = Modifier) {

    Column(
        modifier = modifier.fillMaxSize()
            .background(Color.White)
            .padding(vertical = 70.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.logo_img_blue),
                contentDescription = "SelfBell Logo Blue"
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "SelfBell",
                color = Primary,
                fontFamily = FontFamily(Font(com.selfbell.core.R.font.gabarito_variablefont_wght)),
                fontSize = 40.sp,
                fontWeight = FontWeight(700)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier.border(width = 0.8.dp,
                    color = Primary,
                    shape = RoundedCornerShape(size = 16.dp))
                    .width(343.dp)
                    .height(48.dp)
                    .background(color = Primary, shape = RoundedCornerShape(size = 16.dp))
                    .padding(start = 27.dp, top = 12.dp, end = 27.dp, bottom = 12.dp)
            ){
                Text(
                    text = "로그인 하기",
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = FontFamily(Font(com.selfbell.core.R.font.gabarito_variablefont_wght)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(700),
                    color = White
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                Modifier.border(width = 0.8.dp,
                    color = Color(0xFF797479),
                    shape = RoundedCornerShape(size = 16.dp))
                    .width(343.dp)
                    .height(48.dp)
                    .background(color = White, shape = RoundedCornerShape(size = 16.dp))
                    .padding(start = 27.dp, top = 12.dp, end = 27.dp, bottom = 12.dp)
            ){
                Text(
                    text = "회원가입 하기",
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = FontFamily(Font(com.selfbell.core.R.font.gabarito_variablefont_wght)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(700),
                    color = Black
                )
            }
        }
        Text(
            text = "Developed By Team SelfBell",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(com.selfbell.core.R.font.gabarito_variablefont_wght)),
                fontWeight = FontWeight(600),
                color = Color(0xFF858A90)
            )
        )
    }
}

@Preview
@Composable
fun LandingScreenPreview(){
    LandingScreen(onLoginClick = {}, onSigninClick = {})
}