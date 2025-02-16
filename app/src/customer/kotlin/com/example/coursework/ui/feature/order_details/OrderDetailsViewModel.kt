package com.example.coursework.ui.feature.order_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.data.FoodApi
import com.example.coursework.data.models.Order
import com.example.coursework.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(private val foodApi: FoodApi) : ViewModel() {

    private val _state = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Loading)
    val state get() = _state.asStateFlow()

    private val _event = MutableSharedFlow<OrderDetailsEvent>()
    val event get() = _event.asSharedFlow()


    fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            _state.value = OrderDetailsState.Loading
            val result = safeApiCall { foodApi.getOrderDetails(orderId) }
            when (result) {
                is com.example.coursework.data.remote.ApiResponse.Success -> {
                    _state.value = OrderDetailsState.OrderDetails(result.data)
                }

                is com.example.coursework.data.remote.ApiResponse.Error -> {
                    _state.value = OrderDetailsState.Error(result.message)
                }

                is com.example.coursework.data.remote.ApiResponse.Exception -> {
                    _state.value =
                        OrderDetailsState.Error(result.exception.message ?: "Произошла ошибка")
                }
            }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _event.emit(OrderDetailsEvent.NavigateBack)
        }
    }

    fun getImage(order: Order): Int {
        when (order.status) {
            "Доставлен" -> return com.example.coursework.R.drawable.ic_delivered
            "Готовится" -> return com.example.coursework.R.drawable.ic_preparing
            "В пути" -> return com.example.coursework.R.drawable.picked_by_rider_icon
            else -> return com.example.coursework.R.drawable.ic_pending
        }
    }

    sealed class OrderDetailsEvent {
        object NavigateBack : OrderDetailsEvent()
    }

    sealed class OrderDetailsState {
        object Loading : OrderDetailsState()
        data class OrderDetails(val order: Order) : OrderDetailsState()
        data class Error(val message: String) : OrderDetailsState()
    }
}