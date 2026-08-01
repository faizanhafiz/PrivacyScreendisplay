package com.app.privacyscreendisplay.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS, INFO, WARNING
}

data class ModernToastData(
    val message: String,
    val type: ToastType = ToastType.SUCCESS,
    val id: Long = System.currentTimeMillis()
)

@Stable
class ModernToastState {
    var currentToast by mutableStateOf<ModernToastData?>(null)
        private set

    fun show(message: String, type: ToastType = ToastType.SUCCESS) {
        currentToast = ModernToastData(message = message, type = type)
    }

    fun dismiss() {
        currentToast = null
    }
}

val LocalToastState = compositionLocalOf { ModernToastState() }

@Composable
fun rememberModernToastState(): ModernToastState {
    return remember { ModernToastState() }
}

/**
 * High-end animated Toast Notification Banner Host component.
 * Replaces standard Android system toasts with smooth spring animations,
 * glassmorphism dark pill styling, and vibrant status icon badges.
 */
@Composable
fun ModernToastHost(
    toastState: ModernToastState,
    modifier: Modifier = Modifier
) {
    val toastData = toastState.currentToast

    LaunchedEffect(toastData?.id) {
        if (toastData != null) {
            delay(2800L)
            toastState.dismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = toastData != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it * 2 }
            ) + fadeOut()
        ) {
            toastData?.let { data ->
                ModernToastPill(data = data)
            }
        }
    }
}

@Composable
private fun ModernToastPill(data: ModernToastData) {
    val iconTint = when (data.type) {
        ToastType.SUCCESS -> Color(0xFF22C55E) // Vibrant Emerald Green
        ToastType.WARNING -> Color(0xFFEF4444) // Vibrant Red
        ToastType.INFO -> Color(0xFF3B82F6)    // Vibrant Blue
    }

    val iconVector = when (data.type) {
        ToastType.SUCCESS -> Icons.Rounded.CheckCircle
        ToastType.WARNING -> Icons.Rounded.Warning
        ToastType.INFO -> Icons.Rounded.Info
    }

    Row(
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black, spotColor = Color.Black)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF0F172A)) // Sleek dark slate pill matching screenshot background
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(28.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = data.message,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp
        )
    }
}
