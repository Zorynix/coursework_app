package com.example.coursework.ui.feature.home

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.data.FoodApi
import com.example.coursework.data.models.Category
import com.example.coursework.data.models.Restaurant
import com.example.coursework.data.remote.ApiResponse
import com.example.coursework.data.remote.safeApiCall
import com.example.coursework.di.SearchHistoryPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodApi: FoodApi,
    @SearchHistoryPrefs private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<HomeScreenNavigationEvents?>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    var categories = emptyList<Category>()
    var restaurants = emptyList<Restaurant>()
    private var lastSearchQuery: String? = null
    //private var currentSearchQuery: String = ""

    init {
        loadInitialData()
        _searchHistory.value = getSearchHistory()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = HomeScreenState.Loading
            categories = getCategories()
            restaurants = getPopularRestaurants()
            if (categories.isNotEmpty() && restaurants.isNotEmpty()) {
                _uiState.value = HomeScreenState.Success
            } else {
                _uiState.value = HomeScreenState.Empty
            }
        }
    }

    fun searchRestaurants(query: String) {
        viewModelScope.launch {
            _uiState.value = HomeScreenState.Loading
            if (query.isEmpty()) {
                _uiState.value = HomeScreenState.Success
            } else {
                val filteredRestaurants = restaurants.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                if (filteredRestaurants.isNotEmpty()) {
                    _uiState.value = HomeScreenState.SearchResults(filteredRestaurants)
                } else {
                    _uiState.value = HomeScreenState.NoSearchResults
                }
            }
        }
    }

    fun retryLastSearch() {
        lastSearchQuery?.let { query ->
            Log.d("RetryLastSearch", "Повторный поиск с запросом: $query")
            searchRestaurants(query)
        } ?: Log.d("RetryLastSearch", "Нет последнего запроса для повторения")
    }

    private suspend fun getCategories(): List<Category> {
        var list = emptyList<Category>()
        val response = safeApiCall { foodApi.getCategories() }
        when (response) {
            is ApiResponse.Success -> list = response.data.data
            else -> {}
        }
        return list
    }

    private suspend fun getPopularRestaurants(): List<Restaurant> {
        var list = emptyList<Restaurant>()
        val response = safeApiCall { foodApi.getRestaurants(40.7128, -74.0060) }
        when (response) {
            is ApiResponse.Success -> list = response.data.data
            else -> {}
        }
        return list
    }

    fun onRestaurantSelected(restaurant: Restaurant) {
        viewModelScope.launch {
            _navigationEvent.emit(
                HomeScreenNavigationEvents.NavigateToDetail(restaurant.name, restaurant.imageUrl, restaurant.id)
            )
        }
    }

    private companion object {
        const val SEARCH_HISTORY_KEY = "search_history"
    }

    private fun getSearchHistory(): List<String> {
        val historyString = sharedPreferences.getString(SEARCH_HISTORY_KEY, "") ?: ""
        val history = historyString.split(",").filter { it.isNotBlank() }
        _searchHistory.value = history
        return history
    }

    fun addToSearchHistory(query: String) {
        val currentHistory = getSearchHistory().toMutableList()
        if (currentHistory.contains(query)) {
            currentHistory.remove(query)
        }
        currentHistory.add(0, query)
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        val historyString = currentHistory.joinToString(",")
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, historyString).apply()
        _searchHistory.value = currentHistory
    }

    fun clearSearchHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
        _searchHistory.value = emptyList()
    }

    sealed class HomeScreenState {
        object Loading : HomeScreenState()
        object Empty : HomeScreenState()
        object Success : HomeScreenState()
        data class SearchResults(val restaurants: List<Restaurant>) : HomeScreenState()
        object NoSearchResults : HomeScreenState()
        object Error : HomeScreenState()
    }

    sealed class HomeScreenNavigationEvents {
        data class NavigateToDetail(val name: String, val imageUrl: String, val id: String) :
            HomeScreenNavigationEvents()
    }
}