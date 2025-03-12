package com.example.coursework.ui.feature.address_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.coursework.R
import com.example.coursework.ui.feature.cart.AddressCard
import com.example.coursework.ui.navigation.AddAddress
import com.example.coursework.ui.theme.Text
import com.example.coursework.ui.theme.TextStyle
import com.example.coursework.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddressListScreen(navController: NavController, modifier: Modifier = Modifier, viewModel: AddressListViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when (val addressEvent = it) {
                is AddressListViewModel.AddressEvent.NavigateToEditAddress -> {
                    // Navigate to edit address screen
                }

                is AddressListViewModel.AddressEvent.NavigateToAddAddress -> {
                    navController.navigate(AddAddress)
                }

                is AddressListViewModel.AddressEvent.NavigateBack -> {
                    val address = addressEvent.address
                    navController.previousBackStackEntry?.savedStateHandle?.set("address", address)
                    navController.popBackStack()
                }

                else -> {
                }
            }
        }
    }
    val isAddressAdded =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow("isAddressAdded", false)
            ?.collectAsState(false)
    LaunchedEffect(key1 = isAddressAdded?.value) {
        if (isAddressAdded?.value == true) {
            viewModel.getAddress()
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                modifier =
                Modifier.clickable {
                    navController.popBackStack()
                },
            )

            Text(text = "Адреса", style = TextStyle.titleMedium)
            Icon(
                imageVector = Icons.Filled.AddCircle,
                contentDescription = null,
                modifier =
                Modifier
                    .size(25.dp)
                    .clickable {
                        viewModel.onAddAddressClicked()
                    },
            )
        }
        when (val addressState = state.value) {
            is AddressListViewModel.AddressState.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Загрузка..",
                        style = TextStyle.bodyMedium,
                        color = Theme.extendedColorScheme.onBackgroundHint,
                    )
                }
            }

            is AddressListViewModel.AddressState.Success -> {
                LazyColumn(
                    modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                ) {
                    items(addressState.data) { address ->
                        AddressCard(address = address, onAddressClicked = {
                            viewModel.onAddressSelected(address)
                        })
                    }
                }
            }

            is AddressListViewModel.AddressState.Error -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = addressState.message,
                        style = TextStyle.bodyMedium,
                        color = Theme.extendedColorScheme.onBackgroundHint,
                    )
                    Button(onClick = { viewModel.getAddress() }) {
                        Text(text = "Обновить")
                    }
                }
            }
        }
    }
}
