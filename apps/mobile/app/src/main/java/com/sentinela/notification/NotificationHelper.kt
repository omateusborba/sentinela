package com.sentinela.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sentinela.MainActivity
import com.sentinela.R
import com.sentinela.domain.usecase.ProximityAlert
import java.util.Locale

class NotificationHelper(private val context: Context) {

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showProximityAlert(alert: ProximityAlert) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_ALERT, true)
        }
        val pending = PendingIntent.getActivity(
            context,
            alert.pointId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val text = context.getString(
            R.string.notification_proximity_body,
            alert.distanceKm,
            alert.pointName,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_proximity_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(
            (NOTIFICATION_BASE_ID + alert.pointId).toInt(),
            notification,
        )
    }

    companion object {
        const val CHANNEL_ID = "sentinela_alerts"
        const val EXTRA_OPEN_ALERT = "open_alert"
        private const val NOTIFICATION_BASE_ID = 10_000
    }
}
