package com.selfbell.core.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    @DrawableRes val icon : Int? = null,
    val label: String
)
