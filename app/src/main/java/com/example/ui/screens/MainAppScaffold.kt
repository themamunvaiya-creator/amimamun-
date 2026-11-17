package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.local.QuranDatabase
import com.example.data.remote.AlQuranApiService
import com.example.data.repository.OrgRepository
import com.example.data.repository.QuranRepository
import com.example.ui.auth.signOutUser
import com.example.ui.viewmodel.DuaViewModel
import com.example.ui.viewmodel.OrgViewModel
import com.example.ui.viewmodel.QuranViewModel
import com.example.util.BengaliFormatters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

sealed class MainRoute(
    val route: String,
    val labelBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : MainRoute("home", "হোম", Icons.Filled.Home, Icons.Outlined.Home)
    data object Members : MainRoute("members", "সদস্য", Icons.Filled.Groups, Icons.Outlined.Groups)
    data object Donations : MainRoute("donations", "দান হিসাব", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
    data object Activities : MainRoute("activities", "কার্যক্রম", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object IslamicHub : MainRoute("islamic_hub", "কোরআন-দোয়া", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
}

private val MAIN_DESTINATIONS = listOf(
    MainRoute.Home,
    MainRoute.Members,
    MainRoute.Donations,
    MainRoute.Activities,
    MainRoute.IslamicHub
)

@Composable
fun MainAppScaffold(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val orgViewModel: OrgViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                OrgViewModel(
                    repository = OrgRepository(db),
                    currentUserId = currentUserId,
                    currentUserDisplayName = userDisplayName
                )
            }
        }
    )

    val quranViewModel: QuranViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val dao = QuranDatabase.getInstance(app).quranCacheDao()
                QuranViewModel(QuranRepository(AlQuranApiService.create(), dao))
            }
        }
    )

    val duaViewModel: DuaViewModel = viewModel()

    val membersState by orgViewModel.membersState.collectAsStateWithLifecycle()
    val donationsState by orgViewModel.donationsState.collectAsStateWithLifecycle()
    val financialSummary by orgViewModel.financialSummary.collectAsStateWithLifecycle()
    val activitiesState by orgViewModel.activitiesState.collectAsStateWithLifecycle()
    val unreadCount by orgViewModel.unreadNoticeCount.collectAsStateWithLifecycle()
    val bookmarksState by orgViewModel.quranBookmarksState.collectAsStateWithLifecycle()
    val favoritesState by orgViewModel.duaFavoritesState.collectAsStateWithLifecycle()
    val tasbihState by orgViewModel.tasbihState.collectAsStateWithLifecycle()
    val isSaving by orgViewModel.isSaving.collectAsStateWithLifecycle()

    LaunchedEffect(orgViewModel) {
        orgViewModel.feedbackMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainRoute.Home.route
    var islamicTab by remember { mutableStateOf(IslamicHubTab.QURAN) }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        MAIN_DESTINATIONS.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    val iconVec = if (selected) dest.selectedIcon else dest.unselectedIcon
                                    if (dest == MainRoute.Activities && unreadCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge {
                                                    Text(BengaliFormatters.toBengaliNumber(unreadCount))
                                                }
                                            }
                                        ) {
                                            Icon(iconVec, contentDescription = dest.labelBn)
                                        }
                                    } else {
                                        Icon(iconVec, contentDescription = dest.labelBn)
                                    }
                                },
                                label = {
                                    Text(
                                        text = dest.labelBn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_${dest.route}")
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
                if (useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        MAIN_DESTINATIONS.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    val iconVec = if (selected) dest.selectedIcon else dest.unselectedIcon
                                    if (dest == MainRoute.Activities && unreadCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge {
                                                    Text(BengaliFormatters.toBengaliNumber(unreadCount))
                                                }
                                            }
                                        ) {
                                            Icon(iconVec, contentDescription = dest.labelBn)
                                        }
                                    } else {
                                        Icon(iconVec, contentDescription = dest.labelBn)
                                    }
                                },
                                label = { Text(dest.labelBn) },
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = MainRoute.Home.route
                    ) {
                        composable(MainRoute.Home.route) {
                            HomeScreen(
                                userName = userDisplayName,
                                userEmail = userEmail,
                                membersState = membersState,
                                financialSummary = financialSummary,
                                activitiesState = activitiesState,
                                unreadCount = unreadCount,
                                onNavigateMembers = { navigateTo(MainRoute.Members.route) },
                                onNavigateDonations = { navigateTo(MainRoute.Donations.route) },
                                onNavigateActivities = { navigateTo(MainRoute.Activities.route) },
                                onNavigateQuran = {
                                    islamicTab = IslamicHubTab.QURAN
                                    navigateTo(MainRoute.IslamicHub.route)
                                },
                                onNavigateDuas = {
                                    islamicTab = IslamicHubTab.DUAS
                                    navigateTo(MainRoute.IslamicHub.route)
                                },
                                onNavigateTasbih = {
                                    islamicTab = IslamicHubTab.TASBIH_ZAKAT
                                    navigateTo(MainRoute.IslamicHub.route)
                                },
                                onSignOut = {
                                    signOutUser(credentialManager, scope)
                                }
                            )
                        }
                        composable(MainRoute.Members.route) {
                            MembersScreen(
                                membersState = membersState,
                                isSaving = isSaving,
                                onSaveMember = { member, done -> orgViewModel.saveMember(member, done) },
                                onDeleteMember = { id -> orgViewModel.deleteMember(id) }
                            )
                        }
                        composable(MainRoute.Donations.route) {
                            DonationsScreen(
                                donationsState = donationsState,
                                summary = financialSummary,
                                isSaving = isSaving,
                                onSaveDonation = { rec, done -> orgViewModel.saveDonation(rec, done) },
                                onDeleteDonation = { id -> orgViewModel.deleteDonation(id) }
                            )
                        }
                        composable(MainRoute.Activities.route) {
                            ActivitiesScreen(
                                activitiesState = activitiesState,
                                isSaving = isSaving,
                                onSaveActivity = { act, done -> orgViewModel.saveActivity(act, done) },
                                onMarkRead = { id, read -> orgViewModel.markActivityRead(id, read) },
                                onDeleteActivity = { id -> orgViewModel.deleteActivity(id) },
                                onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                            )
                        }
                        composable(MainRoute.IslamicHub.route) {
                            IslamicHubScreen(
                                selectedTab = islamicTab,
                                onSelectTab = { islamicTab = it },
                                quranViewModel = quranViewModel,
                                duaViewModel = duaViewModel,
                                bookmarksState = bookmarksState,
                                favoritesState = favoritesState,
                                tasbihState = tasbihState,
                                onToggleQuranBookmark = { sNo, aNo, sBn, sAr, tAr, tBn, marked ->
                                    orgViewModel.toggleQuranBookmark(sNo, aNo, sBn, sAr, tAr, tBn, marked)
                                },
                                onToggleDuaFavorite = { dId, titleBn, cat, fav ->
                                    orgViewModel.toggleDuaFavorite(dId, titleBn, cat, fav)
                                },
                                onUpdateTasbih = { dhikr, cur, target, life ->
                                    orgViewModel.updateTasbih(dhikr, cur, target, life)
                                },
                                onShowFeedback = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                            )
                        }
                    }
                }
            }
        }
    }
}
