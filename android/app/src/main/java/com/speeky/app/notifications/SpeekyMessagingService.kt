package com.speeky.app.notifications

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SpeekyMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // TODO: отправь token на backend и привяжи к устройству
    }

    override fun onMessageReceived(message: RemoteMessage) {
        NotificationHelper.ensureChannels(this)
        val title = message.data["title"] ?: message.notification?.title ?: "Speeky"
        val body = message.data["body"] ?: message.notification?.body ?: "Новое событие"
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}
