package com.example.coursework.ui.feature.menu.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.data.FoodApi
import com.example.coursework.data.CourseWorkSession
import com.example.coursework.data.models.FoodItem
import com.example.coursework.data.remote.ApiResponse
import com.example.coursework.data.remote.safeApiCall
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListMenuItemViewModel @Inject constructor(val foodApi: FoodApi, val session: CourseWorkSession) :
    ViewModel() {

    private val _listMenuItemState = MutableStateFlow<ListMenuItemState>(ListMenuItemState.Loading)
    val listMenuItemState = _listMenuItemState.asStateFlow()

    private val _menuItemEvent = MutableSharedFlow<MenuItemEvent>()
    val menuItemEvent = _menuItemEvent.asSharedFlow()

    init {
        Logger.t("ListMenuItemVM").d("ViewModel initialized")
        getListItem()
    }

    private fun getListItem() {
        Logger.t("ListMenuItemVM").d("Fetching menu items")
        viewModelScope.launch {
            val restaurantID = session.getRestaurantId() ?: ""
            Logger.t("ListMenuItemVM").d("Restaurant ID: $restaurantID")
            val response = safeApiCall { foodApi.getRestaurantMenu(restaurantID) }
            when (response) {
                is ApiResponse.Success -> {
                    Logger.t("ListMenuItemVM").d("Menu items fetched successfully, count: ${response.data.foodItems.size}")
                    _listMenuItemState.value = ListMenuItemState.Success(response.data.foodItems)
                }
                is ApiResponse.Error -> {
                    Logger.t("ListMenuItemVM").e("API error: ${response.message}")
                    _listMenuItemState.value = ListMenuItemState.Error(response.message)
                }
                is ApiResponse.Exception -> {
                    Logger.t("ListMenuItemVM").e("Exception occurred during API call")
                    _listMenuItemState.value = ListMenuItemState.Error("An error occurred")
                }
            }
        }
    }

    fun retry() {
        Logger.t("ListMenuItemVM").d("Retry requested")
        getListItem()
    }

    fun onAddItemClicked() {
        viewModelScope.launch {
            Logger.t("ListMenuItemVM").d("Add new menu item clicked")
            _menuItemEvent.emit(MenuItemEvent.AddNewMenuItem)
        }
    }

    sealed class MenuItemEvent {
        object AddNewMenuItem : MenuItemEvent()
    }

    sealed class ListMenuItemState {
        object Loading : ListMenuItemState()
        data class Success(val data: List<FoodItem>) : ListMenuItemState()
        data class Error(val message: String) : ListMenuItemState()
    }
}