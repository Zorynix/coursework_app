package com.example.coursework.ui.feature.menu.add

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.coursework.ui.CourseWorkTextField
import com.example.coursework.ui.navigation.ImagePicker
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddMenuItemScreen(
    navController: NavController,
    viewModel: AddMenuItemViewModel = hiltViewModel()
) {
    Logger.t("AddMenuItem").d("AddMenuItemScreen initialized")

    val name = viewModel.name.collectAsStateWithLifecycle()
    val description = viewModel.description.collectAsStateWithLifecycle()
    val price = viewModel.price.collectAsStateWithLifecycle()
    val uiState = viewModel.addMenuItemState.collectAsStateWithLifecycle()
    val selectedImage = viewModel.imageUrl.collectAsStateWithLifecycle()

    val imageUri =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Uri?>("imageUri", null)
            ?.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = imageUri?.value) {
        imageUri?.value?.let {
            Logger.t("AddMenuItem").d("Image URI received: $it")
            viewModel.onImageUrlChange(it)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.addMenuItemEvent.collectLatest {
            when (it) {
                is AddMenuItemViewModel.AddMenuItemEvent.GoBack -> {
                    Logger.t("AddMenuItem").d("Menu item added successfully, navigating back")
                    Toast.makeText(
                        navController.context, "Блюдо добавлено успешно", Toast.LENGTH_SHORT
                    ).show()
                    navController.previousBackStackEntry?.savedStateHandle?.set("added", true)
                    navController.popBackStack()
                }
                is AddMenuItemViewModel.AddMenuItemEvent.AddNewImage -> {
                    Logger.t("AddMenuItem").d("Navigating to ImagePicker")
                    navController.navigate(ImagePicker)
                }
                is AddMenuItemViewModel.AddMenuItemEvent.ShowErrorMessage -> {
                    Logger.t("AddMenuItem").e("Error occurred: ${it.message}")
                    Toast.makeText(navController.context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Добавить блюдо")
        AsyncImage(
            model = selectedImage.value,
            contentDescription = "Food Image",
            modifier = Modifier
                .size(140.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(LightGray)
                .clickable {
                    Logger.t("AddMenuItem").d("Image clicked")
                    viewModel.onImageClicked()
                }
        )
        CourseWorkTextField(
            value = name.value,
            onValueChange = {
                Logger.t("AddMenuItem").d("Name changed to: $it")
                viewModel.onNameChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Название") }
        )
        CourseWorkTextField(
            value = description.value,
            onValueChange = {
                Logger.t("AddMenuItem").d("Description changed to: $it")
                viewModel.onDescriptionChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Описание") }
        )
        CourseWorkTextField(
            value = price.value,
            onValueChange = {
                Logger.t("AddMenuItem").d("Price changed to: $it")
                viewModel.onPriceChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Цена") }
        )
        if (uiState.value is AddMenuItemViewModel.AddMenuItemState.Loading) {
            Button(onClick = { }, enabled = false) {
                Text(text = "Добавление...")
            }
        } else {
            if (uiState.value is AddMenuItemViewModel.AddMenuItemState.Error) {
                Text(
                    text = (uiState.value as AddMenuItemViewModel.AddMenuItemState.Error).message,
                    color = Red
                )
            }
            Button(onClick = {
                Logger.t("AddMenuItem").d("Add menu item button clicked")
                viewModel.addMenuItem()
            }) {
                Text(text = "Добавить блюдо")
            }
        }
    }
}