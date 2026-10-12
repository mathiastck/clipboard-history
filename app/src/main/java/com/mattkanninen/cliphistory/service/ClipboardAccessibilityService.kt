package com.mattkanninen.cliphistory.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.mattkanninen.cliphistory.data.repository.ClipRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var repository: ClipRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var clipboardManager: ClipboardManager? = null

    private val primaryClipListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkAndSaveClipboard()
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(primaryClipListener)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessing window focus events triggers primary clip evaluation in background
        checkAndSaveClipboard()
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(primaryClipListener)
        serviceScope.cancel()
    }

    private fun checkAndSaveClipboard() {
        val clipData = clipboardManager?.primaryClip ?: return
        if (clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString() ?: return
            if (text.isNotBlank()) {
                serviceScope.launch {
                    repository.saveClip(content = text)
                }
            }
        }
    }
}
