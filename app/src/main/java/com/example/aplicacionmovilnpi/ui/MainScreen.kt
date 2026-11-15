package com.example.aplicacionmovilnpi.ui

import com.example.aplicacionmovilnpi.ui.providers.LocalIsEnglish

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.feature.academic.data.FacultyData
import com.example.aplicacionmovilnpi.feature.settings.ui.AccessibilityScreen
import com.example.aplicacionmovilnpi.feature.dashboard.ui.CalendarScreen
import com.example.aplicacionmovilnpi.feature.academic.ui.GradesScreen
import com.example.aplicacionmovilnpi.feature.settings.ui.HelpScreen
import com.example.aplicacionmovilnpi.feature.dashboard.ui.HomeScreen
import com.example.aplicacionmovilnpi.feature.map.ui.MapScreen
import com.example.aplicacionmovilnpi.feature.dashboard.ui.MessagesScreen
import com.example.aplicacionmovilnpi.feature.settings.ui.MoreScreen
import com.example.aplicacionmovilnpi.feature.academic.ui.ProfessorsScreen
import com.example.aplicacionmovilnpi.feature.settings.ui.ProfileScreen
import com.example.aplicacionmovilnpi.feature.academic.ui.ScheduleScreen
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.PageBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

/**
 * Pestañas principales de navegación inferior de la aplicación.
 *
 * @property id Identificador clave de la pestaña.
 * @property spanishLabel Etiqueta en idioma español.
 * @property englishLabel Etiqueta en idioma inglés.
 * @property icon Icono vectorial de Material Design.
 */
enum class NavTab(val id: String, val spanishLabel: String, val englishLabel: String, val icon: ImageVector) {
    /** Pestaña principal de Inicio */
    HOME("home", "Inicio", "Home", Icons.Rounded.Home),
    /** Pestaña del horario semanal del estudiante */
    SCHEDULE("schedule", "Horario", "Schedule", Icons.Rounded.CalendarToday),
    /** Pestaña del directorio de profesorado y tutorías */
    PROFESSORS("professors", "Profesorado", "Faculty", Icons.Rounded.School),
    /** Pestaña del mapa y planos de navegación indoor */
    MAP("map", "Mapa", "Map", Icons.Rounded.Map),
    /** Pestaña con opciones secundarias y configuración */
    MORE("more", "Más", "More", Icons.Rounded.Menu)
}

/**
 * Pantalla contenedora principal de la aplicación.
 *
 * Administra la navegación entre pestañas inferiores, el renderizado de subpantallas,
 * el encabezado institucional UGR con menú de usuario y las configuraciones de accesibilidad e idioma.
 *
 * @param onLogout Callback para cerrar la sesión del estudiante.
 * @param onLanguageChange Callback para alternar el idioma de la aplicación.
 * @param isLargeText Indica si está activa la preferencia de texto grande.
 * @param onLargeTextChange Callback para actualizar el estado de texto grande.
 * @param isHighContrast Indica si está activo el modo de alto contraste.
 * @param onHighContrastChange Callback para actualizar el estado de alto contraste.
 * @param defaultAccessibleRoute Indica si las rutas asignadas por defecto deben ser accesibles.
 * @param onDefaultAccessibleRouteChange Callback para actualizar las rutas accesibles por defecto.
 */
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onLanguageChange: (Boolean) -> Unit,
    isLargeText: Boolean = false,
    onLargeTextChange: (Boolean) -> Unit = {},
    isHighContrast: Boolean = false,
    onHighContrastChange: (Boolean) -> Unit = {},
    defaultAccessibleRoute: Boolean = false,
    onDefaultAccessibleRouteChange: (Boolean) -> Unit = {}
) {
    // 1. Estado de la pestaña activa en la barra inferior
    var activeTab by rememberSaveable { mutableStateOf(NavTab.HOME) }

    // 2. Clave para forzar la actualización/reubicación al entrar al mapa
    var mapEntryKey by rememberSaveable { mutableIntStateOf(0) }

    // 3. Subpantalla secundaria seleccionada desde el menú "Más" o perfil (null = pantalla principal de la pestaña)
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

    // 4. Estado de visibilidad del menú desplegable del avatar en el encabezado
    var showMenu by remember { mutableStateOf(false) }

    // 5. Destino POI seleccionado para guiar directamente al mapa (null = mapa libre)
    var mapDestination by rememberSaveable { mutableStateOf<String?>(null) }

    // Función auxiliar para abrir el mapa y centrar la ruta hacia un POI
    val openMap: (String?) -> Unit = { poiId ->
        mapDestination = poiId
        mapEntryKey++
        activeTab = NavTab.MAP
        currentSubScreen = null
    }

    // 6. Estructura Scaffold con cabecera y barra de navegación inferior
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
                isEnglish = LocalIsEnglish.current,
                onTabSelected = { tab ->
                    // Cada pulsación en Mapa fuerza una nueva comprobación de ubicación
                    if (tab == NavTab.MAP) {
                        mapEntryKey++
                    }

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
            // 7. Renderizado condicional de subpantallas secundarias o pestañas principales
            when {
                currentSubScreen == "profile" -> ProfileScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "grades" -> GradesScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "calendar" -> CalendarScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "messages" -> MessagesScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "help" -> HelpScreen(onBack = { currentSubScreen = null })
                currentSubScreen == "accessibility" ->
                    AccessibilityScreen(
                        onBack = { currentSubScreen = null },
                        isLargeText = isLargeText,
                        onLargeTextChange = onLargeTextChange,
                        isHighContrast = isHighContrast,
                        onHighContrastChange = onHighContrastChange,
                        defaultAccessibleRoute = defaultAccessibleRoute,
                        onDefaultAccessibleRouteChange = onDefaultAccessibleRouteChange
                    )

                activeTab == NavTab.HOME -> HomeScreen(
                    onNavigateToSchedule = {
                        activeTab = NavTab.SCHEDULE
                        currentSubScreen = null
                    },
                    onNavigateToProfessors = {
                        activeTab = NavTab.PROFESSORS
                        currentSubScreen = null
                    },
                    onNavigateToMap = openMap,
                    onNavigateToCalendar = {
                        activeTab = NavTab.MORE
                        currentSubScreen = "calendar"
                    }
                )

                activeTab == NavTab.SCHEDULE -> ScheduleScreen(onNavigateToMap = openMap)
                activeTab == NavTab.PROFESSORS -> ProfessorsScreen(onNavigateToMap = openMap)
                activeTab == NavTab.MAP -> MapScreen(
                    initialDestPoiId = mapDestination,
                    mapEntryKey = mapEntryKey,
                    defaultAccessibleRoute = defaultAccessibleRoute
                )

                activeTab == NavTab.MORE -> MoreScreen(
                    onNavigateToSubScreen = { subScreen -> currentSubScreen = subScreen },
                    onLanguageChange = onLanguageChange
                )
            }
        }
    }
}

/**
 * Cabecera institucional superior con marca corporativa UGR y avatar de perfil de usuario.
 *
 * @param onAvatarClick Callback al pulsar sobre las iniciales/avatar del estudiante.
 * @param showMenu Controla la visibilidad del menú desplegable del usuario.
 * @param onDismissMenu Callback para cerrar el menú desplegable.
 * @param onProfileClick Callback para navegar al perfil de usuario.
 * @param onLogoutClick Callback para cerrar la sesión.
 */
@Composable
fun UgrHeader(
    onAvatarClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // 1. Superficie superior adaptada al insets de la barra de estado (Edge-to-Edge)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 6.dp)
                .height(64.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logotipo distintivo UGR
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

            // Textos institucionales del centro y campus
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

            // 2. Avatar circular con las iniciales del estudiante y menú desplegable de usuario
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

                // Menú emergente de usuario
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = FacultyData.Student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = FacultyData.Student.email,
                                    fontSize = 10.sp,
                                    color = MutedText
                                )
                            }
                        },
                        onClick = onProfileClick
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        text = { Text(text = if (LocalIsEnglish.current) "My Profile" else "Mi Perfil", fontSize = 12.sp) },
                        onClick = onProfileClick
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                tint = UgrRed,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        text = {
                            Text(
                                text = if (LocalIsEnglish.current) "Log out" else "Cerrar sesión",
                                fontSize = 12.sp,
                                color = UgrRed,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = onLogoutClick
                    )
                }
            }
        }
    }
}

/**
 * Barra de navegación inferior principal con pestañas e iconos adaptados al idioma.
 *
 * @param activeTab Identificador de la pestaña activa actualmente.
 * @param isEnglish Indica si se presenta la etiqueta en idioma inglés.
 * @param onTabSelected Callback ejecutado al presionar sobre cualquier elemento de la barra.
 */
@Composable
fun UgrBottomBar(
    activeTab: NavTab,
    isEnglish: Boolean,
    onTabSelected: (NavTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavTab.entries.forEach { tab ->
            val selected = activeTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = if (isEnglish) tab.englishLabel else tab.spanishLabel,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = if (isEnglish) tab.englishLabel else tab.spanishLabel,
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
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
