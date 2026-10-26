package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WheelPaletteTheme
import com.example.ui.components.AccountSheet
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.HistoryStatsSheet
import com.example.ui.components.ManageItemsSheet
import com.example.ui.components.WheelListSheet
import com.example.ui.components.WheelSettingsSheet
import com.example.ui.components.WinnerDialog
import com.example.ui.wheel.WheelCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val wheels by viewModel.wheels.collectAsStateWithLifecycle()
    val currentWheel by viewModel.currentWheel.collectAsStateWithLifecycle()
    val currentItems by viewModel.currentItems.collectAsStateWithLifecycle()
    val historyList by viewModel.currentHistory.collectAsStateWithLifecycle()
    val statsList by viewModel.currentStats.collectAsStateWithLifecycle()

    val isSpinning by viewModel.isSpinning.collectAsStateWithLifecycle()
    val winnerItem by viewModel.winnerItem.collectAsStateWithLifecycle()
    val showWinnerDialog by viewModel.showWinnerDialog.collectAsStateWithLifecycle()
    val showConfetti by viewModel.showConfetti.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val cloudSyncMessage by viewModel.cloudSyncMessage.collectAsStateWithLifecycle()

    // Sheet visibility states
    var showWheelListSheet by remember { mutableStateOf(false) }
    var showManageItemsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }

    val wheel = currentWheel
    val palette = WheelPaletteTheme.fromName(wheel?.themePalette)
    val activeCount = currentItems.count { it.isEnabled }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { showAccountSheet = true },
                        modifier = Modifier.testTag("account_sync_button")
                    ) {
                        if (currentUser != null) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Cloud Connected",
                                tint = Color(0xFF10B981)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Cloud Sync",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                title = {
                    Surface(
                        onClick = { showWheelListSheet = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.testTag("switch_wheel_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = wheel?.title ?: "Spin Wheel",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 180.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Wheel",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showManageItemsSheet = true },
                        modifier = Modifier.testTag("open_manage_items_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "Manage Names"
                        )
                    }

                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Results & Stats"
                        )
                    }

                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Wheel Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val isExpanded = maxWidth > 600.dp

                if (isExpanded) {
                    // Landscape / Tablet layout: 2-column view
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 500.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WheelCanvas(
                                items = currentItems,
                                paletteTheme = palette,
                                spinDurationSeconds = wheel?.spinDurationSeconds ?: 4.5f,
                                isSpinning = isSpinning,
                                onSpinStart = { viewModel.onSpinStarted() },
                                onSpinTick = { viewModel.onSpinTick() },
                                onSpinComplete = { winner -> viewModel.onSpinCompleted(winner) }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ActiveNamesSummary(
                                activeCount = activeCount,
                                totalCount = currentItems.size,
                                onClick = { showManageItemsSheet = true }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            SpinActionButtons(
                                isSpinning = isSpinning,
                                hasItems = activeCount > 0,
                                onSpin = { viewModel.onSpinStarted() },
                                onShuffle = { viewModel.shuffleItems() },
                                onAdd = { showManageItemsSheet = true }
                            )
                        }
                    }
                } else {
                    // Mobile Portrait Layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top info badge
                        ActiveNamesSummary(
                            activeCount = activeCount,
                            totalCount = currentItems.size,
                            onClick = { showManageItemsSheet = true }
                        )

                        // Center: Interactive Wheel Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            WheelCanvas(
                                items = currentItems,
                                paletteTheme = palette,
                                spinDurationSeconds = wheel?.spinDurationSeconds ?: 4.5f,
                                isSpinning = isSpinning,
                                onSpinStart = { viewModel.onSpinStarted() },
                                onSpinTick = { viewModel.onSpinTick() },
                                onSpinComplete = { winner -> viewModel.onSpinCompleted(winner) }
                            )
                        }

                        // Bottom Action Controls
                        SpinActionButtons(
                            isSpinning = isSpinning,
                            hasItems = activeCount > 0,
                            onSpin = { viewModel.onSpinStarted() },
                            onShuffle = { viewModel.shuffleItems() },
                            onAdd = { showManageItemsSheet = true }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Confetti Particle Explosion
            AnimatedVisibility(
                visible = showConfetti,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                ConfettiEffect(modifier = Modifier.fillMaxSize())
            }
        }
    }

    // Winner Celebration Dialog
    if (showWinnerDialog && winnerItem != null) {
        WinnerDialog(
            winnerItem = winnerItem!!,
            wheelTitle = wheel?.title ?: "",
            onDismiss = { viewModel.dismissWinnerDialog() },
            onSpinAgain = {
                viewModel.dismissWinnerDialog()
                viewModel.onSpinStarted()
            },
            onRemoveWinner = { item ->
                viewModel.toggleItem(item)
            }
        )
    }

    // Wheel Selector Sheet
    if (showWheelListSheet) {
        WheelListSheet(
            wheels = wheels,
            selectedWheelId = wheel?.id ?: 0L,
            onSelectWheel = { selected -> viewModel.selectWheel(selected) },
            onCreateWheel = { title, desc -> viewModel.createWheel(title, desc) },
            onDeleteWheel = { toDelete -> viewModel.deleteWheel(toDelete) },
            onDismiss = { showWheelListSheet = false }
        )
    }

    // Manage Names Sheet
    if (showManageItemsSheet) {
        ManageItemsSheet(
            items = currentItems,
            paletteTheme = palette,
            onAddItem = { name -> viewModel.addItem(name) },
            onBulkAdd = { rawText -> viewModel.addBulkItems(rawText) },
            onToggleItem = { item -> viewModel.toggleItem(item) },
            onEditItem = { item -> viewModel.updateItem(item) },
            onDeleteItem = { item -> viewModel.deleteItem(item) },
            onShuffle = { viewModel.shuffleItems() },
            onSortAZ = { viewModel.sortItemsAZ() },
            onToggleAll = { enable -> viewModel.toggleAllItems(enable) },
            onClearAll = { viewModel.clearAllItems() },
            onDismiss = { showManageItemsSheet = false }
        )
    }

    // Wheel Settings Sheet
    if (showSettingsSheet && wheel != null) {
        WheelSettingsSheet(
            wheel = wheel,
            onDismiss = { showSettingsSheet = false },
            onSave = { updated -> viewModel.updateWheel(updated) }
        )
    }

    // History and Stats Sheet
    if (showHistorySheet) {
        HistoryStatsSheet(
            historyList = historyList,
            statsList = statsList,
            wheelTitle = wheel?.title ?: "",
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { showHistorySheet = false }
        )
    }

    // Account & Cloud Sync Sheet
    if (showAccountSheet) {
        val context = LocalContext.current
        AccountSheet(
            currentUser = currentUser,
            isSyncing = isCloudSyncing,
            syncMessage = cloudSyncMessage,
            onSyncNow = {
                currentUser?.let { viewModel.syncWheelsToCloud(it.uid) }
            },
            onSignOut = {
                viewModel.signOut(context)
            },
            onDismiss = { showAccountSheet = false }
        )
    }
}

@Composable
private fun ActiveNamesSummary(
    activeCount: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("names_summary_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (activeCount > 0) Color(0xFF10B981) else Color(0xFFEF4444))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$activeCount active ($totalCount total)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• Tap to Edit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SpinActionButtons(
    isSpinning: Boolean,
    hasItems: Boolean,
    onSpin: () -> Unit,
    onShuffle: () -> Unit,
    onAdd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Shuffle
            FilledTonalButton(
                onClick = onShuffle,
                enabled = !isSpinning && hasItems,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("quick_shuffle_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Shuffle")
            }

            // Quick Add
            OutlinedButton(
                onClick = onAdd,
                enabled = !isSpinning,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("quick_add_modal_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Name")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Primary SPIN Button
        Button(
            onClick = onSpin,
            enabled = !isSpinning && hasItems,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("primary_spin_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isSpinning) "SPINNING…" else if (!hasItems) "ADD NAMES TO SPIN" else "SPIN THE WHEEL",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
