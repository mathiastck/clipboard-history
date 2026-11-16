package com.mattkanninen.cliphistory.service

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.TileService
import com.mattkanninen.cliphistory.ui.main.MainActivity

class ClipboardTileService : TileService() {

    override fun onCreate() {
        super.onCreate()
        //android.util.Log.i("mkdebug", "ClipboardTileService.onCreate")
    }

    override fun onClick() {
        super.onClick()
        //android.util.Log.i("mkdebug", "ClipboardTileService.onClick")
        val openApp = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Works on the lock screen too; MainActivity starts the overlay service.
        unlockAndRun { startActivityAndCollapse(pendingOpen) }
    }
}
