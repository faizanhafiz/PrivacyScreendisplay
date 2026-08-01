package com.app.privacyscreendisplay.protectedapps.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top Free Plan Quota status card matching exact screenshot layout:
 * Title "Free Plan", count subtitle "2 of 2 apps added", and full green progress bar.
 */
@Composable
fun PlanQuotaCard(
    currentCount: Int,
    maxAllowed: Int,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (maxAllowed > 0) (currentCount.toFloat() / maxAllowed.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF2F9F5))
            .border(1.dp, Color(0xFFE2F3E9), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Free Plan",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$currentCount of $maxAllowed apps added",
            fontSize = 13.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF008746),
            trackColor = Color(0xFFE5F2EB),
            strokeCap = StrokeCap.Round
        )
    }
}

