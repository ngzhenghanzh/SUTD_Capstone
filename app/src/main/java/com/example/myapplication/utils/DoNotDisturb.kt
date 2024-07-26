package com.example.myapplication.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

class DoNotDisturb(private val context: Context) {
    fun isNotificationPolicyAccessGranted(): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun requestNotificationPolicyAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    fun enableDoNotDisturb() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_NONE) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            Toast.makeText(context, "DND is enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "DND already enabled!", Toast.LENGTH_SHORT).show()
        }
    }
}
