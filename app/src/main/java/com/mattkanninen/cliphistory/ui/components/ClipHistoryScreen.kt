package com.mattkanninen.cliphistory.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mattkanninen.cliphistory.data.ClipboardEntry
import com.mattkanninen.cliphistory.service.OverlayWindowService
import com.mattkanninen.cliphistory.ui.main.ClipHistoryUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipHistoryScreen(
    uiState: ClipHistoryUiState,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onTogglePin: (ClipboardEntry) -> Unit,
    onDeleteClip: (ClipboardEntry) -> Unit,
    onAddClip: (String) -> Unit,
    onSearchActivated: () -> Unit,
    onSearchDeactivated: () -> Unit,
    onToggleBubbles: () -> Unit
) {
    val context = LocalContext.current

    //TODO: move these lists to a data class in or near view model
    val focusManager = LocalFocusManager.current
    val categories = if (uiState.isSearchActive) {
        listOf("All", "Text", "URL", "Code")
    } else {
        listOf("All", "Unpinned", "Pinned")
    }
    var showMenu by remember { mutableStateOf(false) }
    var addText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var lastSeenTopId by remember { mutableStateOf<Long?>(null) }

    val topId = uiState.clips.firstOrNull()?.id
    // TODO: if autoscroll annoys (loses scroll position), replace with an
    // up-arrow badge signaling new data instead of jumping to it.
    LaunchedEffect(topId) {
        if (lastSeenTopId != null && topId != null && topId != lastSeenTopId
            && !uiState.isSearchActive
        ) {
            listState.animateScrollToItem(0)
        }
        lastSeenTopId = topId
    }

    BackHandler(enabled = uiState.isSearchActive) {
        focusManager.clearFocus()
        onSearchDeactivated()
    }

    fun submitAdd() {
        if (addText.isNotBlank()) {
            onAddClip(addText)
            addText = ""
            focusManager.clearFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clipboard History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                actions = {
                    if (!uiState.isSearchActive) {
                        IconButton(onClick = onSearchActivated) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                //TODO externalize strings
                                contentDescription = "Search"
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // TODO: clear-all lives here one day (with confirm). No rush.
                            DropdownMenuItem(
                                text = { Text("Clear all (coming soon)") },
                                enabled = false,
                                onClick = { showMenu = false }
                            )
                            // TODO: biometric lock option in this menu.
                            DropdownMenuItem(
                                text = { Text("Biometric lock (coming soon)") },
                                enabled = false,
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Bubbles") },
                                trailingIcon = {
                                    if (uiState.bubblesEnabled) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Bubbles On"
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onToggleBubbles()
                                    context.startService(
                                        Intent(
                                            context,
                                            OverlayWindowService::class.java
                                        ).setAction(OverlayWindowService.ACTION_UPDATE_NOTIFICATION)
                                    )
                                }
                            )
                            // TODO: edge panel option in this menu (SAW overlay: position, auto-collapse, per-app exclusion).
                            DropdownMenuItem(
                                text = { Text("Edge panel (coming soon)") },
                                enabled = false,
                                onClick = { showMenu = false }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Entry field: manual add by default, search on demand
            if (uiState.isSearchActive) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search clipboard history...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            onSearchDeactivated()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Exit Search")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            } else {
                OutlinedTextField(
                    value = addText,
                    onValueChange = { addText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add a clip...") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    trailingIcon = {
                        if (addText.isNotBlank()) {
                            IconButton(onClick = { submitAdd() }) {
                                Icon(Icons.Default.Send, contentDescription = "Save Clip")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submitAdd() })
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clip Items List
            if (uiState.clips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No clips in history",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.clips, key = { it.id }) { clip ->
                        ClipItemCard(
                            clip = clip,
                            onCopyClick = {
                                copyToClipboard(context, clip.content)
                            },
                            onTogglePin = { onTogglePin(clip) },
                            onDelete = { onDeleteClip(clip) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClipItemCard(
    clip: ClipboardEntry,
    onCopyClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopyClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (clip.isPinned) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val categoryText = when {
                    clip.content.startsWith("http://") || clip.content.startsWith("https://") -> "URL"
                    clip.content.contains("{") || clip.content.contains("val ") || clip.content.contains("fun ") -> "Code"
                    else -> "Text"
                }

                Text(
                    text = categoryText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Row {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (clip.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Clip",
                            tint = if (clip.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Clip",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = clip.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dateFormat.format(Date(clip.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Clip", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
}
