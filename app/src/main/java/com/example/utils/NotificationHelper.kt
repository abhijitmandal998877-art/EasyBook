package com.example.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "easybook_reminders"
    private const val CHANNEL_NAME = "EasyBook Reminders"
    private const val CHANNEL_DESC = "Notifications reminding you about unpaid customer debts."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationHelper", "Permission POST_NOTIFICATIONS not granted!")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(System.currentTimeMillis().toInt(), builder.build())
            } catch (e: SecurityException) {
                Log.e("NotificationHelper", "SecurityException posting notification: ${e.message}")
            }
        }
    }

    /**
     * Schedule a reminder 2 days from now (or custom delay for demo) using AlarmManager
     */
    fun scheduleReminderAlarm(context: Context, customerName: String, amount: Double, delayMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("CUSTOMER_NAME", customerName)
            putExtra("AMOUNT", amount)
        }

        val requestCode = customerName.hashCode() + amount.toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + delayMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d("NotificationHelper", "Reminder alarm scheduled successfully for $customerName with amount $amount in ${delayMillis / 1000}s")
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "SecurityException scheduling alarm: ${e.message}")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to schedule alarm: ${e.message}")
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val customerName = intent.getStringExtra("CUSTOMER_NAME") ?: "Customer"
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)

        // Only remind if the amount owed is greater than zero
        if (amount > 0.0) {
            val title = "Payment Reminder: $customerName"
            val message = "Today is the scheduled day to collect outstanding balance of ₹${String.format("%.2f", amount)} from $customerName."
            NotificationHelper.sendNotification(context, title, message)
        }
    }
}
