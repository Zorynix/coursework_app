package com.example.coursework.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.coursework.data.FoodApi
import com.example.coursework.data.models.FCMRequest
import com.example.coursework.data.remote.ApiResponse
import com.example.coursework.data.remote.safeApiCall
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.orhanobut.logger.Logger
import android.os.Build
import androidx.annotation.RequiresApi

@Singleton
class CourseWorkNotificationManager @Inject constructor(
    private val foodApi: FoodApi,
    @ApplicationContext val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val job = CoroutineScope(Dispatchers.IO + SupervisorJob())

    enum class NotificationChannelType(
        val id: String,
        val channelName: String,
        val channelDesc: String,
        val importance: Int,
    ) {
        ORDER("1", "Order", "Order", NotificationManager.IMPORTANCE_HIGH),
        PROMOTION("2", "Promotion", "Promotion", NotificationManager.IMPORTANCE_DEFAULT),
        ACCOUNT("3", "Account", "Account", NotificationManager.IMPORTANCE_LOW),
    }

    fun createChannels() {
        NotificationChannelType.entries.forEach {
            val channel =
                NotificationChannelCompat.Builder(it.id, it.importance)
                    .setDescription(it.channelDesc)
                    .setName(it.channelName)
                    .build()
            notificationManager.createNotificationChannel(channel)
        }
        Logger.t("NotificationManager").d("Notification channels created: ${NotificationChannelType.entries.joinToString { it.channelName }}")
    }

    fun getAndStoreToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                updateToken(it.result)
            } else {
                Logger.t("FCM").e("Failed to retrieve FCM token: ${it.exception?.message}")
            }
        }
    }

    fun updateToken(token: String) {
        job.launch {
            val res = safeApiCall { foodApi.updateToken(FCMRequest(token)) }
            when (res) {
                is ApiResponse.Success -> {
                    Logger.t("FCM").d("Token updated successfully: ${res.data.message}")
                }
                is ApiResponse.Error -> {
                    Logger.t("FCM").e("Failed to update token: ${res.message}")
                }

                is ApiResponse.Exception -> TODO()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun showNotification(
        title: String,
        message: String,
        notificationID: Int,
        intent: PendingIntent,
        notificationChannelType: NotificationChannelType,
    ) {
        val notification =
            NotificationCompat.Builder(context, notificationChannelType.id)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(notificationID, notification)

        Logger.t("NotificationManager").d(
            "Notification shown: id=$notificationID, title='$title', message='$message', channel=${notificationChannelType.channelName}"
        )

    }
}