package com.app.privacyscreendisplay.activitylog.presentation.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.privacyscreendisplay.activitylog.domain.model.ActivityLogItem
import com.app.privacyscreendisplay.activitylog.presentation.viewmodel.ActivityLogUiState
import com.app.privacyscreendisplay.activitylog.presentation.viewmodel.ActivityLogViewModel
import com.app.privacyscreendisplay.core.ads.InterstitialAdManager
import com.app.privacyscreendisplay.core.ads.RewardedAdManager
import com.app.privacyscreendisplay.home.presentation.ui.components.AdBannerCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ActivityLogScreen(
    viewModel: ActivityLogViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActivityLogContent(
        uiState = uiState,
        onClearLogs = viewModel::clearLogs,
        onDeleteSelectedLogs = viewModel::deleteSelectedLogs,
        onDeleteLogItem = viewModel::deleteLogItem,
        onUnblurItem = viewModel::unblurLogItem,
        onNavigateBack = onNavigateBack,
        onNavigateToPremium = onNavigateToPremium,
        modifier = modifier
    )
}

@Composable
fun ActivityLogContent(
    uiState: ActivityLogUiState,
    onClearLogs: () -> Unit,
    onDeleteSelectedLogs: (Set<String>) -> Unit,
    onDeleteLogItem: (String) -> Unit,
    onUnblurItem: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var singleItemToDelete by remember { mutableStateOf<ActivityLogItem?>(null) }
    var isAdLoading by remember { mutableStateOf(false) }
    var selectedLockedItem by remember { mutableStateOf<ActivityLogItem?>(null) }
    var selectedExpandedItem by remember { mutableStateOf<ActivityLogItem?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar - Switches dynamically between Normal Mode & Multi-Select Mode
            if (isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isSelectionMode = false
                            selectedItemIds = emptySet()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cancel Selection",
                            tint = Color(0xFF991B1B)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${selectedItemIds.size} Selected",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B),
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedItemIds.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete Selected",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Activity Log",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    )

                    if (uiState.logs.isNotEmpty()) {
                        // Delete Button: Tapping enters Multi-Select Delete Mode
                        IconButton(
                            onClick = {
                                isSelectionMode = true
                                selectedItemIds = emptySet()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Select to Delete",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF047857))
                }
            } else {
                // Performant LazyColumn - Only visible items load image thumbnails asynchronously off-thread!
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Summary Metric Cards Header Item
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricSummaryCard(
                                title = "Today",
                                value = "${uiState.detectionsToday}",
                                subtitle = "Detections",
                                bgColor = Color(0xFFECFDF5),
                                valueColor = Color(0xFF047857),
                                modifier = Modifier.weight(1f)
                            )

                            MetricSummaryCard(
                                title = "Most Target",
                                value = uiState.mostProtectedApp,
                                subtitle = "Protected App",
                                bgColor = Color(0xFFF0F9FF),
                                valueColor = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f)
                            )

                            MetricSummaryCard(
                                title = "Total Logs",
                                value = "${uiState.totalActivations}",
                                subtitle = "7-Day History",
                                bgColor = Color(0xFFF5F3FF),
                                valueColor = Color(0xFF7C3AED),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (uiState.logs.isEmpty()) {
                        item {
                            EmptyActivityLogView()
                        }
                    } else {
                        // Render Log Items Grouped by Date Group
                        val grouped = uiState.logs.groupBy { it.dateGroup }

                        grouped.forEach { (dateGroup, items) ->
                            item(key = "header_$dateGroup") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateGroup,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )

                                    if (isSelectionMode) {
                                        val allGroupIds = items.map { it.id }.toSet()
                                        val isAllGroupSelected = selectedItemIds.containsAll(allGroupIds)

                                        TextButton(
                                            onClick = {
                                                selectedItemIds = if (isAllGroupSelected) {
                                                    selectedItemIds - allGroupIds
                                                } else {
                                                    selectedItemIds + allGroupIds
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = if (isAllGroupSelected) "Deselect All" else "Select All",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }

                            items(
                                items = items,
                                key = { it.id }
                            ) { logItem ->
                                LogItemCard(
                                    item = logItem,
                                    isPremiumUser = uiState.isPremiumUser,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedItemIds.contains(logItem.id),
                                    onToggleSelect = {
                                        selectedItemIds = if (selectedItemIds.contains(logItem.id)) {
                                            selectedItemIds - logItem.id
                                        } else {
                                            selectedItemIds + logItem.id
                                        }
                                    },
                                    onLockedItemClick = { selectedLockedItem = logItem },
                                    onExpandedItemClick = { selectedExpandedItem = logItem }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Ad Banner for Free Users
            if (!uiState.isPremiumUser) {
                AdBannerCard(
                    onAdClick = onNavigateToPremium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }

    // Delete Selected Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete ${selectedItemIds.size} Selected Logs?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Selected shoulder surfing activity logs and their intruder photo snapshots will be permanently deleted from your device.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idsToDelete = selectedItemIds
                        showDeleteConfirmDialog = false
                        isSelectionMode = false
                        selectedItemIds = emptySet()

                        if (!uiState.isPremiumUser) {
                            InterstitialAdManager.showAdWithLoading(
                                context = context,
                                onLoadingStateChanged = { isAdLoading = it },
                                onAdDismissed = {
                                    onDeleteSelectedLogs(idsToDelete)
                                }
                            )
                        } else {
                            onDeleteSelectedLogs(idsToDelete)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete (${selectedItemIds.size})", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Ad Loading Overlay (Custom 3-dot blue triangle loader without white card background)
    com.app.privacyscreendisplay.core.ui.components.AdLoadingOverlay(
        isVisible = isAdLoading
    )

    // Single Item Delete Confirmation Dialog
    singleItemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { singleItemToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Activity Log?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Delete the shoulder surfing log captured on ${item.appName} at ${item.formattedTime}?",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = item.id
                        singleItemToDelete = null
                        onDeleteLogItem(targetId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { singleItemToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Intruder Snapshot Locked Modal (Watch Ad or Upgrade)
    selectedLockedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedLockedItem = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Intruder Snapshot Locked",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "A stealth photo was captured when a shoulder surfer was detected looking over your shoulder on ${item.appName}.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val targetId = item.id
                            selectedLockedItem = null
                            RewardedAdManager.showAdWithLoading(
                                context = context,
                                onLoadingStateChanged = { isAdLoading = it },
                                onUserEarnedReward = {
                                    onUnblurItem(targetId)
                                },
                                onAdDismissedOrFailed = {}
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Watch Ad to Unblur Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            selectedLockedItem = null
                            onNavigateToPremium()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF047857))
                    ) {
                        Text(
                            text = "Upgrade to Premium for Auto-Unblur",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedLockedItem = null }) {
                    Text("Close", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Fullscreen Photo Zoom Preview Dialog
    selectedExpandedItem?.let { item ->
        val bitmap = rememberAsyncThumbnail(item.imagePath)

        AlertDialog(
            onDismissRequest = { selectedExpandedItem = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Intruder Snapshot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { selectedExpandedItem = null }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Unblurred Intruder Snapshot",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${item.appName} • ${item.formattedTime}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }
            },
            confirmButton = {}
        )
    }
}

/**
 * Asynchronous Sub-Sampled Thumbnail Loader.
 * Decodes image thumbnails on Dispatchers.IO at 1/4 size so screen open and scroll stay buttery smooth with ZERO lag.
 */
@Composable
private fun rememberAsyncThumbnail(imagePath: String?): ImageBitmap? {
    if (imagePath.isNullOrEmpty()) return null
    val resultState = remember(imagePath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(imagePath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 4 // Sub-sample to 1/4 dimensions for ultra-fast thumbnail decoding
                    }
                    val bmp = BitmapFactory.decodeFile(file.absolutePath, options)
                    resultState.value = bmp?.asImageBitmap()
                }
            } catch (_: Exception) {}
        }
    }
    return resultState.value
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    bgColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LogItemCard(
    item: ActivityLogItem,
    isPremiumUser: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLockedItemClick: () -> Unit,
    onExpandedItemClick: () -> Unit
) {
    val isUnblurred = isPremiumUser || item.isUnblurred
    val thumbnailBitmap = rememberAsyncThumbnail(item.imagePath)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFFEF2F2) else Color(0xFFFAFAFA))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFEF4444) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                if (isSelectionMode) {
                    onToggleSelect()
                } else if (isUnblurred) {
                    onExpandedItemClick()
                } else {
                    onLockedItemClick()
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Square Checkbox (Visible when in Multi-Select Delete Mode)
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFEF4444),
                    uncheckedColor = Color(0xFF94A3B8)
                ),
                modifier = Modifier.padding(end = 6.dp)
            )
        }

        // Intruder Snapshot Thumbnail Container (Blurred or Unblurred)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap,
                    contentDescription = "Intruder Snapshot",
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (!isUnblurred) Modifier.blur(16.dp) else Modifier
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Lock Overlay Icon if Locked for Free User
            if (!isUnblurred) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Locked Snapshot",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.appName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.actionText,
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "• ${item.formattedTime}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (!isSelectionMode) {
            // Badge: View / Locked Status
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isUnblurred) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUnblurred) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = if (isUnblurred) Color(0xFF047857) else Color(0xFFD97706),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isUnblurred) "View" else "Locked",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnblurred) Color(0xFF047857) else Color(0xFFD97706)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityLogView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = Color(0xFF047857),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Activity Logs Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Detection events and privacy protection triggers\nwill appear here when shoulder surfers are detected.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
