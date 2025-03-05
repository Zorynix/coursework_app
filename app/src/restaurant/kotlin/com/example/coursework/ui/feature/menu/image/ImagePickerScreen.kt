package com.example.coursework.ui.feature.menu.image

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.orhanobut.logger.Logger

@Composable
fun ImagePickerScreen(navController: NavController) {
    Logger.t("ImagePicker").d("ImagePickerScreen initialized")

    val context = LocalContext.current
    val selectedImageUri = remember {
        mutableStateOf<Uri?>(null)
    }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Logger.t("ImagePicker").d("Image selected: $uri")
                selectedImageUri.value = uri
            } else {
                Logger.t("ImagePicker").w("No image selected, navigating back")
                Toast.makeText(context, "Изображение не выбрано", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Logger.t("ImagePicker").d("Permission granted, launching image picker")
                imagePickerLauncher.launch("image/*")
            } else {
                Logger.t("ImagePicker").w("Permission denied")
            }
        }

    LaunchedEffect(key1 = true) {
        Logger.t("ImagePicker").d("Checking permission")
        if (context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Logger.t("ImagePicker").d("Permission already granted, launching image picker")
            imagePickerLauncher.launch("image/*")
        } else {
            Logger.t("ImagePicker").d("Requesting permission")
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = selectedImageUri.value,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            Logger.t("ImagePicker").d("Select image button clicked, URI: ${selectedImageUri.value}")
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "imageUri",
                selectedImageUri.value
            )
            navController.popBackStack()
        }) {
            Text(text = "Выбрать изображение")
        }
    }
}