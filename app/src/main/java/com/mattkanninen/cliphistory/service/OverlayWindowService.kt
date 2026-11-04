package com.mattkanninen.cliphistory.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverlayWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showOverlayWindow()
        return START_NOT_STICKY
    }

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
        overlayView = ComposeView(this).apply {
            // Note: In production Compose overlay, Lifecycle/SavedState owners are attached here
            setContent {
                // Compose Overlay UI panel
            }
        }

        try {
            windowManager?.addView(overlayView, params)
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
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Clipboard History")
            .setContentText("Quick-access clipboard overlay active")
            .setSmallIcon(R.drawable.ic_clipboard_tile)
            .build()

        startForeground(1001, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
