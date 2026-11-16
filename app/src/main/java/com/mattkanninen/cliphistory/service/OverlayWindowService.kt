package com.mattkanninen.cliphistory.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.core.graphics.drawable.IconCompat
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mattkanninen.cliphistory.R
import com.mattkanninen.cliphistory.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverlayWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        //REMINDER don't ship this logging to github.  mkdebug should not appear in prod logs
        //android.util.Log.i("mkdebug", "onCreate")
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //android.util.Log.i("mkdebug", "OverlayWindowService.onStartCommand")
        showOverlayWindow()
        if (intent?.action == ACTION_UPDATE_NOTIFICATION) {
            startForegroundServiceNotification()
        }
        return START_STICKY
    }

    // TODO: smarter bubbles interaction — drag-to-move, tap-to-expand panel,
    // auto-collapse, and per-app exclusion.
    private fun showOverlayWindow() {
        if (overlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.TOP
        }

        // Overlay View setup
        val view = ComposeView(this).apply {
            // Note: In production Compose overlay, Lifecycle/SavedState owners are attached here
            setContent {
                // Compose Overlay UI panel
            }
        }

        try {
            windowManager?.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "overlay_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Clipboard Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                channel.setAllowBubbles(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // TODO: bubbles hard-off until the system rejection of BubbleMetadata is
        // diagnosed — posting bubble metadata crashes startForeground on this device.
        val bubblesEnabled = false

        val openApp = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val contentIntent = PendingIntent.getActivity(
            this, 0, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val builder = baseNotificationBuilder(channelId, contentIntent)
            if (bubblesEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && publishBubbleShortcut(openApp)
            ) {
                builder.setShortcutId(BUBBLE_SHORTCUT_ID)
                val bubbleIntent = PendingIntent.getActivity(
                    this, 1, openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val bubble = NotificationCompat.BubbleMetadata.Builder(
                    bubbleIntent,
                    IconCompat.createWithResource(this, R.drawable.ic_clipboard_tile)
                )
                    .setDesiredHeight(600)
                    .build()
                builder.setBubbleMetadata(bubble)
            }
            startForeground(1001, builder.build())
        } catch (e: Exception) {
            // Bubble metadata rejected (shortcut/rate-limit/OEM quirk) — never crash,
            // fall back to the plain notification.
            e.printStackTrace()
            startForeground(1001, baseNotificationBuilder(channelId, contentIntent).build())
        }
    }

    private fun baseNotificationBuilder(
        channelId: String,
        contentIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelId)
        //TODO externalize strings
            .setContentTitle("Clipboard History")
            .setContentText("Tap to open Clipboard History")
            .setSmallIcon(R.drawable.ic_clipboard_tile)
            .setContentIntent(contentIntent)
            .setOngoing(true)
    }

    private fun publishBubbleShortcut(openApp: Intent): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return false
        if (shortcutManager.dynamicShortcuts.any { it.id == BUBBLE_SHORTCUT_ID }) return true
        val shortcutIntent = Intent(openApp).setAction(Intent.ACTION_MAIN)
        val shortcut = ShortcutInfo.Builder(this, BUBBLE_SHORTCUT_ID)
            .setShortLabel(getString(R.string.app_name))
            .setIcon(Icon.createWithResource(this, R.drawable.ic_clipboard_tile))
            .setIntent(shortcutIntent)
            .setLongLived(true)
            .build()
        return try {
            shortcutManager.pushDynamicShortcut(shortcut)
            true
        } catch (e: Exception) {
            // Rate-limited or rejected — caller posts without bubble metadata.
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val ACTION_UPDATE_NOTIFICATION =
            "com.mattkanninen.cliphistory.action.UPDATE_NOTIFICATION"
        const val PREFS_NAME = "clip_history_prefs"
        const val KEY_BUBBLES_ENABLED = "bubbles_enabled"
        const val BUBBLE_SHORTCUT_ID = "clipboard_bubble"
    }

    override fun onDestroy() {
        //android.util.Log.i("mkdebug", "OverlayWindowService.onDestroy")
        super.onDestroy()
        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
            }
        } catch (e: IllegalArgumentException) {
            // View was never attached (e.g. overlay permission missing) — nothing to remove.
            e.printStackTrace()
        } finally {
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        //android.util.Log.i("mkdebug", "OverlayWindowService.onBind")
        return null
    }
}
