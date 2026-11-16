package com.mattkanninen.cliphistory.service

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.mattkanninen.cliphistory.data.repository.ClipRepository
import com.mattkanninen.cliphistory.ui.main.MainActivity
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
        //android.util.Log.i("mkdebug", "ClipboardAccessibilityService.onCreate")
        ensureClipboardListener()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        //android.util.Log.i("mkdebug", "ClipboardAccessibilityService.onServiceConnected")
        ensureClipboardListener()
        accessibilityButtonController?.registerAccessibilityButtonCallback(
            object : AccessibilityButtonController.AccessibilityButtonCallback() {
                override fun onClicked(controller: AccessibilityButtonController) {
                    val openApp =
                        Intent(this@ClipboardAccessibilityService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                    startActivity(openApp)
                }
            }
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessing window focus events triggers primary clip evaluation in background
        checkAndSaveClipboard()
    }

    override fun onInterrupt() {
        //android.util.Log.i("mkdebug", "ClipboardAccessibilityService.onInterrupt")
    }

    override fun onDestroy() {
        //android.util.Log.i("mkdebug", "ClipboardAccessibilityService.onDestroy")
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(primaryClipListener)
        serviceScope.cancel()
    }

    private fun ensureClipboardListener() {
        if (clipboardManager == null) {
            clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        }
        clipboardManager?.removePrimaryClipChangedListener(primaryClipListener)
        clipboardManager?.addPrimaryClipChangedListener(primaryClipListener)
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
