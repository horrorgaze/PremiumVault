package com.premium.vault

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class SyncService : Service() {

    private var connection: Connection? = null

    companion object {
        const val CHANNEL_ID = "vault_sync"
        const val NOTIF_ID = 1
        const val ACTION_STOP = "com.premium.vault.STOP"

        // Leave the placeholder for now — Connection() detects it and stays idle.
        private const val WS_URL = "wss://your-server-url-here"
        private const val AUTH_TOKEN = ""
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_ID, buildNotification("Starting…"))

        if (connection == null) {
            connection = Connection(
                url = WS_URL,
                authToken = AUTH_TOKEN,
                onStatus = { status -> updateNotification(status) }
            )
            connection?.connect()
        }

        return START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vault Premium Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the telemetry bridge connected."
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(status: String): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, SyncService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Vault Premium")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setContentIntent(openApp)
            .addAction(0, "Stop", stop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(status: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(status))
    }

    override fun onDestroy() {
        connection?.close()
        connection = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
