package com.example.coursework.notification

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.coursework.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CourseWorkMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var courseWorkNotificationManager: CourseWorkNotificationManager
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        courseWorkNotificationManager.updateToken(token)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val intent = Intent(this, MainActivity::class.java)
        val title = message.notification?.title ?: ""
        val messageText = message.notification?.body ?: ""
        val data = message.data
        val type = data["type"] ?: "general"

        if (type == "order") {
            val orderID = data[ORDER_ID]
            intent.putExtra(ORDER_ID, orderID)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val notificationChannelType =
            when (type) {
                "order" -> CourseWorkNotificationManager.NotificationChannelType.ORDER
                "general" -> CourseWorkNotificationManager.NotificationChannelType.PROMOTION
                else -> CourseWorkNotificationManager.NotificationChannelType.ACCOUNT
            }
        courseWorkNotificationManager.showNotification(
            title,
            messageText,
            13034,
            pendingIntent,
            notificationChannelType,
        )
    }

    companion object {
        const val ORDER_ID = "orderId"
    }
}
