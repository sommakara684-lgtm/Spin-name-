package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.model.WheelItemEntity
import com.example.model.WheelPaletteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageItemsSheet(
    items: List<WheelItemEntity>,
    paletteTheme: WheelPaletteTheme,
    onAddItem: (String) -> Unit,
    onBulkAdd: (String) -> Unit,
    onToggleItem: (WheelItemEntity) -> Unit,
    onEditItem: (WheelItemEntity) -> Unit,
    onDeleteItem: (WheelItemEntity) -> Unit,
    onShuffle: () -> Unit,
    onSortAZ: () -> Unit,
    onToggleAll: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newNameText by remember { mutableStateOf("") }
    var showBulkDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<WheelItemEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("manage_items_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Names & Choices",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${items.count { it.isEnabled }} active of ${items.size} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { showBulkDialog = true },
                    label = { Text("Bulk Paste") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("bulk_paste_button")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Add Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newNameText,
                    onValueChange = { newNameText = it },
                    placeholder = { Text("Type name and press enter…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newNameText.isNotBlank()) {
                                onAddItem(newNameText.trim())
                                newNameText = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        if (newNameText.isNotBlank()) {
                            onAddItem(newNameText.trim())
                            newNameText = ""
                        }
                    },
                    enabled = newNameText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("quick_add_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add name")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Quick Chips (Shuffle, Sort A-Z, Enable All, Clear)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = onShuffle,
                    label = { Text("Shuffle") },
                    icon = { Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("shuffle_items_button")
                )
                SuggestionChip(
                    onClick = onSortAZ,
                    label = { Text("A-Z") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("sort_az_button")
                )
                val allEnabled = items.isNotEmpty() && items.all { it.isEnabled }
                SuggestionChip(
                    onClick = { onToggleAll(!allEnabled) },
                    label = { Text(if (allEnabled) "Disable All" else "Enable All") },
                    modifier = Modifier.testTag("toggle_all_button")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Wheel is empty! Add names above or use Bulk Paste.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        val itemIndex = items.indexOf(item)
                        val color = if (item.colorHex != null) {
                            Color(item.colorHex)
                        } else {
                            paletteTheme.getColorForIndex(itemIndex)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_row_${item.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isEnabled) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = item.isEnabled,
                                        onCheckedChange = { onToggleItem(item) },
                                        modifier = Modifier.testTag("checkbox_${item.id}")
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (item.isEnabled) color else Color.Gray.copy(alpha = 0.4f))
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = if (item.isEnabled) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        }
                                    )

                                    if (item.weight > 1) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${item.weight}x",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { itemToEdit = item },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("edit_item_btn_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit name",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteItem(item) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_item_btn_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete name",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBulkDialog) {
        BulkAddDialog(
            onDismiss = { showBulkDialog = false },
            onConfirm = { text ->
                onBulkAdd(text)
                showBulkDialog = false
            }
        )
    }

    itemToEdit?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { updated ->
                onEditItem(updated)
                itemToEdit = null
            }
        )
    }
}
