package com.example.aplicacionmovilnpi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.ui.screens.AccessibilityScreen
import com.example.aplicacionmovilnpi.ui.screens.CalendarScreen
import com.example.aplicacionmovilnpi.ui.screens.GradesScreen
import com.example.aplicacionmovilnpi.ui.screens.HelpScreen
import com.example.aplicacionmovilnpi.ui.screens.HomeScreen
import com.example.aplicacionmovilnpi.ui.screens.MapScreen
import com.example.aplicacionmovilnpi.ui.screens.MessagesScreen
import com.example.aplicacionmovilnpi.ui.screens.MoreScreen
import com.example.aplicacionmovilnpi.ui.screens.ProfessorsScreen
import com.example.aplicacionmovilnpi.ui.screens.ProfileScreen
import com.example.aplicacionmovilnpi.ui.screens.ScheduleScreen
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.PageBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

enum class NavTab(val id: String, val label: String, val icon: String) {
    HOME("home", "Inicio", "🏠"),
    SCHEDULE("schedule", "Horario", "📅"),
    PROFESSORS("professors", "Profesorado", "👨‍🏫"),
    MAP("map", "Mapa", "🗺️"),
    MORE("more", "Más", "☰")
}

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf(NavTab.HOME) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    // POI al que debe guiar el mapa al abrirlo desde otra pantalla (null = mapa sin ruta)
    var mapDestination by remember { mutableStateOf<String?>(null) }
    val openMap: (String?) -> Unit = { poiId ->
        mapDestination = poiId
        activeTab = NavTab.MAP
        currentSubScreen = null
    }

    Scaffold(
        topBar = {
            UgrHeader(
                onAvatarClick = { showMenu = true },
                showMenu = showMenu,
                onDismissMenu = { showMenu = false },
                onProfileClick = {
                    showMenu = false
                    currentSubScreen = "profile"
                },
                onLogoutClick = {
                    showMenu = false
                    onLogout()
                }
            )
        },
        bottomBar = {
            UgrBottomBar(
                activeTab = activeTab,
                onTabSelected = { tab ->
                    activeTab = tab
                    currentSubScreen = null
                    mapDestination = null
                }
            )
        },
        containerColor = PageBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                currentSubScreen == "profile" -> ProfileScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "grades" -> GradesScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "calendar" -> CalendarScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "messages" -> MessagesScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "help" -> HelpScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "accessibility" -> AccessibilityScreen(onBack = { currentSubScreen = null })

                activeTab == NavTab.HOME -> HomeScreen(
                    onNavigateToSchedule = { activeTab = NavTab.SCHEDULE; currentSubScreen = null },
                    onNavigateToProfessors = { activeTab = NavTab.PROFESSORS; currentSubScreen = null },
                    onNavigateToMap = openMap,
                    onNavigateToCalendar = { activeTab = NavTab.MORE; currentSubScreen = "calendar" }
                )
                activeTab == NavTab.SCHEDULE -> ScheduleScreen(onNavigateToMap = openMap)
                activeTab == NavTab.PROFESSORS -> ProfessorsScreen(onNavigateToMap = openMap)
                activeTab == NavTab.MAP -> MapScreen(initialDestPoiId = mapDestination)
                activeTab == NavTab.MORE -> MoreScreen(
                    onNavigateToSubScreen = { subScreen -> currentSubScreen = subScreen }
                )
            }
        }
    }
}

@Composable
fun UgrHeader(
    onAvatarClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo Mark
            Surface(
                color = UgrRed,
                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 12.dp, bottomEnd = 6.dp),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "UGR",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "ETSIIT · UGR",
                    fontSize = 10.sp,
                    color = MutedText
                )
                Text(
                    text = "UGR CAMPUS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // User Avatar Circle
            Box {
                Surface(
                    color = Color(0xFFEEE4EA),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onAvatarClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = FacultyData.Student.initials,
                            color = Color(0xFF5E3850),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = FacultyData.Student.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = FacultyData.Student.email, fontSize = 10.sp, color = MutedText)
                            }
                        },
                        onClick = onProfileClick
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(text = "👤 Mi Perfil", fontSize = 12.sp) },
                        onClick = onProfileClick
                    )
                    DropdownMenuItem(
                        text = { Text(text = "🚪 Cerrar sesión", fontSize = 12.sp, color = UgrRed, fontWeight = FontWeight.Bold) },
                        onClick = onLogoutClick
                    )
                }
            }
        }
    }
}

@Composable
fun UgrBottomBar(
    activeTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(72.dp)
    ) {
        NavTab.entries.forEach { tab ->
            val selected = activeTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Text(text = tab.icon, fontSize = 18.sp) },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UgrRed,
                    selectedTextColor = UgrRed,
                    unselectedIconColor = MutedText,
                    unselectedTextColor = MutedText,
                    indicatorColor = Color(0xFFFCEDF0)
                )
            )
        }
    }
}
