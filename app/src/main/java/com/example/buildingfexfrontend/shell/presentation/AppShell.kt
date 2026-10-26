package com.example.buildingfexfrontend.shell.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.buildingfexfrontend.R
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.AppLanguage
import com.example.buildingfexfrontend.core.i18n.Language
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.dashboard.presentation.AdminDashboardScreen
import com.example.buildingfexfrontend.finances.presentation.CollectionsScreen
import com.example.buildingfexfrontend.finances.presentation.FinanceScreen
import com.example.buildingfexfrontend.imports.presentation.ImportsScreen
import com.example.buildingfexfrontend.incidents.presentation.AdminIncidentsScreen
import com.example.buildingfexfrontend.information.presentation.InformationScreen
import com.example.buildingfexfrontend.residents.presentation.ResidentsScreen
import com.example.buildingfexfrontend.settings.presentation.SettingsScreen
import com.example.buildingfexfrontend.socialspaces.presentation.GenerationScreen
import com.example.buildingfexfrontend.socialspaces.presentation.SpacesScreen
import com.example.buildingfexfrontend.support.presentation.AdminSupportScreen
import com.example.buildingfexfrontend.team.presentation.TeamScreen
import com.example.buildingfexfrontend.ui.theme.BfPrimary
import kotlinx.coroutines.launch

private data class ShellItem(
    val id: String,
    val titleKey: String,
    val icon: ImageVector,
    val screen: @Composable (AppContainer) -> Unit,
)

/** Owns the ViewModels of a single drawer destination (like a web route). */
private class DestinationViewModelStore : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

private val AdminItems = listOf(
    ShellItem("admin_dashboard", "shell.nav.dashboard", Icons.Outlined.Dashboard, { AdminDashboardScreen(it) }),
    ShellItem("admin_residents", "shell.nav.residents", Icons.Outlined.People, { ResidentsScreen(it) }),
    ShellItem("admin_spaces", "shell.nav.admin_spaces", Icons.Outlined.MeetingRoom, { SpacesScreen(it) }),
    ShellItem("admin_imports", "shell.nav.imports", Icons.Outlined.Upload, { ImportsScreen(it) }),
    ShellItem("admin_finance", "shell.nav.finances", Icons.Outlined.AccountBalanceWallet, { FinanceScreen(it) }),
    ShellItem("admin_generation", "shell.nav.generation", Icons.Outlined.CalendarMonth, { GenerationScreen(it) }),
    ShellItem("admin_incidents", "shell.nav.incidents", Icons.Outlined.Flag, { AdminIncidentsScreen(it) }),
    ShellItem("admin_information", "shell.nav.information", Icons.Outlined.Campaign, { InformationScreen(it) }),
    ShellItem("admin_support", "shell.nav.admin_support", Icons.Outlined.Forum, { AdminSupportScreen(it) }),
    ShellItem("admin_collections", "shell.nav.admin_collections", Icons.Outlined.TrendingUp, { CollectionsScreen(it) }),
    ShellItem("admin_team", "shell.nav.team", Icons.Outlined.Badge, { TeamScreen(it) }),
    ShellItem("settings", "shell.nav.settings", Icons.Outlined.Settings, { SettingsScreen(it) }),
)

/**
 * Root shell: lateral drawer (like the web sidebar) + top bar.
 * The drawer items mirror `AppShellView.vue`: admin menu (this app is admin-only;
 * residents use the Flutter resident app).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(container: AppContainer) {
    val session by container.session.session.collectAsStateWithLifecycle()
    val items = AdminItems
    val language by AppLanguage.flow.collectAsStateWithLifecycle()

    var selectedId by rememberSaveable { mutableStateOf(items.first().id) }
    val selected = items.firstOrNull { it.id == selectedId } ?: items.first()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                ) {
                    ShellHeader(
                        name = session?.profile?.name.orEmpty(),
                        email = session?.profile?.email.orEmpty(),
                        role = string("shell.role.admin"),
                    )
                    items.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(string(item.titleKey)) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            selected = item.id == selected.id,
                            onClick = {
                                selectedId = item.id
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = string("shell.language") + ": " +
                                    if (language == Language.EN) "English" else "Español",
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        icon = { Icon(Icons.Outlined.Language, contentDescription = null) },
                        selected = false,
                        onClick = { AppLanguage.toggle() },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                    NavigationDrawerItem(
                        label = { Text(string("shell.logout")) },
                        icon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                        selected = false,
                        onClick = { container.session.clear() },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(string(selected.titleKey), fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = string("shell.openMenu"))
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Fresh ViewModels per drawer destination (mirrors the web route remount):
                // each section reloads in `init { load() }` every time it is entered.
                val storeOwner = remember(selected.id) { DestinationViewModelStore() }
                DisposableEffect(selected.id) {
                    val owner = storeOwner
                    onDispose { owner.viewModelStore.clear() }
                }
                CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
                    key(selected.id) { selected.screen(container) }
                }
            }
        }
    }
}

@Composable
private fun ShellHeader(name: String, email: String, role: String) {
    Column(modifier = Modifier.padding(20.dp)) {
        Image(
            painter = painterResource(R.drawable.logo_buildingfex),
            contentDescription = string("shell.appName"),
            modifier = Modifier
                .width(150.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit,
        )
        if (name.isNotBlank()) {
            Text(
                text = name,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (email.isNotBlank()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = role,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = BfPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
