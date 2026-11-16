package com.mattkanninen.cliphistory.ui.main

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mattkanninen.cliphistory.service.OverlayWindowService
import com.mattkanninen.cliphistory.ui.components.ClipHistoryScreen
import com.mattkanninen.cliphistory.ui.theme.ClipboardHistoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ClipHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //android.util.Log.i("mkdebug", "MainActivity.onCreate")
        startOverlayService()
        setContent {
            ClipboardHistoryTheme {
                val uiState by viewModel.uiState.collectAsState()

                ClipHistoryScreen(
                    uiState = uiState,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onCategorySelected = viewModel::onCategorySelected,
                    onTogglePin = viewModel::onTogglePin,
                    onDeleteClip = viewModel::onDeleteClip,
                    onAddClip = viewModel::onAddClip,
                    onSearchActivated = viewModel::onSearchActivated,
                    onSearchDeactivated = viewModel::onSearchDeactivated,
                    onToggleBubbles = viewModel::onToggleBubbles
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //android.util.Log.i("mkdebug", "MainActivity.onResume")
        viewModel.onResumeClipboard(readSystemClipboardText())
    }

    private fun readSystemClipboardText(): String? {
        return try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = clipboard?.primaryClip ?: return null
            if (clip.itemCount == 0) return null
            clip.getItemAt(0).coerceToText(this)?.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayWindowService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
