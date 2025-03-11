package com.example.coursework.ui.feature.order_success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import com.example.coursework.ui.theme.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.coursework.ui.navigation.Home
import com.example.coursework.ui.theme.TextStyle
import com.example.coursework.ui.theme.Theme

@Composable
fun OrderSuccess(orderID: String, navController: NavController, modifier: Modifier = Modifier) {
    BackHandler {
        navController.popBackStack(route = Home, inclusive = false)
    }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(text = "Успешный заказ", style = TextStyle.titleMedium)
        Text(
            text = "ID Заказа: $orderID",
            style = TextStyle.bodyMedium,
            color = Theme.extendedColorScheme.onBackgroundHint,
        )
        Button(onClick = {
            navController.popBackStack(route = Home, inclusive = false)
        }) {
            Text(text = "Продолжить покупки")
        }
    }
}
