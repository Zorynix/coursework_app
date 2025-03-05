package com.example.coursework

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.coursework.data.FoodApi
import com.example.coursework.data.CourseWorkSession
import com.example.coursework.ui.CourseWorkNavHost
import com.example.coursework.ui.feature.home.HomeScreen
import com.example.coursework.ui.feature.menu.add.AddMenuItemScreen
import com.example.coursework.ui.feature.menu.image.ImagePickerScreen
import com.example.coursework.ui.feature.menu.list.ListMenuItemsScreen
import com.example.coursework.ui.feature.order_details.OrderDetailsScreen
import com.example.coursework.ui.feature.order_list.OrderListScreen
import com.example.coursework.ui.features.auth.AuthScreen
import com.example.coursework.ui.features.auth.login.SignInScreen
import com.example.coursework.ui.features.auth.signup.SignUpScreen
import com.example.coursework.ui.features.notifications.NotificationsList
import com.example.coursework.ui.features.notifications.NotificationsViewModel
import com.example.coursework.ui.navigation.AddMenu
import com.example.coursework.ui.navigation.AuthScreen
import com.example.coursework.ui.navigation.Home
import com.example.coursework.ui.navigation.ImagePicker
import com.example.coursework.ui.navigation.Login
import com.example.coursework.ui.navigation.MenuList
import com.example.coursework.ui.navigation.NavRoute
import com.example.coursework.ui.navigation.Notification
import com.example.coursework.ui.navigation.OrderDetails
import com.example.coursework.ui.navigation.OrderList
import com.example.coursework.ui.navigation.SignUp
import com.example.coursework.ui.theme.CourseWorkTheme
import com.example.coursework.ui.theme.Mustard
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseCourseWorkActivity() {
    var showSplashScreen = true

    @Inject
    lateinit var foodApi: FoodApi

    @Inject
    lateinit var session: CourseWorkSession

    sealed class BottomNavItem(val route: NavRoute, val icon: Int) {
        object Home : BottomNavItem(com.example.coursework.ui.navigation.Home, R.drawable.ic_home)
        object Notification :
            BottomNavItem(com.example.coursework.ui.navigation.Notification, R.drawable.ic_notification)
        object Orders : BottomNavItem(OrderList, R.drawable.ic_orders)
        object Menu : BottomNavItem(MenuList, android.R.drawable.ic_menu_more)
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.t("MainActivity").d("onCreate started")

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                showSplashScreen
            }
            setOnExitAnimationListener { screen ->
                Logger.t("SplashScreen").d("Splash screen animation started")
                val zoomX = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_X, 0.5f, 0f)
                val zoomY = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_Y, 0.5f, 0f)
                zoomX.duration = 500
                zoomY.duration = 500
                zoomX.interpolator = OvershootInterpolator()
                zoomY.interpolator = OvershootInterpolator()
                zoomX.doOnEnd {
                    screen.remove()
                    Logger.t("SplashScreen").d("Splash screen animation completed and removed")
                }
                zoomY.doOnEnd {
                    screen.remove()
                }
                zoomY.start()
                zoomX.start()
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Logger.t("MainActivity").d("Setting content")
        setContent {
            CourseWorkTheme {
                val shouldShowBottomNav = remember { mutableStateOf(false) }
                val navItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Notification,
                    BottomNavItem.Orders,
                    BottomNavItem.Menu
                )
                val navController = rememberNavController()
                val notificationViewModel: NotificationsViewModel = hiltViewModel()
                val unreadCount = notificationViewModel.unreadCount.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = true) {
                    viewModel.event.collectLatest {
                        when (it) {
                            is HomeViewModel.HomeEvent.NavigateToOrderDetail -> {
                                Logger.t("Navigation").d("Navigating to OrderDetails with ID: ${it.orderID}")
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
                            NavigationBar(containerColor = Color.White) {
                                navItems.forEach { item ->
                                    val selected = currentRoute?.hierarchy?.any { it.route == item.route::class.qualifiedName } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            Logger.t("Navigation").d("Bottom nav item clicked: ${item.route::class.simpleName}")
                                            navController.navigate(item.route)
                                        },
                                        icon = {
                                            Box(modifier = Modifier.size(48.dp)) {
                                                Icon(
                                                    painter = painterResource(id = item.icon),
                                                    contentDescription = null,
                                                    tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                                    modifier = Modifier.align(Center)
                                                )
                                                if (item.route == Notification && unreadCount.value > 0) {
                                                    ItemCount(unreadCount.value)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    SharedTransitionLayout {
                        CourseWorkNavHost(
                            navController = navController,
                            startDestination = if (session.getToken() != null) Home else AuthScreen,
                            modifier = Modifier.padding(innerPadding),
                        ) {
                            composable<SignUp> {
                                shouldShowBottomNav.value = false
                                Logger.t("Navigation").d("Navigated to SignUp screen")
                                SignUpScreen(navController)
                            }
                            composable<AuthScreen> {
                                shouldShowBottomNav.value = false
                                Logger.t("Navigation").d("Navigated to Auth screen")
                                AuthScreen(navController, false)
                            }
                            composable<Login> {
                                shouldShowBottomNav.value = false
                                Logger.t("Navigation").d("Navigated to Login screen")
                                SignInScreen(navController, false)
                            }
                            composable<Home> {
                                shouldShowBottomNav.value = true
                                Logger.t("Navigation").d("Navigated to Home screen")
                                HomeScreen(navController)
                            }
                            composable<Notification> {
                                SideEffect {
                                    shouldShowBottomNav.value = true
                                }
                                Logger.t("Navigation").d("Navigated to Notification screen")
                                NotificationsList(navController, notificationViewModel)
                            }
                            composable<OrderList> {
                                shouldShowBottomNav.value = true
                                Logger.t("Navigation").d("Navigated to OrderList screen")
                                OrderListScreen(navController)
                            }
                            composable<OrderDetails> {
                                shouldShowBottomNav.value = false
                                val orderID = it.toRoute<OrderDetails>().orderId
                                Logger.t("Navigation").d("Navigated to OrderDetails screen with ID: $orderID")
                                OrderDetailsScreen(orderID, navController)
                            }
                            composable<MenuList> {
                                shouldShowBottomNav.value = true
                                Logger.t("Navigation").d("Navigated to MenuList screen")
                                ListMenuItemsScreen(navController, this)
                            }
                            composable<AddMenu> {
                                shouldShowBottomNav.value = false
                                Logger.t("Navigation").d("Navigated to AddMenu screen")
                                AddMenuItemScreen(navController)
                            }
                            composable<ImagePicker> {
                                shouldShowBottomNav.value = false
                                Logger.t("Navigation").d("Navigated to ImagePicker screen")
                                ImagePickerScreen(navController)
                            }
                        }
                    }
                }
            }
        }

        if (::foodApi.isInitialized) {
            Logger.t("MainActivity").d("FoodApi initialized successfully")
        }

        CoroutineScope(Dispatchers.IO).launch {
            Logger.t("MainActivity").d("Starting splash screen delay")
            delay(3000)
            showSplashScreen = false
            Logger.t("MainActivity").d("Splash screen delay completed, processing intent")
            processIntent(intent, viewModel)
        }
    }
}

@Composable
fun BoxScope.ItemCount(count: Int) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(Mustard)
            .align(Alignment.TopEnd)
    ) {
        Text(
            text = "$count",
            modifier = Modifier.align(Center),
            color = Color.White,
            style = TextStyle(fontSize = 10.sp)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CourseWorkTheme {
        Greeting("Android")
    }
}