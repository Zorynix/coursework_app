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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.coursework.R
import com.example.coursework.ThemeViewModel
import com.example.coursework.data.models.Category
import com.example.coursework.data.models.Restaurant
import com.example.coursework.ui.navigation.RestaurantDetails
import com.example.coursework.ui.theme.AppThemeType
import com.example.coursework.ui.theme.Text
import com.example.coursework.ui.theme.TextStyle
import com.example.coursework.ui.theme.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: HomeViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    val searchHistory by viewModel.searchHistory.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val currentTheme by themeViewModel.currentTheme.collectAsState()

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

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(2000)
            viewModel.searchRestaurants(searchQuery)
        } else {
            viewModel.loadInitialData()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { themeViewModel.toggleTheme() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (currentTheme == AppThemeType.Dark) R.drawable.light_mode else R.drawable.dark_mode
                        ),
                        contentDescription = "Toggle theme",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                SearchBar(
                    modifier = Modifier.weight(1f).padding(bottom = 20.dp),
                    value = searchQuery,
                    leftContent = {
                        Image(
                            imageVector = ImageVector.vectorResource(id = R.drawable.search),
                            contentDescription = "search",
                        )
                    },
                    placeholder = "Найти еду или ресторан",
                    onChange = { newValue -> searchQuery = newValue },
                    onClear = {
                        searchQuery = ""
                        focusManager.clearFocus()
                        viewModel.loadInitialData()
                    },
                    onFocusChange = { isFocused -> isSearchFocused = isFocused },
                    onSearchComplete = {
                        if (searchQuery.isNotEmpty()) {
                            viewModel.addToSearchHistory(searchQuery)
                            viewModel.searchRestaurants(searchQuery)
                            focusManager.clearFocus()
                        }
                    })
            }

            if (isSearchFocused && searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
                Column(
                    modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
                        .padding(8.dp)) {
                    Text("История поиска", style = TextStyle.titleMedium)
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(searchHistory) { query ->
                            Text(
                                text = query,
                                modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        searchQuery = query
                                        viewModel.searchRestaurants(query)
                                        focusManager.clearFocus()
                                    }
                                    .padding(8.dp),
                                style = TextStyle.bodyMedium)
                        }
                    }
                    Button(
                        onClick = { viewModel.clearSearchHistory() },
                        modifier = Modifier.align(Alignment.End)) {
                        Text("Очистить историю")
                    }
                }
            }

            when (val state = uiState) {
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
                        Text(text = "Нет результатов поиска", textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { viewModel.retryLastSearch() }) { Text(text = "Обновить") }
                    }
                }
                is HomeViewModel.HomeScreenState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "Ошибка при выполнении запроса")
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { viewModel.retryLastSearch() }) { Text(text = "Обновить") }
                    }
                }
                is HomeViewModel.HomeScreenState.Loading -> {}
            }
        }

        if (uiState is HomeViewModel.HomeScreenState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leftContent: @Composable () -> Unit,
    onChange: (value: String) -> Unit,
    onClear: () -> Unit,
    onSearchComplete: () -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier =
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Theme.extendedColorScheme.backgroundBox)
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
            placeholder = { if (value.isEmpty()) Text(text = placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions =
            KeyboardActions(
                onSearch = {
                    onSearchComplete()
                    keyboardController?.hide()
                }),
            modifier =
            Modifier.weight(1f).focusRequester(focusRequester).onFocusChanged {
                onFocusChange(it.isFocused)
                if (it.isFocused) keyboardController?.show()
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            },
        )
    }
}

@Composable
fun CategoriesList(
    categories: List<Category>,
    modifier: Modifier = Modifier,
    onCategorySelected: (Category) -> Unit
) {
    LazyRow(modifier = modifier) {
        items(categories) { CategoryItem(category = it, onCategorySelected = onCategorySelected) }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RestaurantList(
    restaurants: List<Restaurant>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    onRestaurantSelected: (Restaurant) -> Unit,
) {
    Column(modifier = modifier) {
        Row {
            Text(
                text = "Популярные рестораны",
                style = TextStyle.titleMedium,
                modifier = Modifier.padding(20.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { /*TODO*/ }) {
                Text(text = "Показать всё", style = TextStyle.bodySmall)
            }
        }
    }
    LazyRow(modifier = modifier) {
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
        Modifier.padding(8.dp)
            .width(250.dp)
            .height(229.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onRestaurantSelected(restaurant) },
    ) {
        Column(modifier = Modifier.fillMaxSize()
            .width(250.dp)
            .height(229.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Theme.extendedColorScheme.backgroundBox)
            .clickable { onRestaurantSelected(restaurant) },
        ) {
            AsyncImage(
                model = restaurant.imageUrl,
                contentDescription = null,
                modifier =
                Modifier.fillMaxSize()
                    .weight(1f)
                    .sharedElement(
                        state = rememberSharedContentState(key = "image/${restaurant.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier =
                Modifier.padding(12.dp).clickable {
                    onRestaurantSelected(restaurant)
                },
            ) {
                Text(
                    text = restaurant.name,
                    style = TextStyle.titleMedium,
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
                            style = TextStyle.bodySmall,
                            color = Theme.extendedColorScheme.onBackgroundHint,
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
                            style = TextStyle.bodySmall,
                            color = Theme.extendedColorScheme.onBackgroundHint,
                        )
                    }
                }
            }
        }
        Row(
            modifier =
            Modifier.align(TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "4.8",
                style = TextStyle.titleMedium,
                modifier = Modifier.padding(4.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Image(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                colorFilter = ColorFilter.tint(Color.Yellow),
            )
            Text(
                text = "(50)",
                style = TextStyle.bodySmall,
                color = Theme.extendedColorScheme.onBackgroundHint)
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    modifier: Modifier = Modifier,
    onCategorySelected: (Category) -> Unit
) {
    Column(
        modifier =
        modifier
            .padding(8.dp)
            .height(90.dp)
            .widthIn(60.dp, 100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Theme.extendedColorScheme.backgroundBox)
            .padding(8.dp)
            .clickable { onCategorySelected(category) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,
    ) {
        AsyncImage(
            model = category.imageUrl,
            contentDescription = null,
            modifier =
            Modifier.size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Inside,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = category.name,
            style = TextStyle.categoryName,
            textAlign = TextAlign.Center,
        )
    }
}