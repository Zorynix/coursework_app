package com.example.coursework

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import com.example.coursework.ui.theme.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.coursework.ui.theme.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.coursework.data.CourseWorkSession
import com.example.coursework.data.FoodApi
import com.example.coursework.data.models.FoodItem
import com.example.coursework.ui.feature.add_address.AddAddressScreen
import com.example.coursework.ui.feature.address_list.AddressListScreen
import com.example.coursework.ui.feature.cart.CartScreen
import com.example.coursework.ui.feature.cart.CartViewModel
import com.example.coursework.ui.feature.food_item_details.FoodDetailsScreen
import com.example.coursework.ui.feature.home.HomeScreen
import com.example.coursework.ui.feature.order_details.OrderDetailsScreen
import com.example.coursework.ui.feature.order_success.OrderSuccess
import com.example.coursework.ui.feature.orders.OrderListScreen
import com.example.coursework.ui.feature.restaurant_details.RestaurantDetailsScreen
import com.example.coursework.ui.features.auth.AuthScreen
import com.example.coursework.ui.features.auth.login.SignInScreen
import com.example.coursework.ui.features.auth.signup.SignUpScreen
import com.example.coursework.ui.features.notifications.NotificationsList
import com.example.coursework.ui.features.notifications.NotificationsViewModel
import com.example.coursework.ui.navigation.AddAddress
import com.example.coursework.ui.navigation.AddressList
import com.example.coursework.ui.navigation.AuthScreen
import com.example.coursework.ui.navigation.Cart
import com.example.coursework.ui.navigation.FoodDetails
import com.example.coursework.ui.navigation.Home
import com.example.coursework.ui.navigation.Login
import com.example.coursework.ui.navigation.NavRoute
import com.example.coursework.ui.navigation.Notification
import com.example.coursework.ui.navigation.OrderDetails
import com.example.coursework.ui.navigation.OrderList
import com.example.coursework.ui.navigation.OrderSuccess
import com.example.coursework.ui.navigation.RestaurantDetails
import com.example.coursework.ui.navigation.SignUp
import com.example.coursework.ui.navigation.foodItemNavType
import com.example.coursework.ui.theme.CourseWorkTheme
import com.example.coursework.ui.theme.Mustard
import com.example.coursework.ui.theme.Theme
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.reflect.typeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseCourseWorkActivity() {
    var showSplashScreen = true

    @Inject lateinit var foodApi: FoodApi

    @Inject lateinit var session: CourseWorkSession

    sealed class BottomNavItem(val route: NavRoute, val icon: Int) {
        object Home : BottomNavItem(com.example.coursework.ui.navigation.Home, R.drawable.ic_home)
        object Cart : BottomNavItem(com.example.coursework.ui.navigation.Cart, R.drawable.ic_cart)
        object Notification : BottomNavItem(com.example.coursework.ui.navigation.Notification, R.drawable.ic_notification)
        object Orders : BottomNavItem(OrderList, R.drawable.ic_orders)
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { showSplashScreen }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_X, 0.5f, 0f)
                val zoomY = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_Y, 0.5f, 0f)
                zoomX.duration = 500
                zoomY.duration = 500
                zoomX.interpolator = OvershootInterpolator()
                zoomY.interpolator = OvershootInterpolator()
                zoomX.doOnEnd { screen.remove() }
                zoomY.doOnEnd {
                    screen.remove()
                    Logger.t("SplashScreen").d("Splash screen animation completed and removed")
                }
                zoomY.start()
                zoomX.start()
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val themeViewModel: ThemeViewModel = hiltViewModel()

            val theme by themeViewModel.currentTheme.collectAsStateWithLifecycle()


            CourseWorkTheme(theme = theme) {
                val shouldShowBottomNav = remember { mutableStateOf(false) }
                val navItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Cart,
                    BottomNavItem.Notification,
                    BottomNavItem.Orders,
                )
                val navController = rememberNavController()
                val cartViewModel: CartViewModel = hiltViewModel()
                val cartItemSize = cartViewModel.cartItemCount.collectAsStateWithLifecycle()
                val notificationViewModel: NotificationsViewModel = hiltViewModel()
                val unreadCount = notificationViewModel.unreadCount.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = true) {
                    viewModel.event.collectLatest {
                        when (it) {
                            is HomeViewModel.HomeEvent.NavigateToOrderDetail -> {
                                Logger.t("Navigation").d("Navigating to OrderDetails with orderID=${it.orderID}")
                                navController.navigate(OrderDetails(it.orderID))
                            }
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val currentRoute = navController.currentBackStackEntryAsState().value?.destination
                        AnimatedVisibility(visible = shouldShowBottomNav.value) {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                                navItems.forEach { item ->
                                    val selected = currentRoute?.hierarchy?.any {
                                        it.route == item.route::class.qualifiedName
                                    } == true

                                    NavigationBarItem(
                                        selected = selected,
                                        colors = NavigationBarItemColors(
                                            selectedIconColor = Color.Unspecified,
                                            disabledIconColor = Color.Unspecified,
                                            disabledTextColor = Color.Unspecified,
                                            selectedIndicatorColor = Color.Unspecified,
                                            selectedTextColor = Color.Unspecified,
                                            unselectedIconColor = Color.Unspecified,
                                            unselectedTextColor = Color.Unspecified,
                                        ),
                                        onClick = {
                                            Logger.t("Navigation").d("Bottom nav item clicked: ${item.route::class.simpleName}")
                                            navController.navigate(item.route)
                                        },
                                        icon = {
                                            Box(modifier = Modifier.size(48.dp)) {
                                                Icon(
                                                    painter = painterResource(id = item.icon),
                                                    contentDescription = null,
                                                    tint = if (selected) MaterialTheme.colorScheme.primary else Theme.extendedColorScheme.onBackgroundHint,
                                                    modifier = Modifier.align(Center),
                                                )
                                                if (item.route == Cart && cartItemSize.value > 0) {
                                                    ItemCount(cartItemSize.value)
                                                }
                                                if (item.route == Notification && unreadCount.value > 0) {
                                                    ItemCount(unreadCount.value)
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    },
                ) { innerPadding ->
                    SharedTransitionLayout {
                        NavHost(
                            navController = navController,
                            startDestination = if (session.getToken() != null) Home else AuthScreen,
                            modifier = Modifier.padding(innerPadding),
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300),
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300),
                                ) + fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300),
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300),
                                ) + fadeOut(animationSpec = tween(300))
                            },
                        ) {
                            composable<SignUp> {
                                shouldShowBottomNav.value = false
                                SignUpScreen(navController)
                            }
                            composable<AuthScreen> {
                                shouldShowBottomNav.value = false
                                AuthScreen(navController)
                            }
                            composable<Login> {
                                shouldShowBottomNav.value = false
                                SignInScreen(navController)
                            }
                            composable<Home> {
                                shouldShowBottomNav.value = true
                                HomeScreen(navController, this)
                            }
                            composable<RestaurantDetails> {
                                shouldShowBottomNav.value = false
                                val route = it.toRoute<RestaurantDetails>()
                                RestaurantDetailsScreen(
                                    navController,
                                    name = route.restaurantName,
                                    imageUrl = route.restaurantImageUrl,
                                    restaurantID = route.restaurantId,
                                    this,
                                )
                            }
                            composable<FoodDetails>(typeMap = mapOf(typeOf<FoodItem>() to foodItemNavType)) {
                                shouldShowBottomNav.value = false
                                val route = it.toRoute<FoodDetails>()
                                FoodDetailsScreen(
                                    navController,
                                    foodItem = route.foodItem,
                                    this,
                                    onItemAddedToCart = { cartViewModel.getCart() },
                                )
                            }
                            composable<Cart> {
                                shouldShowBottomNav.value = true
                                CartScreen(navController, cartViewModel)
                            }
                            composable<Notification> {
                                SideEffect { shouldShowBottomNav.value = true }
                                NotificationsList(navController, notificationViewModel)
                            }
                            composable<AddressList> {
                                shouldShowBottomNav.value = false
                                AddressListScreen(navController)
                            }
                            composable<AddAddress> {
                                shouldShowBottomNav.value = false
                                AddAddressScreen(navController)
                            }
                            composable<OrderSuccess> {
                                shouldShowBottomNav.value = false
                                val orderID = it.toRoute<OrderSuccess>().orderId
                                OrderSuccess(orderID, navController)
                            }
                            composable<OrderList> {
                                shouldShowBottomNav.value = true
                                OrderListScreen(navController)
                            }
                            composable<OrderDetails> {
                                SideEffect { shouldShowBottomNav.value = false }
                                val orderID = it.toRoute<OrderDetails>().orderId
                                OrderDetailsScreen(navController, orderID)
                            }
                        }
                    }
                }
            }
        }

        if (::foodApi.isInitialized) {
            Logger.t("MainActivity").d("FoodApi initialized successfully")
        } else {
            Logger.t("MainActivity").e("FoodApi failed to initialize")
        }

        CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
            showSplashScreen = false
            Logger.t("MainActivity").d("Splash screen hidden after 3 seconds")
            processIntent(intent, viewModel)
        }
    }
}

@Composable
fun BoxScope.ItemCount(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(16.dp).clip(CircleShape).background(Mustard).align(Alignment.TopEnd),
    ) {
        Text(
            text = "$count",
            modifier = Modifier.align(Center),
            color = Theme.extendedColorScheme.onBackgroundHint,
            style = TextStyle.bodySmall,
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CourseWorkTheme { Greeting("Android") }
}