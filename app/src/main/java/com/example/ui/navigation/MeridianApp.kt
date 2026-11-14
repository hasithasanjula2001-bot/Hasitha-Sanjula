package com.example.ui.navigation

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.ui.screens.ComplianceHubScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LcDetailScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.VerifyLcScreen
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SovereignGold
import com.example.ui.theme.SovereignGoldMuted
import com.example.viewmodel.ComplianceViewModel
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.MeridianViewModelFactory
import com.example.viewmodel.VaultViewModel
import com.example.viewmodel.VerificationViewModel

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_VERIFY = "verify"
const val ROUTE_COMPLIANCE = "compliance"
const val ROUTE_VAULT = "vault"
const val ROUTE_LC_DETAIL = "lc_detail/{lcId}"

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topDestinations = listOf(
    TopLevelDestination(
        route = ROUTE_DASHBOARD,
        labelRes = R.string.nav_dashboard,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard"
    ),
    TopLevelDestination(
        route = ROUTE_VERIFY,
        labelRes = R.string.nav_verify,
        selectedIcon = Icons.Filled.DocumentScanner,
        unselectedIcon = Icons.Outlined.DocumentScanner,
        testTag = "nav_verify"
    ),
    TopLevelDestination(
        route = ROUTE_COMPLIANCE,
        labelRes = R.string.nav_compliance,
        selectedIcon = Icons.Filled.Gavel,
        unselectedIcon = Icons.Outlined.Gavel,
        testTag = "nav_compliance"
    ),
    TopLevelDestination(
        route = ROUTE_VAULT,
        labelRes = R.string.nav_vault,
        selectedIcon = Icons.Filled.FolderSpecial,
        unselectedIcon = Icons.Outlined.FolderSpecial,
        testTag = "nav_vault"
    )
)

@Composable
fun MeridianApp() {
    val context = LocalContext.current
    val factory = remember(context.applicationContext) {
        MeridianViewModelFactory(context.applicationContext as Application)
    }

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val verificationViewModel: VerificationViewModel = viewModel(factory = factory)
    val complianceViewModel: ComplianceViewModel = viewModel(factory = factory)
    val vaultViewModel: VaultViewModel = viewModel(factory = factory)

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    val navigateTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
    ) {
        val useRail = maxWidth >= 600.dp
        val showChrome = currentRoute in topDestinations.map { it.route }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MidnightNavy,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail && showChrome) {
                    NavigationBar(
                        containerColor = DeepNavy,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        topDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                modifier = Modifier.testTag(dest.testTag),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SovereignGold,
                                    selectedTextColor = SovereignGold,
                                    indicatorColor = SovereignGoldMuted,
                                    unselectedIconColor = MutedSlate,
                                    unselectedTextColor = MutedSlate
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && showChrome) {
                    NavigationRail(
                        containerColor = DeepNavy,
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        Spacer(modifier = Modifier.weight(0.1f))
                        topDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                modifier = Modifier.testTag(dest.testTag),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = SovereignGold,
                                    selectedTextColor = SovereignGold,
                                    indicatorColor = SovereignGoldMuted,
                                    unselectedIconColor = MutedSlate,
                                    unselectedTextColor = MutedSlate
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_DASHBOARD
                    ) {
                        composable(ROUTE_DASHBOARD) {
                            val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                            DashboardScreen(
                                uiState = state,
                                onVerifyNewLc = { navigateTopLevel(ROUTE_VERIFY) },
                                onOpenComplianceHub = { navigateTopLevel(ROUTE_COMPLIANCE) },
                                onOpenVault = { navigateTopLevel(ROUTE_VAULT) },
                                onOpenLcDetail = { lcId -> navController.navigate("lc_detail/$lcId") }
                            )
                        }

                        composable(ROUTE_VERIFY) {
                            val state by verificationViewModel.uiState.collectAsStateWithLifecycle()
                            VerifyLcScreen(
                                uiState = state,
                                onDocumentSelected = { uri, mime ->
                                    verificationViewModel.onDocumentSelected(uri, mime)
                                },
                                onRetry = { verificationViewModel.retryCurrentDocument() },
                                onSaveToVault = { verificationViewModel.saveResultToVault() },
                                onOpenSavedLc = { lcId -> navController.navigate("lc_detail/$lcId") },
                                onReset = { verificationViewModel.reset() }
                            )
                        }

                        composable(ROUTE_COMPLIANCE) {
                            val state by complianceViewModel.uiState.collectAsStateWithLifecycle()
                            ComplianceHubScreen(
                                uiState = state,
                                onSelectTab = { complianceViewModel.selectTab(it) },
                                onUpdateSearch = { complianceViewModel.updateSearchQuery(it) },
                                onSelectFilterChip = { complianceViewModel.selectFilterChip(it) },
                                onQuickScreenQueryChange = { complianceViewModel.updateQuickScreenQuery(it) },
                                onRunQuickScreen = { complianceViewModel.executeQuickScreen() },
                                onClearQuickScreen = { complianceViewModel.clearQuickScreen() },
                                onUpdateLimit = { limit, cap, pct, active ->
                                    complianceViewModel.saveUpdatedLimit(limit, cap, pct, active)
                                },
                                onAddLimit = { scope, name, code, cap, pct ->
                                    complianceViewModel.addCustomLimit(scope, name, code, cap, pct)
                                }
                            )
                        }

                        composable(ROUTE_VAULT) {
                            val state by vaultViewModel.uiState.collectAsStateWithLifecycle()
                            val integrity by vaultViewModel.integrityState.collectAsStateWithLifecycle()
                            VaultScreen(
                                uiState = state,
                                integrityState = integrity,
                                onUpdateSearch = { vaultViewModel.updateSearchQuery(it) },
                                onSelectStatusFilter = { vaultViewModel.selectStatusFilter(it) },
                                onSelectLc = { vaultViewModel.selectLc(it) },
                                onOpenLcDetail = { lcId ->
                                    vaultViewModel.resetIntegrityCheck()
                                    navController.navigate("lc_detail/$lcId")
                                },
                                onVerifyHash = { vaultViewModel.reverifyDocumentHash(it) },
                                onDeleteLc = { vaultViewModel.deleteLc(it) }
                            )
                        }

                        composable(
                            route = ROUTE_LC_DETAIL,
                            arguments = listOf(navArgument("lcId") { type = NavType.LongType })
                        ) { entry ->
                            val lcId = entry.arguments?.getLong("lcId") ?: -1L
                            val lc by vaultViewModel.observeLc(lcId).collectAsStateWithLifecycle(initialValue = null)
                            val integrity by vaultViewModel.integrityState.collectAsStateWithLifecycle()
                            LaunchedEffect(lcId) {
                                vaultViewModel.resetIntegrityCheck()
                            }
                            LcDetailScreen(
                                lc = lc,
                                integrityState = integrity,
                                onBack = { navController.popBackStack() },
                                onVerifyHash = { vaultViewModel.reverifyDocumentHash(it) },
                                onDeleteLc = { vaultViewModel.deleteLc(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
