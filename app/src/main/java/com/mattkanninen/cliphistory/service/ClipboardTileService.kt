package com.mattkanninen.cliphistory.service

import android.content.Intent
import android.service.quicksettings.TileService

class ClipboardTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, OverlayWindowService::class.java)
        startForegroundService(intent)
    }
}
