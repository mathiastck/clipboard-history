package com.mattkanninen.cliphistory.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mattkanninen.cliphistory.ui.components.ClipHistoryScreen
import com.mattkanninen.cliphistory.ui.theme.PixelClipHistoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ClipHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixelClipHistoryTheme {
                val uiState by viewModel.uiState.collectAsState()

                ClipHistoryScreen(
                    uiState = uiState,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onCategorySelected = viewModel::onCategorySelected,
                    onTogglePin = viewModel::onTogglePin,
                    onDeleteClip = viewModel::onDeleteClip,
                    onClearHistory = viewModel::onClearHistory
                )
            }
        }
    }
}
