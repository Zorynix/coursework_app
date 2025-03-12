package com.example.coursework.ui.feature.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.example.coursework.ui.theme.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.coursework.R
import com.example.coursework.data.models.Address
import com.example.coursework.data.models.CartItem
import com.example.coursework.data.models.CheckoutDetails
import com.example.coursework.ui.BasicDialog
import com.example.coursework.ui.feature.food_item_details.FoodItemCounter
import com.example.coursework.ui.navigation.AddressList
import com.example.coursework.ui.navigation.OrderSuccess
import com.example.coursework.ui.theme.TextStyle
import com.example.coursework.ui.theme.Theme
import com.example.coursework.utils.StringUtils
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, viewModel: CartViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val showErrorDialog =
        remember {
            mutableStateOf(
                false,
            )
        }
    val address =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Address?>(
            "address",
            null,
        )
            ?.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = address?.value) {
        address?.value?.let {
            viewModel.onAddressSelected(it)
        }
    }

    val paymentSheet =
        rememberPaymentSheet(paymentResultCallback = {
            if (it is PaymentSheetResult.Completed) {
                viewModel.onPaymentSuccess()
            } else {
                viewModel.onPaymentFailed()
            }
        })
    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when (it) {
                is CartViewModel.CartEvent.onItemRemoveError,
                is CartViewModel.CartEvent.onQuantityUpdateError,
                is CartViewModel.CartEvent.showErrorDialog,
                -> {
                    showErrorDialog.value = true
                }

                is CartViewModel.CartEvent.onAddressClicked -> {
                    navController.navigate(AddressList)
                }

                is CartViewModel.CartEvent.OrderSuccess -> {
                    navController.navigate(OrderSuccess(it.orderId!!))
                }

                is CartViewModel.CartEvent.OnInitiatePayment -> {
                    PaymentConfiguration.init(navController.context, it.data.publishableKey)
                    val customer =
                        PaymentSheet.CustomerConfiguration(
                            it.data.customerId,
                            it.data.ephemeralKeySecret,
                        )
                    val paymentSheetConfig =
                        PaymentSheet.Configuration(
                            merchantDisplayName = "CourseWork",
                            customer = customer,
                            allowsDelayedPaymentMethods = false,
                        )

                    paymentSheet.presentWithPaymentIntent(
                        it.data.paymentIntentClientSecret,
                        paymentSheetConfig,
                    )
                }

                else -> {
                }
            }
        }
    }
    Column(
        modifier =
        Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                tint = MaterialTheme.colorScheme.onBackground,
                painter = painterResource(id = R.drawable.back),
                modifier =
                Modifier
                    .clip(CircleShape)
                    .clickable {
                        navController.popBackStack()
                    },
                contentDescription = "Назад",
            )
            Text(text = "Корзина", style = TextStyle.titleLarge)
            Spacer(modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.size(16.dp))
        when (uiState.value) {
            is CartViewModel.CartUiState.Loading -> {
                Spacer(modifier = Modifier.size(16.dp))
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    CircularProgressIndicator()
                    Text(
                        text = "Загрузка",
                        style = TextStyle.bodyMedium,
                        color = Theme.extendedColorScheme.onBackgroundHint,
                    )
                }
            }

            is CartViewModel.CartUiState.Success -> {
                val data = (uiState.value as CartViewModel.CartUiState.Success).data
                if (data.items.size > 0) {
                    LazyColumn {
                        items(data.items) { it ->
                            CartItemView(cartItem = it, onIncrement = { cartItem, _ ->
                                viewModel.incrementQuantity(cartItem)
                            }, onDecrement = { cartItem, _ ->
                                viewModel.decrementQuantity(cartItem)
                            }, onRemove = {
                                viewModel.removeItem(it)
                            })
                        }
                        item {
                            CheckoutDetailsView(data.checkoutDetails)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cart),
                            contentDescription = null,
                            tint = Theme.extendedColorScheme.onBackgroundHint,
                        )
                        Text(
                            text = "Корзина пуста",
                            style = TextStyle.bodyMedium,
                            color = Theme.extendedColorScheme.onBackgroundHint,
                        )
                    }
                }
            }

            is CartViewModel.CartUiState.Error -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val message = (uiState.value as CartViewModel.CartUiState.Error).message
                    Text(text = message, style = TextStyle.bodyMedium)
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Обновить")
                    }
                }
            }

            CartViewModel.CartUiState.Nothing -> {}
        }
        val selectedAddress = viewModel.selectedAddress.collectAsStateWithLifecycle()
        Spacer(modifier = Modifier.weight(1f))
        if (uiState.value is CartViewModel.CartUiState.Success) {
            AddressCard(selectedAddress.value) {
                viewModel.onAddressClicked()
            }
            Button(
                onClick = { viewModel.checkout() },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAddress.value != null,
            ) {
                Text(text = "Заказать")
            }
        }
    }

    if (showErrorDialog.value) {
        ModalBottomSheet(onDismissRequest = { showErrorDialog.value = false }) {
            BasicDialog(title = viewModel.errorTitle, description = viewModel.errorMessage) {
                showErrorDialog.value = false
            }
        }
    }
}

@Composable
fun AddressCard(address: Address?, modifier: Modifier = Modifier, onAddressClicked: () -> Unit) {
    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(8.dp)
            .clip(
                RoundedCornerShape(16.dp),
            )
            .background(Theme.extendedColorScheme.backgroundBox)
            .clickable { onAddressClicked.invoke() }
            .padding(16.dp),
    ) {
        if (address != null) {
            Column {
                Text(text = address.addressLine1, style = TextStyle.titleMedium)
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "${address.city}, ${address.state}, ${address.country}",
                    style = TextStyle.bodyMedium,
                    color = Theme.extendedColorScheme.onBackgroundHint,
                )
            }
        } else {
            Text(text = "Выбрать адрес", style = TextStyle.bodyMedium)
        }
    }
}

@Composable
fun CheckoutDetailsView(checkoutDetails: CheckoutDetails, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        CheckoutRowItem(title = "Промежуточный итог", value = checkoutDetails.subTotal, currency = "USD")
        CheckoutRowItem(title = "НДС", value = checkoutDetails.tax, currency = "USD")
        CheckoutRowItem(
            title = "Доставка",
            value = checkoutDetails.deliveryFee,
            currency = "USD",
        )
        CheckoutRowItem(title = "Всего", value = checkoutDetails.totalAmount, currency = "USD")
    }
}

@Composable
fun CheckoutRowItem(modifier: Modifier = Modifier, title: String, value: Double, currency: String) {
    Column(modifier = modifier) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            Text(text = title, style = TextStyle.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = StringUtils.formatCurrency(value),
                style = TextStyle.titleMedium,
            )
            Text(
                text = currency,
                style = TextStyle.titleMedium,
                color = Color.LightGray,
            )
        }
        VerticalDivider()
    }
}

@Composable
fun CartItemView(
    modifier: Modifier = Modifier,
    cartItem: CartItem,
    onIncrement: (CartItem, Int) -> Unit,
    onDecrement: (CartItem, Int) -> Unit,
    onRemove: (CartItem) -> Unit,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AsyncImage(
            model = cartItem.menuItemId.imageUrl,
            contentDescription = null,
            modifier =
            Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = cartItem.menuItemId.name, style = TextStyle.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onRemove.invoke(cartItem) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = cartItem.menuItemId.description,
                maxLines = 1,
                color = Theme.extendedColorScheme.onBackgroundHint,
                style = TextStyle.bodySmall,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${cartItem.menuItemId.price}",
                    style = TextStyle.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.weight(1f))
                FoodItemCounter(
                    count = cartItem.quantity,
                    onCounterIncrement = { onIncrement.invoke(cartItem, cartItem.quantity) },
                    onCounterDecrement = { onDecrement.invoke(cartItem, cartItem.quantity) },
                )
            }
        }
    }
}
