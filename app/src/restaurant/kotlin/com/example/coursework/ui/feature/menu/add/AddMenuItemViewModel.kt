package com.example.coursework.ui.feature.menu.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.data.FoodApi
import com.example.coursework.data.CourseWorkSession
import com.example.coursework.data.models.FoodItem
import com.example.coursework.data.remote.ApiResponse
import com.example.coursework.data.remote.safeApiCall
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddMenuItemViewModel @Inject constructor(
    val foodApi: FoodApi,
    val session: CourseWorkSession,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _imageUrl = MutableStateFlow<Uri?>(null)
    val imageUrl = _imageUrl.asStateFlow()

    private val _addMenuItemState = MutableStateFlow<AddMenuItemState>(AddMenuItemState.Idle)
    val addMenuItemState = _addMenuItemState.asStateFlow()

    private val _addMenuItemEvent = MutableSharedFlow<AddMenuItemEvent>()
    val addMenuItemEvent = _addMenuItemEvent.asSharedFlow()

    init {
        Logger.t("AddMenuItemVM").d("ViewModel initialized")
    }

    fun onNameChange(name: String) {
        Logger.t("AddMenuItemVM").d("Name changed to: $name")
        _name.value = name
    }

    fun onDescriptionChange(description: String) {
        Logger.t("AddMenuItemVM").d("Description changed to: $description")
        _description.value = description
    }

    fun onPriceChange(price: String) {
        Logger.t("AddMenuItemVM").d("Price changed to: $price")
        _price.value = price
    }

    fun onImageUrlChange(imageUrl: Uri?) {
        Logger.t("AddMenuItemVM").d("Image URL changed to: $imageUrl")
        _imageUrl.value = imageUrl
    }

    fun addMenuItem() {
        val name = name.value
        val description = description.value
        val price = price.value.toDoubleOrNull() ?: 0.0
        val restaurantId = session.getRestaurantId() ?: ""

        Logger.t("AddMenuItemVM").d("Add menu item requested - Name: $name, Description: $description, Price: $price, RestaurantID: $restaurantId")

        if (name.isEmpty() || description.isEmpty() || price == 0.0 || imageUrl.value == null) {
            Logger.t("AddMenuItemVM").w("Validation failed: empty fields or no image")
            _addMenuItemEvent.tryEmit(AddMenuItemEvent.ShowErrorMessage("Пожалуйста заполните все поля"))
            return
        }

        viewModelScope.launch {
            _addMenuItemState.value = AddMenuItemState.Loading
            Logger.t("AddMenuItemVM").d("Starting image upload")
            val imageUrl = uploadImage(imageUri = imageUrl.value!!)
            if (imageUrl == null) {
                Logger.t("AddMenuItemVM").e("Image upload failed")
                _addMenuItemState.value = AddMenuItemState.Error("Загрузка изображения не удалась")
                return@launch
            }
            Logger.t("AddMenuItemVM").d("Image uploaded successfully, URL: $imageUrl")

            val response = safeApiCall {
                foodApi.addRestaurantMenu(
                    restaurantId,
                    FoodItem(
                        name = name,
                        description = description,
                        price = price,
                        imageUrl = imageUrl,
                        restaurantId = restaurantId
                    )
                )
            }
            when (response) {
                is ApiResponse.Success -> {
                    Logger.t("AddMenuItemVM").d("Menu item added successfully")
                    _addMenuItemState.value = AddMenuItemState.Success("Блюдо добавлено успешно")
                    _addMenuItemEvent.emit(AddMenuItemEvent.GoBack)
                }
                is ApiResponse.Error -> {
                    Logger.t("AddMenuItemVM").e("API error: ${response.message}")
                    _addMenuItemState.value = AddMenuItemState.Error(response.message)
                }
                is ApiResponse.Exception -> {
                    Logger.t("AddMenuItemVM").e("Network exception occurred during API call")
                    _addMenuItemState.value = AddMenuItemState.Error("Network Error")
                }
            }
        }
    }

    suspend fun uploadImage(imageUri: Uri): String? {
        Logger.t("AddMenuItemVM").d("Uploading image: $imageUri")
        val file = fileFromUri(imageUri)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)
        val response = safeApiCall { foodApi.uploadImage(multipartBody) }
        when (response) {
            is ApiResponse.Success -> {
                Logger.t("AddMenuItemVM").d("Image upload successful, URL: ${response.data.url}")
                return response.data.url
            }
            else -> {
                Logger.t("AddMenuItemVM").e("Image upload failed")
                return null
            }
        }
    }

    private fun fileFromUri(imageUri: Uri): File {
        Logger.t("AddMenuItemVM").d("Creating file from URI: $imageUri")
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val file = File.createTempFile(
            "temp-${System.currentTimeMillis()}-coursework",
            "jpg",
            context.cacheDir
        )
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Logger.t("AddMenuItemVM").d("File created: ${file.absolutePath}")
        return file
    }

    fun onImageClicked() {
        viewModelScope.launch {
            Logger.t("AddMenuItemVM").d("Image clicked, emitting AddNewImage event")
            _addMenuItemEvent.emit(AddMenuItemEvent.AddNewImage)
        }
    }

    sealed class AddMenuItemState {
        object Idle : AddMenuItemState()
        object Loading : AddMenuItemState()
        data class Success(val message: String) : AddMenuItemState()
        data class Error(val message: String) : AddMenuItemState()
    }

    sealed class AddMenuItemEvent {
        data class ShowErrorMessage(val message: String) : AddMenuItemEvent()
        object AddNewImage : AddMenuItemEvent()
        object GoBack : AddMenuItemEvent()
    }
}