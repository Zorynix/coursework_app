package com.example.coursework.ui.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.data.FoodApi
import com.example.coursework.data.models.Address
import com.example.coursework.data.models.CartItem
import com.example.coursework.data.models.CartResponse
import com.example.coursework.data.models.ConfirmPaymentRequest
import com.example.coursework.data.models.Order
import com.example.coursework.data.models.PaymentIntentRequest
import com.example.coursework.data.models.PaymentIntentResponse
import com.example.coursework.data.models.UpdateCartItemRequest
import com.example.coursework.data.remote.ApiResponse
import com.example.coursework.data.remote.safeApiCall
import com.example.coursework.ui.features.orders.OrderListViewModel.OrderListEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {
    var errorTitle: String = ""
    var errorMessage: String = ""
    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<CartEvent>()
    val event = _event.asSharedFlow()
    private var cartResponse: CartResponse? = null
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount = _cartItemCount.asStateFlow()
    private var paymentIntent: PaymentIntentResponse? = null

    private val address = MutableStateFlow<Address?>(null)
    val selectedAddress = address.asStateFlow()

    init {
        getCart()
    }

    fun getCart() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res = safeApiCall { foodApi.getCart() }
            when (res) {
                is ApiResponse.Success -> {
                    cartResponse = res.data
                    _cartItemCount.value = res.data.items.size
                    _uiState.value = CartUiState.Success(res.data)
                }

                is ApiResponse.Error -> {
                    _uiState.value = CartUiState.Error(res.message)
                }

                else -> {
                    _uiState.value = CartUiState.Error("Произошла ошибка")
                }
            }
        }
    }

    fun incrementQuantity(cartItem: CartItem) {
        if (cartItem.quantity == 5) {
            return
        }
        updateItemQuantity(cartItem, cartItem.quantity + 1)
    }

    fun decrementQuantity(cartItem: CartItem) {
        if (cartItem.quantity == 1) {
            return
        }
        updateItemQuantity(cartItem, cartItem.quantity - 1)
    }


    private fun updateItemQuantity(cartItem: CartItem, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res =
                safeApiCall { foodApi.updateCart(UpdateCartItemRequest(cartItem.id, quantity)) }
            when (res) {
                is ApiResponse.Success -> {
                    getCart()
                }

                else -> {
                    cartResponse?.let {
                        _uiState.value = CartUiState.Success(cartResponse!!)
                    }
                    errorTitle = "Не удаолось обновить количество"
                    errorMessage = "Произошла ошибка при обновлении количества этой позиции"
                    _event.emit(CartEvent.onQuantityUpdateError)
                }
            }
        }
    }

    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res =
                safeApiCall { foodApi.deleteCartItem(cartItem.id) }
            when (res) {
                is ApiResponse.Success -> {
                    getCart()
                }

                else -> {
                    cartResponse?.let {
                        _uiState.value = CartUiState.Success(cartResponse!!)
                    }
                    errorTitle = "Не удалось удалить"
                    errorMessage = "Произошла ошибка при удалении этой позиции"
                    _event.emit(CartEvent.onItemRemoveError)
                }
            }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val paymentDetails =
                safeApiCall { foodApi.getPaymentIntent(PaymentIntentRequest(address.value!!.id!!)) }

            when (paymentDetails) {
                is ApiResponse.Success -> {
                    paymentIntent = paymentDetails.data
                    _event.emit(CartEvent.OnInitiatePayment(paymentDetails.data))
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }

                else -> {
                    errorTitle = "Не удалось оформить заказ"
                    errorMessage = "Произошла ошибка при оформлении заказа"
                    _event.emit(CartEvent.showErrorDialog)
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }
            }
        }
    }

    fun onAddressClicked() {
        viewModelScope.launch {
            _event.emit(CartEvent.onAddressClicked)
        }
    }

    fun onAddressSelected(it: Address) {
        address.value = it
    }

    fun onPaymentFailed() {
        errorTitle = "Ошибка оплаты"
        errorMessage = "Произошла ошибка при оплате"
        viewModelScope.launch {
            _event.emit(CartEvent.showErrorDialog)
        }
    }

    fun onPaymentSuccess() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val response =
                safeApiCall {
                    foodApi.verifyPurchase(
                        ConfirmPaymentRequest(
                            paymentIntent!!.paymentIntentId,
                            address.value!!.id!!,
                        ),
                        paymentIntent!!.paymentIntentId,
                    )
                }
            when (response) {
                is ApiResponse.Success -> {
                    _event.emit(CartEvent.OrderSuccess(response.data.orderId))
                    _uiState.value = CartUiState.Success(cartResponse!!)
                    getCart()
                }

                else -> {
                    errorTitle = "Ошибка оплаты"
                    errorMessage = "Произошла ошибка при оплате"
                    _event.emit(CartEvent.showErrorDialog)
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }
            }
        }
    }

    sealed class CartUiState {
        object Nothing : CartUiState()
        object Loading : CartUiState()
        data class Success(val data: CartResponse) : CartUiState()
        data class Error(val message: String) : CartUiState()
    }

    sealed class CartEvent {
        object showErrorDialog : CartEvent()
        data class OrderSuccess(val orderId: String?) : CartEvent()
        object OnCheckout : CartEvent()
        data class OnInitiatePayment(val data: PaymentIntentResponse) : CartEvent()
        object onQuantityUpdateError : CartEvent()
        object onItemRemoveError : CartEvent()
        object onAddressClicked : CartEvent()
    }
}
