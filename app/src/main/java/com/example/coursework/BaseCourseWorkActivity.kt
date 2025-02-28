package com.example.coursework

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.coursework.notification.CourseWorkMessagingService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseCourseWorkActivity : ComponentActivity() {
    val viewModel by viewModels<HomeViewModel>()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent, viewModel)
    }

    protected fun processIntent(intent: Intent, viewModel: HomeViewModel) {
        if (intent.hasExtra(CourseWorkMessagingService.ORDER_ID)) {
            val orderID = intent.getStringExtra(CourseWorkMessagingService.ORDER_ID)
            viewModel.navigateToOrderDetail(orderID!!)
            intent.removeExtra(CourseWorkMessagingService.ORDER_ID)
        }
    }
}
