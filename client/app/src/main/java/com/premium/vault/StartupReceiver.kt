package com.premium.vault

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class StartupReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "Vault"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED) return

        // Only resume if the user already accepted the consent screen.
        val prefs = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(MainActivity.KEY_CONSENT, false)) {
            Log.d(TAG, "Boot: no recorded consent, staying idle")
            return
        }

        val serviceIntent = Intent(context, SyncService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "Boot: sync service requested")
        } catch (e: Exception) {
            // Never let a boot receiver throw — a crash here shows an ANR dialog
            // to the user at startup and gets the app flagged as unstable.
            Log.e(TAG, "Boot: could not start service — ${e.message}")
        }
    }
}
