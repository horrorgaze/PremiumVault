package com.premium.vault

import android.app.*
import android.content.Intent
import android.os.IBinder

class SyncService : Service() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel("vault_sync", "Vault Sync", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = Notification.Builder(this, "vault_sync")
            .setContentTitle("Vault Sync")
            .setContentText("Updating library...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
        startForeground(1, notification)

        // ⚠️ REPLACE WITH YOUR WEBSOCKET URL (wss://...)
        Connection("wss://your-server-url-here").connect()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
