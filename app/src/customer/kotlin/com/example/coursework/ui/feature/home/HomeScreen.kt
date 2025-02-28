package com.example.coursework.ui.feature.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.coursework.R
import com.example.coursework.data.models.Category
import com.example.coursework.data.models.Restaurant
import com.example.coursework.ui.navigation.RestaurantDetails
import com.example.coursework.ui.theme.Primary
import com.example.coursework.ui.theme.Typography
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest {
            when (it) {
                is HomeViewModel.HomeScreenNavigationEvents.NavigateToDetail -> {
                    navController.navigate(RestaurantDetails(it.id, it.name, it.imageUrl))
                }
                else -> {}
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val uiState = viewModel.uiState.collectAsState()
        var searchQuery by rememberSaveable { mutableStateOf("") }

        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchBar(
                modifier = Modifier.weight(1f),
                value = searchQuery,
                leftContent = {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.search),
                        contentDescription = "search",
                    )
                },
                placeholder = "Найти еду или ресторан",
                onChange = { newValue ->
                    searchQuery = newValue
                    if (newValue.isNotEmpty()) {
                        viewModel.searchRestaurants(newValue)
                    } else {
                        viewModel.loadInitialData()
                    }
                },
                onClear = {
                    searchQuery = ""
                    focusManager.clearFocus()
                    viewModel.loadInitialData()
                },
            )
        }

        when (val state = uiState.value) {
            is HomeViewModel.HomeScreenState.Loading -> {
                Text(text = "Загрузка", modifier = Modifier.fillMaxSize().wrapContentSize())
            }
            is HomeViewModel.HomeScreenState.Empty -> {
                Text(text = "Пусто", modifier = Modifier.fillMaxSize().wrapContentSize())
            }
            is HomeViewModel.HomeScreenState.Success -> {
                val categories = viewModel.categories
                CategoriesList(categories = categories, onCategorySelected = {})

                RestaurantList(
                    restaurants = viewModel.restaurants,
                    animatedVisibilityScope,
                    onRestaurantSelected = { viewModel.onRestaurantSelected(it) },
                )
            }
            is HomeViewModel.HomeScreenState.SearchResults -> {
                RestaurantList(
                    restaurants = state.restaurants,
                    animatedVisibilityScope,
                    onRestaurantSelected = { viewModel.onRestaurantSelected(it) },
                )
            }
            is HomeViewModel.HomeScreenState.NoSearchResults -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Нет результатов поиска",
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retryLastSearch() }) {
                        Text(text = "Обновить")
                    }
                }
            }
            is HomeViewModel.HomeScreenState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = "Ошибка при выполнении запроса")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retryLastSearch() }) {
                        Text(text = "Обновить")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    leftContent: @Composable () -> Unit,
    onChange: (value: String) -> Unit,
    onClear: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier =
        modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEFEFEF))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        leftContent()
        Spacer(modifier = Modifier.width(8.dp))

        TextField(
            colors =
            TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
            ),
            value = value,
            onValueChange = onChange,
            placeholder = {
                if (value.isEmpty()) Text(text = placeholder)
            },
            singleLine = true,
            modifier =
            Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) keyboardController?.show()
                },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Очистить",
                        )
                    }
                }
            },
        )
    }
}

@Composable
fun CategoriesList(categories: List<Category>, onCategorySelected: (Category) -> Unit) {
    LazyRow {
        items(categories) { CategoryItem(category = it, onCategorySelected = onCategorySelected) }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RestaurantList(
    restaurants: List<Restaurant>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRestaurantSelected: (Restaurant) -> Unit,
) {
    Column {
        Row {
            Text(
                text = "Популярные рестораны",
                style = Typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { /*TODO*/ }) {
                Text(text = "Показать всё", style = Typography.bodySmall)
            }
        }
    }
    LazyRow {
        items(restaurants) { RestaurantItem(it, animatedVisibilityScope, onRestaurantSelected) }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RestaurantItem(
    restaurant: Restaurant,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRestaurantSelected: (Restaurant) -> Unit,
) {
    Box(
        modifier =
        Modifier
            .padding(8.dp)
            .width(250.dp)
            .height(229.dp)
            .shadow(16.dp, shape = RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onRestaurantSelected(restaurant) }
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = restaurant.imageUrl,
                contentDescription = null,
                modifier =
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .sharedElement(
                        state = rememberSharedContentState(key = "image/${restaurant.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier =
                Modifier
                    .background(Color.White)
                    .padding(12.dp)
                    .clickable { onRestaurantSelected(restaurant) },
            ) {
                Text(
                    text = restaurant.name,
                    style = Typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier =
                    Modifier.sharedElement(
                        state = rememberSharedContentState(key = "title/${restaurant.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_delivery),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Быстрая доставка",
                            style = Typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.timer),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Бесплатная доставка",
                            style = Typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }
        Row(
            modifier =
            Modifier
                .align(TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = "4.8", style = Typography.titleSmall, modifier = Modifier.padding(4.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Image(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                colorFilter = ColorFilter.tint(Color.Yellow),
            )
            Text(text = "(50)", style = Typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun CategoryItem(category: Category, onCategorySelected: (Category) -> Unit) {
    Column(
        modifier =
        Modifier
            .padding(8.dp)
            .height(90.dp)
            .widthIn(60.dp, 100.dp)
            .clickable { onCategorySelected(category) }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(45.dp),
                ambientColor = Color.Gray.copy(alpha = 0.8f),
                spotColor = Color.Gray.copy(alpha = 0.8f),
            )
            .background(color = Color.White)
            .clip(RoundedCornerShape(45.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,
    ) {
        AsyncImage(
            model = category.imageUrl,
            contentDescription = null,
            modifier =
            Modifier
                .size(40.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Primary,
                    spotColor = Primary,
                )
                .clip(CircleShape),
            contentScale = ContentScale.Inside,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = category.name,
            style = TextStyle(fontSize = 10.sp),
            textAlign = TextAlign.Center,
        )
    }
}
