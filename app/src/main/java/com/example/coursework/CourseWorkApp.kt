package com.example.coursework

import android.app.Application
import com.example.coursework.notification.CourseWorkNotificationManager
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CourseWorkApp : Application() {
    @Inject
    lateinit var courseWorkNotificationManager: CourseWorkNotificationManager
    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        courseWorkNotificationManager.createChannels()
        courseWorkNotificationManager.getAndStoreToken()
    }
}
