package com.app.privacyscreendisplay.protectedapps.presentation.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.privacyscreendisplay.protectedapps.domain.model.ProtectedApp

/**
 * Container card rendering the list of protected apps (WhatsApp, Gmail)
 * matching the screenshot design.
 */
@Composable
fun AddedAppItemCard(
    apps: List<ProtectedApp>,
    onRemoveClick: (ProtectedApp) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
    ) {
        apps.forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vector Icon / System Icon Badge
                AppIconBadge(app = app)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                IconButton(
                    onClick = { onRemoveClick(app) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options for ${app.appName}",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            if (index < apps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color(0xFFF1F5F9)
                )
            }
        }
    }
}
@Composable
private fun AppIconBadge(app: ProtectedApp) {
    val context = LocalContext.current
    val appName = app.appName
    val isWhatsApp = appName.equals("WhatsApp", ignoreCase = true)
    val isPhonePe = appName.equals("PhonePe", ignoreCase = true)

    val systemIcon = remember(app.packageName) {
        try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(app.packageName)
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    val bgColor = when {
        systemIcon != null -> Color.Transparent
        isWhatsApp -> Color(0xFF25D366)
        isPhonePe -> Color(0xFF5F259F)
        else -> Color(0xFFF1F5F9)
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            systemIcon != null -> {
                Image(
                    bitmap = systemIcon,
                    contentDescription = appName,
                    modifier = Modifier.size(36.dp)
                )
            }
            isWhatsApp -> {
                // WhatsApp Chat Bubble & Phone Icon Graphic
                Canvas(modifier = Modifier.size(22.dp)) {
                    val path = Path().apply {
                        addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                    }
                    drawPath(path, Color.White, style = Fill)
                }
            }
            isPhonePe -> {
                // PhonePe Hindi Logo 'पे' style Graphic
                Text(
                    text = "पे",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            else -> {
                Canvas(modifier = Modifier.size(22.dp)) {
                    val w = size.width
                    val h = size.height
                    val mPath = Path().apply {
                        moveTo(0f, h * 0.2f)
                        lineTo(w * 0.5f, h * 0.55f)
                        lineTo(w, h * 0.2f)
                        lineTo(w, h * 0.8f)
                        lineTo(w * 0.8f, h * 0.8f)
                        lineTo(w * 0.8f, h * 0.4f)
                        lineTo(w * 0.5f, h * 0.65f)
                        lineTo(w * 0.2f, h * 0.4f)
                        lineTo(w * 0.2f, h * 0.8f)
                        lineTo(0f, h * 0.8f)
                        close()
                    }
                    drawPath(mPath, Color(0xFFEA4335))
                }
            }
        }
    }
}
