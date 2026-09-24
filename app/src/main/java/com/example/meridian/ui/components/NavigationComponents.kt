package com.example.meridian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meridian.data.model.ModuleRoute

data class NavItem(
    val route: ModuleRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeridianTopAppBar(
    currentRoute: ModuleRoute,
    onOpenCommandPalette: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onOpenQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = "Meridian",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MERIDIAN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentRoute.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onOpenCommandPalette,
                modifier = Modifier.testTag("command_palette_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search & Commands",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onOpenThemeDialog,
                modifier = Modifier.testTag("theme_selector_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = "Change Theme",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            FilledIconButton(
                onClick = onOpenQuickAdd,
                modifier = Modifier.testTag("quick_add_header_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Add"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@Composable
fun MeridianBottomNavigationBar(
    currentRoute: ModuleRoute,
    onNavigate: (ModuleRoute) -> Unit,
    journalBadge: String? = null,
    recoveryBadge: String? = null,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(ModuleRoute.OVERVIEW, "Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        NavItem(ModuleRoute.TIMELINE, "Timeline", Icons.Filled.History, Icons.Outlined.History),
        NavItem(ModuleRoute.JOURNAL, "Logbook", Icons.Filled.Book, Icons.Outlined.Book, journalBadge),
        NavItem(ModuleRoute.STUDY, "Study", Icons.Filled.School, Icons.Outlined.School),
        NavItem(ModuleRoute.RECOVERY, "Unbound", Icons.Filled.Shield, Icons.Outlined.Shield, recoveryBadge),
        NavItem(ModuleRoute.FINANCE, "Finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        NavItem(ModuleRoute.PULSE, "Pulse", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        NavItem(ModuleRoute.GOALS, "Goals", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .testTag("bottom_nav_bar")
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (!item.badge.isNullOrBlank()) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(item.badge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                    unselectedTextColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
