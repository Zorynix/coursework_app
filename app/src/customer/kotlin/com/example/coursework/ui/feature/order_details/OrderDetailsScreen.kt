package com.example.coursework.ui.feature.order_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.coursework.R
import com.example.coursework.ui.feature.orders.OrderDetailsText
import com.example.coursework.utils.StringUtils
import kotlinx.coroutines.flow.collectLatest


@Composable
fun OrderDetailsScreen(
    navController: NavController,
    orderID: String,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = orderID) {
        viewModel.getOrderDetails(orderID)
    }

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when (it) {
                is OrderDetailsViewModel.OrderDetailsEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                modifier = Modifier
                    .shadow(12.dp, clip = true, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        viewModel.navigateBack()
                    },
                contentDescription = "Назад",
            )
            Text(text = "Детали заказа", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.size(48.dp))
        }
        val uiState = viewModel.state.collectAsStateWithLifecycle()
        when (uiState.value) {
            is OrderDetailsViewModel.OrderDetailsState.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                    Text(text = "Загрузка")
                }
            }

            is OrderDetailsViewModel.OrderDetailsState.OrderDetails -> {
                val order =
                    (uiState.value as OrderDetailsViewModel.OrderDetailsState.OrderDetails).order
                OrderDetailsText(order)
                Row {
                    Text(text = "Цена:")
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = StringUtils.formatCurrency(order.totalAmount))
                }
                Row {
                    Text(text = "Дата:")
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = order.createdAt)
                }
                Row {
                    Image(
                        painter = painterResource(id = viewModel.getImage(order)),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(text = "${order.status}")
                }
            }

            is OrderDetailsViewModel.OrderDetailsState.Error -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = (uiState.value as OrderDetailsViewModel.OrderDetailsState.Error).message)
                    Button(onClick = { viewModel.getOrderDetails(orderID) }) {
                        Text(text = "Обновить")
                    }
                }
            }
        }


    }
}