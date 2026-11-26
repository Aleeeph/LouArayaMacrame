package cl.louaraya.macrame.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.louaraya.macrame.ui.screens.CampaignsScreen
import cl.louaraya.macrame.ui.screens.CatalogScreen
import cl.louaraya.macrame.ui.screens.LoginScreen
import cl.louaraya.macrame.ui.screens.MapScreen
import cl.louaraya.macrame.ui.screens.OrdersScreen
import cl.louaraya.macrame.ui.screens.ProductDetailScreen
import cl.louaraya.macrame.ui.screens.ProductFormScreen
import cl.louaraya.macrame.ui.screens.ProfileScreen
import cl.louaraya.macrame.ui.screens.QrScannerScreen
import cl.louaraya.macrame.ui.screens.RegisterScreen
import cl.louaraya.macrame.ui.screens.SplashScreen
import cl.louaraya.macrame.viewmodel.SessionState
import cl.louaraya.macrame.viewmodel.SessionViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CATALOG = "catalog"
    const val CAMPAIGNS = "campaigns"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val QR_SCANNER = "qr_scanner"
    const val MAP = "map"
    const val PRODUCT_FORM = "product_form?productId={productId}"
    const val PRODUCT_DETAIL = "product_detail/{productId}"

    fun productForm(id: Long = -1L) = "product_form?productId=$id"
    fun productDetail(id: Long) = "product_detail/$id"
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.CATALOG, "Catálogo", Icons.Rounded.Storefront),
    TabItem(Routes.CAMPAIGNS, "Campañas", Icons.Rounded.Campaign),
    TabItem(Routes.ORDERS, "Solicitudes", Icons.Rounded.ShoppingBag),
    TabItem(Routes.PROFILE, "Perfil", Icons.Rounded.Person)
)

@Composable
fun AppNavHost(sessionVm: SessionViewModel) {
    val navController = rememberNavController()
    val session by sessionVm.session.collectAsStateWithLifecycle()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(300)) { it } + fadeIn(),
                exit = slideOutVertically(tween(200)) { it } + fadeOut()
            ) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.CATALOG) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 10 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 10 } }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    ready = session !is SessionState.Loading,
                    onFinished = {
                        val destination = if (session is SessionState.LoggedIn) Routes.CATALOG else Routes.LOGIN
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.CATALOG) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onGoRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Routes.CATALOG) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.CATALOG) {
                CatalogScreen(
                    onAddProduct = { navController.navigate(Routes.productForm()) },
                    onOpenProduct = { navController.navigate(Routes.productDetail(it)) },
                    onOpenScanner = { navController.navigate(Routes.QR_SCANNER) }
                )
            }
            composable(Routes.QR_SCANNER) {
                QrScannerScreen(
                    onBack = { navController.popBackStack() },
                    onProductFound = { id ->
                        navController.navigate(Routes.productDetail(id)) {
                            popUpTo(Routes.QR_SCANNER) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.CAMPAIGNS) { CampaignsScreen() }
            composable(Routes.ORDERS) { OrdersScreen() }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onOpenMap = { navController.navigate(Routes.MAP) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.MAP) {
                MapScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.PRODUCT_FORM,
                arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                ProductFormScreen(
                    productId = entry.arguments?.getLong("productId") ?: -1L,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { entry ->
                val productId = entry.arguments?.getLong("productId") ?: -1L
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.productForm(it)) }
                )
            }
        }
    }
}
