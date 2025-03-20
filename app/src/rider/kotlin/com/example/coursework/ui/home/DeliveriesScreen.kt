package com.example.coursework.ui.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import com.example.coursework.ui.theme.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.coursework.ui.features.notifications.ErrorScreen
import com.example.coursework.ui.features.notifications.LoadingScreen
import com.example.coursework.ui.theme.TextStyle
import com.example.coursework.ui.theme.Theme
import com.example.coursework.utils.StringUtils

@Composable
fun DeliveriesScreen(
    navController: NavController,
    homeViewModel: DeliveriesViewModel = hiltViewModel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Доставка",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        val uiState = homeViewModel.deliveriesState.collectAsStateWithLifecycle()
        when (val state = uiState.value) {
            is DeliveriesViewModel.DeliveriesState.Loading -> {
                LoadingScreen()
            }

            is DeliveriesViewModel.DeliveriesState.Success -> {
                LazyColumn {
                    items(state.deliveries) { delivery ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = delivery.customerAddress,
                                style = TextStyle.titleMedium
                            )
                            Text(
                                text = delivery.restaurantAddress,
                                style = TextStyle.bodyMedium
                            )
                            Text(
                                text = delivery.orderId,
                                style = TextStyle.bodyMedium
                            )
                            Text(
                                text = "${delivery.estimatedDistance} km",
                                style = TextStyle.bodyMedium,
                                color = Theme.extendedColorScheme.onBackgroundHint
                            )
                            Text(
                                text = StringUtils.formatCurrency(delivery.estimatedEarning),
                                color = Color.Green
                            )

                            Row {
                                Button(onClick = { homeViewModel.deliveryAccepted(delivery) }) {
                                    Text(text = "Принять")
                                }
                                Button(onClick = { homeViewModel.deliveryRejected(delivery) }) {
                                    Text(text = "Отклонить")
                                }
                            }
                        }
                    }
                }
            }

            is DeliveriesViewModel.DeliveriesState.Error -> {
                ErrorScreen(message = state.message) {
                    homeViewModel.getDeliveries()
                }
            }
        }
    }
}