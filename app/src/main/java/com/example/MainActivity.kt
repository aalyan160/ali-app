package com.example
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import android.app.TimePickerDialog
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.IntrinsicSize
import org.json.JSONObject
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.collectAsState
import io.github.jan.supabase.gotrue.SessionStatus
import android.content.ContextWrapper
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
class MainActivity : ComponentActivity() {
    lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID")
            .requestEmail()
            .build()
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkModePref by mainViewModel.isDarkMode.observeAsState()
            val darkTheme = isDarkModePref ?: androidx.compose.foundation.isSystemInDarkTheme()
            val usePurpleTheme = mainViewModel.isRewardUnlocked("custom_theme")
            MyApplicationTheme(darkTheme = darkTheme, usePurpleTheme = usePurpleTheme) {
                val context = LocalContext.current
                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        } else true
                    )
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasNotificationPermission = isGranted
                }
                
                LaunchedEffect(Unit) {
                    NotificationHelper.createNotificationChannel(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val commitmentSet by mainViewModel.commitmentSet.observeAsState(initial = false)
                val authStatus by mainViewModel.currentSessionStatus.collectAsState(initial = SessionStatus.LoadingFromStorage)
                var isSplashFinished by remember { mutableStateOf(false) }
                var onboardingCompleted by remember { 
                    mutableStateOf(context.getSharedPreferences("lockedin_prefs", Context.MODE_PRIVATE).getBoolean("onboarding_complete", false)) 
                }
                // Animation logic handled inside SplashScreen
                if (!isSplashFinished) {
                    SplashScreen(onFinished = { isSplashFinished = true })
                } else if (!onboardingCompleted) {
                    OnboardingScreen(onComplete = {
                        context.getSharedPreferences("lockedin_prefs", Context.MODE_PRIVATE).edit().putBoolean("onboarding_complete", true).apply()
                        onboardingCompleted = true
                    })
                } else {
                    when (authStatus) {
                        is SessionStatus.LoadingFromStorage -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF00DFC1))
                            }
                        }
                        is SessionStatus.Authenticated -> {
                            val navController = rememberNavController()
                            val isStartStudy = intent.getBooleanExtra("START_STUDY", false)
                            val startDest = if (isStartStudy) "study" else "home"
                            if (commitmentSet) {
                                LockedInApp(navController = navController, viewModel = mainViewModel, startDestination = startDest)
                            } else {
                                CommitmentSetupScreen(viewModel = mainViewModel)
                            }
                        }
                        else -> {
                            var showPrivacyPolicy by remember { mutableStateOf(false) }
                            if (showPrivacyPolicy) {
                                PrivacyPolicyScreen(onBack = { showPrivacyPolicy = false })
                            } else {
                                LoginSignupScreen(
                                    viewModel = mainViewModel,
                                    onPrivacyPolicyClick = { showPrivacyPolicy = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockedInApp(
    navController: NavHostController,
    viewModel: MainViewModel,
    startDestination: String = "home"
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOCKEDIN",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (currentRoute == "dashboard") {
                        IconButton(
onClick = { navController.navigate("profile") }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
   ),
                modifier = Modifier.height(52.dp).border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF121414),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0xFF00DFC1),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
            ) {
                val items = listOf(
                    Triple("home", "Home", Icons.Default.Home),
                    Triple("study", "Study", Icons.Default.PlayArrow),
                    Triple("plan", "Plan", Icons.Default.DateRange),
                    Triple("ai", "AI", Icons.Default.Star),
                    Triple("profile", "Profile", Icons.Default.Person)
                )
                items.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, contentDescription = label)
                                if (currentRoute == route) {
                                    Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, shape = CircleShape))
                                }
                            }
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00DFC1),
                            selectedTextColor = Color(0xFF00DFC1),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_$route")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel, navController = navController)
            }
            composable("study") {
                StudyScreen(viewModel = viewModel)
            }
            composable("tasks") {
                TasksScreen(viewModel = viewModel)
            }
            composable("plan") {
                PlanScreen(viewModel = viewModel, navController = navController)
            }
            composable("notes") {
                NotesScreen(viewModel = viewModel)
            }
            composable("profile") {
                ProfileScreen(viewModel = viewModel, navController = navController)
            }
            composable("stats") {
                StatsScreen(viewModel = viewModel, navController = navController)
            }
            composable("session_history") {
                SessionHistoryScreen(viewModel = viewModel, navController = navController)
            }
            composable("achievements") {
                AchievementsScreen(viewModel = viewModel, navController = navController)
            }
            composable("leaderboard") {
                LeaderboardScreen(viewModel = viewModel, navController = navController)
            }
            composable("xp_leaderboard") {
                XpLeaderboardScreen(viewModel = viewModel, navController = navController)
            }
            composable("rewards_shop") {
                RewardsShopScreen(viewModel = viewModel, navController = navController)
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel, navController = navController)
            }
            composable("privacy_policy") {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable("ai") {
                AiChatScreen(viewModel = viewModel)
            }
        }
    }
}
// --- Leaderboard ---
@Composable
fun LeaderboardScreen(viewModel: MainViewModel, navController: NavController) {
    UnifiedLeaderboardScreen(viewModel = viewModel, navController = navController, initialTab = 0)
}

@Composable
fun XpLeaderboardScreen(viewModel: MainViewModel, navController: NavController) {
    UnifiedLeaderboardScreen(viewModel = viewModel, navController = navController, initialTab = 1)
}

@Composable
fun UnifiedLeaderboardScreen(viewModel: MainViewModel, navController: NavController, initialTab: Int = 0) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val weeklyLeaderboard by viewModel.leaderboardList.observeAsState(initial = emptyList())
    val xpLeaderboard by viewModel.xpLeaderboardList.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchLeaderboard()
        viewModel.fetchXpLeaderboard()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFFE2E2E2))
            }
            Text("Leaderboards", color = Color(0xFFE2E2E2), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Custom Segmented Switcher for Weekly vs Top XP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2020), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) Color(0xFF00DFC1) else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Weekly Study",
                    color = if (selectedTab == 0) Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) Color(0xFF00DFC1) else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Top XP Leaders",
                    color = if (selectedTab == 1) Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentList = if (selectedTab == 0) weeklyLeaderboard else xpLeaderboard

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No data found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentList.size) { index ->
                    val user = currentList[index]
                    val rank = index + 1
                    
                    val isCurrentUser = when (user) {
                        is LeaderboardUser -> user.isCurrentUser
                        is XpLeaderboardUser -> user.isCurrentUser
                        else -> false
                    }
                    val initials = when (user) {
                        is LeaderboardUser -> user.initials
                        is XpLeaderboardUser -> user.initials
                        else -> ""
                    }
                    val username = when (user) {
                        is LeaderboardUser -> user.username
                        is XpLeaderboardUser -> user.username
                        else -> ""
                    }
                    val scoreText = when (user) {
                        is LeaderboardUser -> "${user.weeklyMinutes} min"
                        is XpLeaderboardUser -> "${user.xp} XP"
                        else -> ""
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                if (isCurrentUser) BorderStroke(1.dp, Color(0xFF00DFC1)) else BorderStroke(0.0.dp, Color.Transparent),
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rank.toString(),
                                color = when (rank) {
                                    1 -> Color(0xFFFFD700)
                                    2 -> Color(0xFFC0C0C0)
                                    3 -> Color(0xFFCD7F32)
                                    else -> Color.Gray
                                },
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(30.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                username,
                                color = Color(0xFFE2E2E2),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                scoreText,
                                color = if (selectedTab == 1) Color(0xFF00DFC1) else Color.Gray,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
// --- Rewards Shop ---
data class Reward(
    val id: String,
    val name: String,
    val xpCost: Int,
    val icon: ImageVector
)
@Composable
fun RewardsShopScreen(viewModel: MainViewModel, navController: NavController) {
    val userXp by viewModel.userXP.observeAsState(initial = 0)
    
    val rewards = listOf(
        Reward("custom_theme", "Custom Theme", 200, Icons.Default.Edit),
        Reward("profile_badge", "Profile Badge", 500, Icons.Default.Star),
        Reward("exclusive_sounds", "Exclusive Sounds", 800, Icons.Default.PlayArrow)
    )
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFFE2E2E2))
            }
            Text("Rewards Shop", color = Color(0xFFE2E2E2), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(52.dp).weight(1f))
            Text("XP: $userXp", color = Color(0xFF00DFC1), fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Complete sessions to earn XP and unlock rewards",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(rewards) { reward ->
                val isUnlocked = viewModel.isRewardUnlocked(reward.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(reward.icon, reward.name, tint = Color(0xFF00DFC1), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reward.name, color = Color(0xFFE2E2E2), fontWeight = FontWeight.Bold)
                            Text("${reward.xpCost} XP", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { (userXp.toFloat() / reward.xpCost).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                color = Color(0xFF00DFC1),
                                trackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        }
                        if (isUnlocked) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF00DFC1).copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, "Unlocked", tint = Color(0xFF00DFC1), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unlocked", color = Color(0xFF00DFC1), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else if (userXp < reward.xpCost) {
                            Text(
                                text = "Not enough XP",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        } else {
                            Button(
                                modifier = Modifier.width(110.dp).height(40.dp),
                                onClick = { 
                                    if (!isUnlocked && viewModel.deductXP(reward.xpCost)) {
                                        viewModel.unlockReward(reward.id)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00DFC1))
   ) {
                                Text("Unlock", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SettingsScreen(viewModel: MainViewModel, navController: NavController) {
    val notificationSound by viewModel.notificationSound.observeAsState(initial = true)
    val dailyReminderTime by viewModel.dailyReminderTime.observeAsState(initial = null)
    val breakReminder by viewModel.breakReminder.observeAsState(initial = true)
    val studyCommitmentDuration by viewModel.studyCommitmentDuration.observeAsState(initial = 1)
    val appLanguage by viewModel.appLanguage.observeAsState(initial = "English")
    val userName by viewModel.userName.observeAsState(initial = "User")
    
    val context = LocalContext.current
    var showDurationDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    
    EditNameDialog(
        showDialog = showEditNameDialog,
        onDismiss = { showEditNameDialog = false },
        initialName = userName,
        onSave = { viewModel.setUserName(it) }
    )
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFFE2E2E2))
            }
            Text("Settings", color = Color(0xFFE2E2E2), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        // Edit Username
        Card(
            modifier = Modifier.height(52.dp).fillMaxWidth().clickable { showEditNameDialog = true },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Change Username", color = Color(0xFFE2E2E2), fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFE2E2E2))
            }
        }
        
        // Notification Sound
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Notification Sound", color = Color(0xFFE2E2E2), fontWeight = FontWeight.Bold)
                Switch(checked = notificationSound, onCheckedChange = { viewModel.setNotificationSound(it) })
            }
        }
        
        // Break Reminder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Break Reminder", color = Color(0xFFE2E2E2), fontWeight = FontWeight.Bold)
                Switch(checked = breakReminder, onCheckedChange = { viewModel.setBreakReminder(it) })
            }
        }
        
        // Daily Reminder Time
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                val cal = java.util.Calendar.getInstance()
                val parts = dailyReminderTime?.split(":") ?: emptyList()
                val hour = if (parts.size == 2) parts[0].toIntOrNull() ?: cal.get(java.util.Calendar.HOUR_OF_DAY) else cal.get(java.util.Calendar.HOUR_OF_DAY)
                val min = if (parts.size == 2) parts[1].toIntOrNull() ?: cal.get(java.util.Calendar.MINUTE) else cal.get(java.util.Calendar.MINUTE)
                
                android.app.TimePickerDialog(context, { _, h, m ->
                    viewModel.setDailyReminder(h, m)
                }, hour, min, true).show()
            },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Daily Reminder Time", color = Color(0xFFE2E2E2), fontWeight = FontWeight.Bold)
                Text(dailyReminderTime ?: "None", color = Color(0xFF00DFC1))
            }
        }
        
        // Study Commitment Duration
        Box {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showDurationDropdown = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Study Commitment Duration", color = Color(0xFFE2E2E2), fontWeight = FontWeight.Bold)
                    Text("$studyCommitmentDuration Month(s)", color = Color(0xFF00DFC1))
                }
            }
            androidx.compose.material3.DropdownMenu(
                expanded = showDurationDropdown,
                onDismissRequest = { showDurationDropdown = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                listOf(1, 2, 3, 6, 12).forEach { months ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("$months Month(s)", color = Color(0xFFE2E2E2)) },
                        onClick = {
                            viewModel.setStudyCommitmentDuration(months)
                            showDurationDropdown = false
                        }
                    )
                }
            }
        }
        
        // App Language
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("App Language", color = Color(0xFFE2E2E2), fontWeight = FontWeight.Bold)
                Text("$appLanguage (Coming Soon)", color = Color(0xFF00DFC1))
            }
        }
        
        // Delete Account
        Spacer(modifier = Modifier.height(16.dp))
        var showDeleteConfirm by remember { mutableStateOf(false) }
        Button(
            onClick = { showDeleteConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
            modifier = Modifier.height(52.dp).fillMaxWidth()
        ) {
            Text("Delete Account", color = Color.White)
        }
        
        if (showDeleteConfirm) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to permanently delete your account and all associated data? This action cannot be undone.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAccount()
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "LockedIn Version 1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val checkUnlocked: (List<StudySession>, Int, Int) -> Boolean
)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AchievementBadge(achievement: Achievement, sessions: List<StudySession>, streak: Int, xp: Int) {
    val isUnlocked = achievement.checkUnlocked(sessions, streak, xp)
    val tealColor = Color(0xFF00DFC1)
    val context = LocalContext.current
    var showShare by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (showShare) showShare = false },
                onLongClick = {
                    if (isUnlocked) {
                        showShare = true
                    }
                }
            ),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) tealColor.copy(alpha = 0.1f) else Color(0xFF1E2020)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isUnlocked) 2.dp else 1.dp, if (isUnlocked) tealColor else Color.Gray)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = achievement.title,
                    tint = if (isUnlocked) tealColor else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = achievement.title,
                    color = if (isUnlocked) Color(0xFFE2E2E2) else Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = achievement.description,
                    color = if (isUnlocked) Color(0xFFE2E2E2).copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 12.sp
                )
            }
            if (showShare) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF1E2020).copy(alpha = 0.95f))
                        .clickable {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "I just unlocked ${achievement.title} on LockedIn!")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                            showShare = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = tealColor, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Share", color = tealColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable
fun StatsScreen(viewModel: MainViewModel, navController: NavController) {
    val weeklyStudyMinutes by viewModel.weeklyStudyMinutes.observeAsState(initial = 0)
    val weeklySessionsCompleted by viewModel.weeklySessionsCompleted.observeAsState(initial = 0)
    val bestStreak by viewModel.bestStreak.observeAsState(initial = 0)
    val userXP by viewModel.userXP.observeAsState(initial = 0)
    val weeklyStudyData by viewModel.weeklyStudyData.observeAsState(initial = emptyList())
    val studySessions by viewModel.studySessions.observeAsState(initial = emptyList())
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val whiteText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val cardShape = RoundedCornerShape(12.dp)
    val cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = whiteText)
            }
            Text("Stats & Analytics", color = whiteText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = cardShape, border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Time", color = mutedText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${weeklyStudyMinutes}m", color = tealColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = cardShape, border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Sessions", color = mutedText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$weeklySessionsCompleted", color = tealColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = cardShape, border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Best Streak", color = mutedText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$bestStreak", color = tealColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = cardShape, border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total XP", color = mutedText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$userXP", color = tealColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Weekly Study Time (Last 7 Days)", color = whiteText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = cardShape, border = cardBorder
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                if (weeklyStudyData.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No data yet", color = mutedText)
                    }
                } else {
                    val maxMins = weeklyStudyData.maxOrNull() ?: 1f
                    val yMax = if (maxMins == 0f) 1f else maxMins
                    
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyStudyData.forEachIndexed { index, value ->
                            val heightFraction = (value / yMax).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(tealColor)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val daysLabels = listOf("D-6", "D-5", "D-4", "D-3", "D-2", "Yest", "Today")
                        for (i in 0 until 7) {
                            Text(
                                text = daysLabels.getOrNull(i) ?: "",
                                color = mutedText,
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Recent Sessions", color = whiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        val recentSessions = studySessions.sortedByDescending { it.timestamp }.take(5)
        if (recentSessions.isEmpty()) {
            Text("No sessions yet. Start your first session!", color = mutedText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        } else {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
            recentSessions.forEach { session ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = sdf.format(java.util.Date(session.timestamp)),
                            color = mutedText,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${session.durationMinutes} Minutes",
                                color = whiteText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+${session.durationMinutes} XP",
                                color = tealColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("session_history") },
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                modifier = Modifier.height(52.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Full Session History", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionHistoryScreen(viewModel: MainViewModel, navController: NavController) {
    val studySessions by viewModel.studySessions.observeAsState(initial = emptyList())
    
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val whiteText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val redColor = Color(0xFFFF5252)
    var sessionToDelete by remember { mutableStateOf<StudySession?>(null) }
    
    if (sessionToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this session? This action cannot be undone.") },
            confirmButton = {
                TextButton(
onClick = {
                    sessionToDelete?.let { viewModel.deleteStudySession(it) }
                    sessionToDelete = null
  }) {
                    Text("Delete", color = redColor)
                }
            },
            dismissButton = {
                TextButton(
onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = tealColor)
                }
            },
            containerColor = cardColor,
            titleContentColor = whiteText,
            textContentColor = mutedText
   )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = whiteText)
            }
            Text("Session History", color = whiteText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val sortedSessions = studySessions.sortedByDescending { it.timestamp }
        if (sortedSessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No sessions yet. Start your first session!", color = mutedText, textAlign = TextAlign.Center)
            }
        } else {
            val sdf = remember { java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()) }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedSessions) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { sessionToDelete = session }
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = sdf.format(java.util.Date(session.timestamp)),
                                color = mutedText,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${session.durationMinutes} Minutes",
                                    color = whiteText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+${session.durationMinutes} XP",
                                    color = tealColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun AchievementsScreen(viewModel: MainViewModel, navController: NavController) {
    val sessions by viewModel.studySessions.observeAsState(initial = emptyList())
    val streak by viewModel.streak.observeAsState(initial = 0)
    val xp by viewModel.userXP.observeAsState(initial = 0)
    val tasks by viewModel.tasks.observeAsState(initial = emptyList())
    fun isAfter10PM(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= 22
    }
    fun isBefore8AM(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return hour < 8
    }
    val achievements = listOf(
        Achievement("first_session", "First Session", "Complete your first study session", Icons.Default.PlayArrow) { s, _, _ -> s.isNotEmpty() },
        Achievement("streak_starter", "Streak Starter", "Get a 3 day streak", Icons.Default.DateRange) { _, st, _ -> st >= 3 },
        Achievement("grinder", "Grinder", "Complete 10 sessions", Icons.Default.Edit) { s, _, _ -> s.size >= 10 },
        Achievement("night_owl", "Night Owl", "Study after 10pm", Icons.Default.Lock) { s, _, _ -> s.any { isAfter10PM(it.timestamp) } },
        Achievement("legend", "Legend", "Reach 500 XP", Icons.Default.Star) { _, _, x -> x >= 500 },
        Achievement("early_bird", "Early Bird", "Study before 8am", Icons.Default.CheckCircle) { s, _, _ -> s.any { isBefore8AM(it.timestamp) } },
        Achievement("focused", "Focused", "Complete a 50 min session", Icons.Default.CheckCircle) { s, _, _ -> s.any { it.durationMinutes >= 50 } },
        Achievement("planner", "Planner", "Add 5 tasks to your plan", Icons.Default.List) { _, _, _ -> tasks.size >= 5 },
        Achievement("consistent", "Consistent", "Study 7 days in a row", Icons.Default.DateRange) { _, st, _ -> st >= 7 },
        Achievement("xp_hunter", "XP Hunter", "Earn 200 XP total", Icons.Default.Star) { _, _, x -> x >= 200 }
    )
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFFE2E2E2))
            }
            Text("Achievements", color = Color(0xFFE2E2E2), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(achievements.size) { index ->
                AchievementBadge(achievements[index], sessions, streak, xp)
            }
        }
    }
}
@Composable
fun StreakCalendar(sessions: List<StudySession>, cardColor: Color, tealColor: Color) {
    val calendar = remember { java.util.Calendar.getInstance() }
    val currentMonth = calendar.get(java.util.Calendar.MONTH)
    val currentYear = calendar.get(java.util.Calendar.YEAR)
    val monthName = remember { java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(calendar.time) }
    
    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1 // 0 = Sun, 1 = Mon...
    
    val todayCal = remember { java.util.Calendar.getInstance() }
    val isCurrentMonthYear = currentYear == todayCal.get(java.util.Calendar.YEAR) && currentMonth == todayCal.get(java.util.Calendar.MONTH)
    val todayDay = todayCal.get(java.util.Calendar.DAY_OF_MONTH)
    
    val dayStats = remember(sessions) {
        val stats = mutableMapOf<Int, Pair<Int, Int>>()
        val cal = java.util.Calendar.getInstance()
        for (s in sessions) {
            cal.timeInMillis = s.timestamp
            if (cal.get(java.util.Calendar.YEAR) == currentYear && cal.get(java.util.Calendar.MONTH) == currentMonth) {
                val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
                val current = stats[day] ?: Pair(0, 0)
                stats[day] = Pair(current.first + s.durationMinutes, current.second + 1)
            }
        }
        stats
    }
    val totalMinutesThisMonth = dayStats.values.sumOf { it.first }
    val totalSessionsThisMonth = dayStats.values.sumOf { it.second }
    var selectedDayStats by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedDay by remember { mutableStateOf(0) }
    if (selectedDayStats != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedDayStats = null },
            title = { Text("Day $selectedDay") },
            text = {
                Column {
                    Text("Total study time: ${selectedDayStats!!.first} mins")
                    Text("Sessions completed: ${selectedDayStats!!.second}")
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
onClick = { selectedDayStats = null }) {
                    Text("Close")
                }
  }
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(monthName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                    Text(it, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val totalCells = daysInMonth + firstDayOfWeek
            val rows = (totalCells + 6) / 7
            
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (c in 0..6) {
                        val i = r * 7 + c
                        val day = i - firstDayOfWeek + 1
                        if (day in 1..daysInMonth) {
                            val stats = dayStats[day]
                            val isHighlighted = stats != null
                            val isToday = isCurrentMonthYear && day == todayDay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(if (isHighlighted) tealColor else Color.Transparent)
                                    .then(if (isToday && !isHighlighted) Modifier.border(1.dp, tealColor, CircleShape) else Modifier)
                                    .clickable(enabled = isHighlighted) {
                                        selectedDayStats = stats
                                        selectedDay = day
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = if (isHighlighted) Color.Black else if (isToday) tealColor else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isHighlighted || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isHighlighted) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 6.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(tealColor)
                                    )
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Total This Month", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$totalMinutesThisMonth minutes • $totalSessionsThisMonth sessions", color = tealColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
@Composable
fun EditNameDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    initialName: String,
    onSave: (String) -> Unit
) {
    if (!showDialog) return
    var editNameInput by remember { mutableStateOf(initialName) }
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val whiteText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Name") },
        text = {
            OutlinedTextField(
                value = editNameInput,
                onValueChange = { editNameInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
onClick = {
                if (editNameInput.isNotBlank()) {
                    onSave(editNameInput.trim())
                }
  onDismiss()
            }) {
                Text("Save", color = tealColor)
            }
        },
        dismissButton = {
            TextButton(
onClick = onDismiss) {
                Text("Cancel", color = mutedText)
            }
        },
        containerColor = cardColor,
        titleContentColor = whiteText,
        textContentColor = whiteText
   )
}
@Composable
fun ProfileStatBox(title: String, value: String, modifier: Modifier = Modifier, cardColor: Color, tealColor: Color) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = tealColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
@Composable
fun ProfileScreen(viewModel: MainViewModel, navController: NavController) {
    val userName by viewModel.userName.observeAsState(initial = "User")
    val userEmail = viewModel.getCurrentUserEmail() ?: "user@example.com"
    val userXp by viewModel.userXP.observeAsState(initial = 0)
    val studySessions by viewModel.studySessions.observeAsState(initial = emptyList())
    val initials = userName.take(2).uppercase()
    val cardShape = RoundedCornerShape(12.dp)
    val cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(userName) }
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val whiteText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(tealColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initials, style = MaterialTheme.typography.displayMedium, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = userName, style = MaterialTheme.typography.headlineSmall, color = whiteText)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { 
                editNameInput = userName
                showEditNameDialog = true 
            }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = tealColor, modifier = Modifier.size(16.dp))
            }
            if (viewModel.isRewardUnlocked("profile_badge")) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Profile Badge",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(text = userEmail, style = MaterialTheme.typography.bodyMedium, color = mutedText)
        Spacer(modifier = Modifier.height(24.dp))
        EditNameDialog(
            showDialog = showEditNameDialog,
            onDismiss = { showEditNameDialog = false },
            initialName = editNameInput,
            onSave = { viewModel.setUserName(it) }
        )
        val currentLevel = (userXp / 100) + 1
        val nextLevelXp = currentLevel * 100
        val progress = (userXp % 100).toFloat() / 100f
        
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Level $currentLevel (${userXp} XP)", color = mutedText)
                Text("Next Level: $nextLevelXp", color = mutedText)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(12.dp)),
                color = tealColor,
                trackColor = cardColor
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        val bestStreak by viewModel.bestStreak.observeAsState(initial = 0)
        val totalSessions = studySessions.size
        val totalHours = studySessions.sumOf { it.durationMinutes } / 60
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatBox(
                title = "Total Sessions",
                value = totalSessions.toString(),
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                tealColor = tealColor
            )
            ProfileStatBox(
                title = "Total Hours",
                value = totalHours.toString(),
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                tealColor = tealColor
            )
            ProfileStatBox(
                title = "Best Streak",
                value = bestStreak.toString(),
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                tealColor = tealColor
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        StreakCalendar(sessions = studySessions, cardColor = cardColor, tealColor = tealColor)
        Spacer(modifier = Modifier.height(24.dp))
        // Stats & Analytics Button
        Button(
            onClick = { navController.navigate("stats") },
            colors = ButtonDefaults.buttonColors(containerColor = cardColor),
            modifier = Modifier.height(52.dp).fillMaxWidth(),
            shape = cardShape,
            border = cardBorder
        ) {
            Text("Stats & Analytics", color = whiteText)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        // Achievements Button
        Button(
            onClick = { navController.navigate("achievements") },
            colors = ButtonDefaults.buttonColors(containerColor = cardColor),
            modifier = Modifier.height(52.dp).fillMaxWidth(),
            shape = cardShape,
            border = cardBorder
        ) {
            Text("View Achievements", color = whiteText)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        // Rewards Shop Button
        Button(
            onClick = { navController.navigate("rewards_shop") },
            colors = ButtonDefaults.buttonColors(containerColor = cardColor),
            modifier = Modifier.height(52.dp).fillMaxWidth(),
            shape = cardShape,
            border = cardBorder
        ) {
            Text("Rewards Shop", color = whiteText)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        // Settings Button
        Button(
            onClick = { navController.navigate("settings") },
            colors = ButtonDefaults.buttonColors(containerColor = cardColor),
            modifier = Modifier.height(52.dp).fillMaxWidth(),
            shape = cardShape,
            border = cardBorder
        ) {
            Text("Settings", color = whiteText)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        // Privacy Policy Button
        Button(
            onClick = { navController.navigate("privacy_policy") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.height(52.dp).fillMaxWidth(),
            shape = cardShape,
            border = cardBorder
        ) {
            Text("Privacy Policy", color = mutedText)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        // Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = cardShape,
            border = cardBorder
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, "Badge", tint = tealColor, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Legend", color = whiteText, fontWeight = FontWeight.Bold)
                    Text("Current Badge", color = mutedText)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = cardShape,
            border = cardBorder
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode", color = whiteText)
                val isDarkModePref by viewModel.isDarkMode.observeAsState()
                val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                val darkThemeState = isDarkModePref ?: isSystemDark
                Switch(checked = darkThemeState, onCheckedChange = { viewModel.setDarkMode(it) })
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.signOut() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
            modifier = Modifier.height(52.dp).fillMaxWidth()
        ) {
            Text("Log Out", color = Color.Black)
        }
    }
}
@Composable
fun HomeScreen(viewModel: MainViewModel, navController: NavController) {
    val sessionsCompleted by viewModel.sessionsCompleted.observeAsState(initial = -1)
    val streak by viewModel.streak.observeAsState(initial = 0)
    val userXp by viewModel.userXP.observeAsState(initial = 0)
    val commitmentDuration by viewModel.studyCommitmentDuration.observeAsState(initial = 1)
    val commitmentStartDate by viewModel.studyCommitmentStartDate.observeAsState(initial = null)
    val sessions by viewModel.studySessions.observeAsState(initial = emptyList())
    val weeklyGoalHours by viewModel.weeklyGoalHours.observeAsState(initial = 10)
    
    val hasStudiedToday = remember(sessions) {
        val todayCal = java.util.Calendar.getInstance()
        todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        todayCal.set(java.util.Calendar.MINUTE, 0)
        todayCal.set(java.util.Calendar.SECOND, 0)
        todayCal.set(java.util.Calendar.MILLISECOND, 0)
        val startOfToday = todayCal.timeInMillis
        sessions.any { it.timestamp >= startOfToday }
    }
    
    // Define colors
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val greenColor = MaterialTheme.colorScheme.secondary
    val whiteText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        var userEmail by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) {
            userEmail = viewModel.getCurrentUserEmail()
        }
        val userName by viewModel.userName.observeAsState(initial = "")
        val displayName = userName.ifBlank { userEmail?.substringBefore("@") ?: "Student" }
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar placeholder
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(tealColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = displayName.first().toString().uppercase(), color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "LockedIn", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
            IconButton(onClick = { navController.navigate("xp_leaderboard") }) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard", tint = tealColor)
            }
        }
        
        // Greeting
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Good Morning, ${displayName}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                if (viewModel.isRewardUnlocked("profile_badge")) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Profile Badge",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (streak == 0) "🔥 Start your streak today!" else "🔥 $streak DAY STREAK",
                color = tealColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            
            if (streak >= 3 && !hasStudiedToday) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Study today to keep your streak!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Daily Motivational Quote
        val dailyQuotes = remember {
            listOf(
                "The secret of getting ahead is getting started.",
                "It always seems impossible until it's done.",
                "Don't watch the clock; do what it does. Keep going.",
                "The future depends on what you do today.",
                "Believe you can and you're halfway there.",
                "Success is no accident. It is hard work, perseverance, learning, studying, sacrifice and most of all, love of what you are doing.",
                "Focus on your goal. Don't look in any direction but ahead.",
                "Doubt kills more dreams than failure ever will.",
                "You don't have to be great to start, but you have to start to be great.",
                "Push yourself, because no one else is going to do it for you.",
                "Sometimes later becomes never. Do it now.",
                "Great things never come from comfort zones.",
                "Dream it. Wish it. Do it.",
                "Success doesn't just find you. You have to go out and get it.",
                "The harder you work for something, the greater you'll feel when you achieve it.",
                "Dream bigger. Do bigger.",
                "Don't stop when you're tired. Stop when you're done.",
                "Wake up with determination. Go to bed with satisfaction.",
                "Do something today that your future self will thank you for.",
                "Little things make big days.",
                "It's going to be hard, but hard does not mean impossible.",
                "Don't wait for opportunity. Create it.",
                "Sometimes we're tested not to show our weaknesses, but to discover our strengths.",
                "The key to success is to focus on goals, not obstacles.",
                "Dream it. Believe it. Build it.",
                "Motivation is what gets you started. Habit is what keeps you going.",
                "You may see me struggle, but you will never see me quit.",
                "I'm not telling you it is going to be easy - I'm telling you it's going to be worth it.",
                "A journey of a thousand miles begins with a single step.",
                "Make each day your masterpiece."
            )
        }
        val dayOfYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR) }
        val todayQuote = dailyQuotes[dayOfYear % 30]
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Quote",
                    tint = tealColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "\"$todayQuote\"",
                    color = mutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
        // Study Goal Card
        val targetDateMs = remember(commitmentDuration, commitmentStartDate) {
            if (commitmentStartDate == null) {
                System.currentTimeMillis() + (commitmentDuration * 30L * 24L * 60L * 60L * 1000L)
            } else {
                commitmentStartDate!! + (commitmentDuration * 30L * 24L * 60L * 60L * 1000L)
            }
        }
        val daysRemaining = remember(targetDateMs) {
            val remainingMs = targetDateMs - System.currentTimeMillis()
            if (remainingMs > 0) (remainingMs / (24L * 60L * 60L * 1000L)).toInt() else 0
        }
        val targetDateStr = remember(targetDateMs) {
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(targetDateMs))
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Study Goal", color = mutedText, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$daysRemaining Days left", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Target: $targetDateStr", color = tealColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
onClick = { navController.navigate("settings") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2020)),
                        shape = RoundedCornerShape(12.dp)
   ) {
                        Text("Adjust Plan", color = Color.White, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Weekly Target (hrs): ", color = mutedText, fontSize = 14.sp)
                    androidx.compose.foundation.text.BasicTextField(
                        value = weeklyGoalHours.toString(),
                        onValueChange = { 
                            val num = it.toIntOrNull()
                            if (num != null) viewModel.setWeeklyGoalHours(num)
                            else if (it.isEmpty()) viewModel.setWeeklyGoalHours(0)
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.width(40.dp).background(Color(0xFF1E2020), RoundedCornerShape(12.dp)).padding(4.dp)
                    )
                }
                
                val hoursStudiedThisWeek = remember(sessions) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    val startOfWeekMs = calendar.timeInMillis
                    
                    val minutesStudied = sessions.filter { it.timestamp >= startOfWeekMs }.sumOf { it.durationMinutes }
                    minutesStudied / 60f
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    val progress = if (weeklyGoalHours > 0) (hoursStudiedThisWeek / weeklyGoalHours).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(12.dp)),
                        color = tealColor,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    val hoursStr = if (hoursStudiedThisWeek % 1 == 0f) {
                        hoursStudiedThisWeek.toInt().toString()
                    } else {
                        String.format(java.util.Locale.getDefault(), "%.1f", hoursStudiedThisWeek)
                    }
                    Text("$hoursStr of $weeklyGoalHours hours", color = tealColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        // Level Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val currentLevel = (userXp / 100) + 1
                val currentXpInLevel = userXp % 100
                Text("Level $currentLevel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$userXp XP earned across all sessions", color = mutedText, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { currentXpInLevel / 100f },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(12.dp)),
                        color = tealColor,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("$currentXpInLevel/100 XP", color = tealColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { navController.navigate("tasks") },
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF1E2020))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tasks", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tasks", color = Color.White, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { navController.navigate("plan") },
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF1E2020))
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "Plan", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Plan", color = Color.White, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { navController.navigate("leaderboard") },
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF1E2020))
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Leaderboard", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Goals", color = Color.White, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { navController.navigate("xp_leaderboard") },
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF1E2020))
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = "XP Leaderboard", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Leaders", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
        val notes by viewModel.notes.observeAsState(initial = emptyList())
        val recentNotes = notes.sortedByDescending { it.timestamp }.take(3)
        if (recentNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Notes", color = Color.White, fontWeight = FontWeight.Bold)
            }
            recentNotes.forEach { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(note.content, color = whiteText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(note.timestamp)), color = mutedText, fontSize = 12.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("study") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tealColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Start Studying", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Studying", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        // Recent Tasks
        val tasks by viewModel.tasks.observeAsState(initial = emptyList())
        val recentTasks = tasks.sortedByDescending { it.timestamp }.take(3)
        if (recentTasks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Tasks", color = Color.White, fontWeight = FontWeight.Bold)
                TextButton(onClick = { navController.navigate("tasks") }) {
                    Text("VIEW ALL", color = tealColor)
                }
            }
            recentTasks.forEach { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.updateTask(task.copy(isCompleted = it)) },
                            colors = CheckboxDefaults.colors(checkedColor = tealColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(task.name, color = whiteText, modifier = Modifier.weight(1f), textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Surface(
                            color = if (task.isCompleted) greenColor.copy(alpha = 0.2f) else if (task.priority == "High") Color.Red.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (task.isCompleted) "DONE" else if (task.priority == "High") "URGENT" else task.priority.uppercase(),
                                color = if (task.isCompleted) greenColor else if (task.priority == "High") Color.Red else mutedText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(viewModel: MainViewModel) {
    // Define colors
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val greenColor = MaterialTheme.colorScheme.secondary
    val selectedType by viewModel.selectedSessionType.observeAsState(initial = MainViewModel.SessionType.POMODORO_25)
    val context = LocalContext.current
    val isFocusModeOn by viewModel.isFocusModeOn.observeAsState(initial = false)
    val isLockedIn by viewModel.isLockedIn.observeAsState(initial = false)
    val isOnBreak by viewModel.isOnBreak.observeAsState(initial = false)
    val isPaused by viewModel.isPaused.observeAsState(initial = false)
    val timeRemainingString by viewModel.timeRemainingString.observeAsState(initial = "25:00")
    val sessionProgress by viewModel.sessionProgress.observeAsState(initial = 1.0f)
    val sessionLabel by viewModel.sessionLabel.observeAsState(initial = "Focus Session")
    val sessionCompletedMessage by viewModel.sessionCompletedMessage.observeAsState(initial = null)
    val showCompletionDialog by viewModel.showCompletionDialog.observeAsState(initial = false)
    val lastEarnedXp by viewModel.lastEarnedXp.observeAsState(initial = 0)
    val userXP by viewModel.userXP.observeAsState(initial = 0)
    val customMinutes by viewModel.customMinutes.observeAsState(initial = 15)
    var showCustomDialog by remember { mutableStateOf(false) }
    var customMinutesString by remember { mutableStateOf("") }
    
    var showNotesSheet by remember { mutableStateOf(false) }
    var quickNoteContent by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    // Smooth UI animations
    val progressAnimated by animateFloatAsState(targetValue = sessionProgress, label = "Progress")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = sessionLabel,
                onValueChange = { viewModel.setSessionLabel(it) },
                label = { Text("Session Label") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tealColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )
            IconButton(
                onClick = { showNotesSheet = true },
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(cardColor)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Quick Notes", tint = tealColor)
            }
        }
        // Session Complete Banner
        sessionCompletedMessage?.let { msg ->
            val isBreakCompleted = msg.contains("Break")
            val bannerColor = if (isBreakCompleted) Color(0xFF00DFC1) else Color(0xFF6FFE00)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("session_completed_banner"),
                colors = CardDefaults.cardColors(
                    containerColor = bannerColor.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = bannerColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBreakCompleted) "⚡" else "🎉",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor
                        )
                    }
                    IconButton(
                        onClick = { viewModel.dismissCompletedMessage() },
                        modifier = Modifier.height(52.dp).testTag("dismiss_completed_banner_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = bannerColor
                        )
                    }
                }
            }
        }
        // Pill Buttons
        val buttons = listOf(
            MainViewModel.SessionType.POMODORO_25 to "25 min", 
            MainViewModel.SessionType.EXTENDED_50 to "50 min", 
            MainViewModel.SessionType.CUSTOM to "Custom"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            buttons.forEach { (type, label) ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF1D2121) else Color.Transparent)
                        .border(if (isSelected) 1.dp else 0.dp, if (isSelected) tealColor else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { 
                            if (type == MainViewModel.SessionType.CUSTOM) {
                                showCustomDialog = true
                            }
                            viewModel.setSessionType(type) 
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (isSelected) Color.White else Color.Gray)
                }
            }
        }
        if (showCustomDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showCustomDialog = false },
                title = { Text("Custom Session Duration") },
                text = {
                    OutlinedTextField(
                        value = customMinutesString,
                        onValueChange = { if (it.all { char -> char.isDigit() }) customMinutesString = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("Minutes") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tealColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    TextButton(
onClick = {
                        val mins = customMinutesString.toIntOrNull()
                        if (mins != null && mins > 0) {
                            viewModel.setCustomMinutes(mins)
                        }
                        showCustomDialog = false
  }) {
                        Text("OK", color = tealColor)
                    }
                },
                dismissButton = {
                    TextButton(
onClick = { showCustomDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = cardColor,
                titleContentColor = Color.White,
                textContentColor = Color.White
   )
        }
        // Circular Timer
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val timerBrush = if (isOnBreak) {
                    Brush.horizontalGradient(listOf(Color(0xFFFFA500), Color(0xFFFF4500)))
                } else {
                    Brush.horizontalGradient(listOf(tealColor, greenColor))
                }
                drawArc(
                    brush = timerBrush,
                    startAngle = -90f,
                    sweepAngle = 360f * sessionProgress,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeRemainingString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 64.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (isLockedIn || isOnBreak) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOnBreak) {
                            if (isPaused) "BREAK PAUSED" else "BREAK"
                        } else {
                            if (isPaused) "PAUSED" else "ACTIVE"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnBreak && !isPaused) {
                            Color(0xFFFFA500)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
        // Control buttons
        if (isOnBreak) {
            Button(
onClick = { viewModel.cancelTimer() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = cardColor, // Subtle background
                    contentColor = Color.White
   ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("skip_break_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SKIP BREAK",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        } else if (isLockedIn) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pause / Resume Button
                Button(
onClick = { viewModel.togglePause() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (isPaused) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
   ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("pause_resume_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPaused) "RESUME" else "PAUSE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                // Break Focus Button
                Button(
onClick = { viewModel.cancelTimer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
   ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("break_focus_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "BREAK FOCUS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            // Lock in now button
            Button(
onClick = { viewModel.toggleLockIn() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
   ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("lock_in_toggle_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "LOCK IN NOW",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
        // Ambient Sounds Panel
        val currentSound by viewModel.currentSound.observeAsState(initial = null)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ambient Sounds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sounds = mutableListOf(
                        "🌧️ Rain" to AmbientSoundManager.SoundType.RAIN,
                        "🎧 Lo-fi" to AmbientSoundManager.SoundType.LOFI,
                        "📻 Noise" to AmbientSoundManager.SoundType.WHITE_NOISE
                    )
                    if (viewModel.isRewardUnlocked("exclusive_sounds")) {
                        sounds.add("🌲 Forest" to AmbientSoundManager.SoundType.FOREST)
                        sounds.add("🌊 Ocean" to AmbientSoundManager.SoundType.OCEAN)
                    }
                    
                    sounds.forEach { (label, type) ->
                        val isSelected = currentSound == type
                        Button(
onClick = {
                                if (isSelected) {
                                    viewModel.stopAmbientSound()
                                } else {
                                    viewModel.playAmbientSound(type)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
   ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (currentSound != null) {
                    Row(
                        modifier = Modifier.height(52.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { viewModel.stopAmbientSound() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp).width(4.dp))
                            Text("Stop Audio", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        // Focus Mode Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFocusModeOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isFocusModeOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFocusModeOn) 4.dp else 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isFocusModeOn) "Focus Mode ON" else "Focus Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isFocusModeOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Block notifications while studying",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFocusModeOn) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                androidx.compose.material3.Switch(
                    checked = isFocusModeOn,
                    onCheckedChange = { viewModel.toggleFocusMode(context) },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }
        
        if (showCompletionDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.dismissCompletionDialog() },
                title = { Text("Session Complete!") },
                text = {
                    Column {
                        Text("You earned $lastEarnedXp XP")
                        Text("Total XP: $userXP")
                    }
                },
                confirmButton = {
                    TextButton(
onClick = { viewModel.dismissCompletionDialog() }) {
                        Text("OK", color = tealColor)
                    }
                },
                containerColor = cardColor,
                titleContentColor = Color.White,
                textContentColor = Color.White
   )
        }
    }
    if (showNotesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotesSheet = false },
            sheetState = sheetState,
            containerColor = cardColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding()
            ) {
                Text(
                    text = "Quick Note",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = quickNoteContent,
                    onValueChange = { quickNoteContent = it },
                    placeholder = { Text("How is the session going?") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tealColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (quickNoteContent.isNotBlank()) {
                            viewModel.addNote("Quick", "Session Note", quickNoteContent)
                            quickNoteContent = ""
                            coroutineScope.launch {
                                sheetState.hide()
                                showNotesSheet = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tealColor)
                ) {
                    Text("Save Note", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Project Metadata",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "LockedIn is fully compliant with all requested Empty Activity system configuration baselines. Below is a specification declaration of the project components:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpecItem(label = "Application ID", value = "com.lockedin.app")
            SpecItem(label = "Minimum SDK", value = "API 26 (Android 8.0)")
            SpecItem(label = "Material Design", value = "Material Design 3 (M3)")
            SpecItem(label = "Navigation", value = "Jetpack Navigation (NavHost)")
            SpecItem(label = "Architecture", value = "ViewModel + LiveData (Jetpack)")
            SpecItem(label = "UI Engine", value = "Jetpack Compose with Bom")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Ready to build from this production-grade base template.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun SpecItem(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alphaIcon = remember { androidx.compose.animation.core.Animatable(0f) }
    val alphaText1 = remember { androidx.compose.animation.core.Animatable(0f) }
    val alphaText2 = remember { androidx.compose.animation.core.Animatable(0f) }
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaIcon.animateTo(1f, androidx.compose.animation.core.tween(500))
        alphaText1.animateTo(1f, androidx.compose.animation.core.tween(500))
        alphaText2.animateTo(1f, androidx.compose.animation.core.tween(500))
        progress.animateTo(1f, androidx.compose.animation.core.tween(2000))
        onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "App Logo",
                tint = Color(0xFF00DFC1),
                modifier = Modifier.size(100.dp).graphicsLayer(alpha = alphaIcon.value)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "LockedIn",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.graphicsLayer(alpha = alphaText1.value)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PRECISION PRODUCTIVITY",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE2E2E2),
                modifier = Modifier.graphicsLayer(alpha = alphaText2.value)
            )
            Spacer(modifier = Modifier.height(48.dp))
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.fillMaxWidth(0.6f).height(6.dp).clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF00DFC1),
                trackColor = Color(0xFF1E2020)
            )
        }
    }
}
@Composable
fun MessageCard(message: String, isError: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
        border = BorderStroke(1.dp, if (isError) Color.Red else Color(0xFF00DFC1))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) Color.Red else Color(0xFF00DFC1)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = message, color = Color.White)
        }
    }
}
@Composable
fun EmptyStateView(icon: ImageVector, message: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF00DFC1),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val bgColor = MaterialTheme.colorScheme.background
    val tealColor = MaterialTheme.colorScheme.primary
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val whiteText = MaterialTheme.colorScheme.onBackground
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (step == 1) {
            Text(
                text = "Welcome to LockedIn",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = whiteText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Deep focus for the digital generation",
                style = MaterialTheme.typography.bodyLarge,
                color = mutedText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
onClick = { step = 2 },
                colors = ButtonDefaults.buttonColors(containerColor = tealColor, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
   ) {
                Text("Next")
            }
        } else if (step == 2) {
            Text(
                text = "Earn XP Every Session",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = whiteText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Study, earn points, unlock rewards\nin the shop",
                style = MaterialTheme.typography.bodyLarge,
                color = mutedText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tealColor)
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
onClick = { step = 3 },
                colors = ButtonDefaults.buttonColors(containerColor = tealColor, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
   ) {
                Text("Next")
            }
        } else if (step == 3) {
            Text(
                text = "Set Your First Goal",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = whiteText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tell us your target date and we will\ntrack your progress every day",
                style = MaterialTheme.typography.bodyLarge,
                color = mutedText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
modifier = Modifier.fillMaxWidth().height(52.dp), 
onClick = onComplete,
                colors = ButtonDefaults.buttonColors(containerColor = tealColor, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
   ) {
                Text("Get Started")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: MainViewModel) {
    val tasks by viewModel.tasks.observeAsState(initial = emptyList())
    val isLoadingTasks by viewModel.isLoadingTasks.observeAsState(initial = false)
    
    var taskName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") } // High, Medium, Low
    // Define colors
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val greenColor = MaterialTheme.colorScheme.secondary
    val redColor = MaterialTheme.colorScheme.error
    val yellowColor = MaterialTheme.colorScheme.tertiary
    val whiteText = Color(0xFFE2E2E2)
    val mutedText = Color(0xFF94A3B8)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tasks & Goals",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Tasks Progress Card
        item {
            val totalCount = tasks.size
            val completedCount = tasks.count { it.isCompleted }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$completedCount/$totalCount Completed", color = whiteText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = tealColor,
                        trackColor = Color(0xFF334155)
                    )
                }
            }
        }
        // Add task Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Task", color = whiteText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        placeholder = { Text("Task description...", color = mutedText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tealColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("High" to redColor, "Medium" to yellowColor, "Low" to tealColor).forEach { (p, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (priority == p) color else Color(0xFF1D2121))
                                    .clickable { priority = p },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, color = if (priority == p) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Gradient Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(tealColor, greenColor)))
                            .clickable {
                                if (taskName.isNotBlank()) {
                                    viewModel.addTask(taskName, priority)
                                    taskName = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        if (isLoadingTasks && tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00DFC1))
                }
            }
        } else if (tasks.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyStateView(
                        icon = Icons.AutoMirrored.Filled.List,
                        message = "No tasks yet!",
                        subtitle = "Add your first goal 🎯"
                    )
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                val priorityColor = when (task.priority) {
                    "High" -> MaterialTheme.colorScheme.error
                    "Medium" -> MaterialTheme.colorScheme.tertiary
                    "Low" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primary
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_item_card_${task.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                        } else {
                            priorityColor.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTaskComplete(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = priorityColor,
                                uncheckedColor = priorityColor.copy(alpha = 0.6f),
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("task_complete_checkbox_${task.id}")
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(priorityColor)
                                )
                                Text(
                                    text = "${task.priority.uppercase()} PRIORITY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = priorityColor
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.deleteTask(task) },
                            modifier = Modifier.height(52.dp).testTag("task_delete_btn_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(viewModel: MainViewModel, navController: NavController) {
    val slots by viewModel.scheduleSlots.observeAsState(initial = emptyList())
    val isLoadingSchedule by viewModel.isLoadingSchedule.observeAsState(initial = false)
    val tasks by viewModel.tasks.observeAsState(initial = emptyList())
    
    var scheduleTaskName by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var selectedColorHex by remember { mutableStateOf("#00FFCC") } // Default neon green
    
    var goalTaskName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") } // High, Medium, Low
    var showCompletedTasks by remember { mutableStateOf(false) }
    
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }
    val colorOptions = listOf(
        "#00FFCC", // Neon Green
        "#38BDF8", // Electric Blue
        "#A855F7", // Purple
        "#FF5555", // Hot Pink
        "#FFCC00", // Vivid Yellow
        "#FB923C" // Soft Orange
    )
    // Define colors
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val greenColor = MaterialTheme.colorScheme.secondary
    val redColor = MaterialTheme.colorScheme.error
    val yellowColor = MaterialTheme.colorScheme.tertiary
    val whiteText = MaterialTheme.colorScheme.onBackground
    val mutedText = Color(0xFF94A3B8)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Daily Plan",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Slot", color = whiteText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            placeholder = { Text("Start", color = mutedText) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tealColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            placeholder = { Text("End", color = mutedText) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tealColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = scheduleTaskName,
                        onValueChange = { scheduleTaskName = it },
                        placeholder = { Text("Task name", color = mutedText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tealColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Task Add Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(tealColor, greenColor)))
                            .clickable {
                                if (scheduleTaskName.isNotBlank()) {
                                    viewModel.addScheduleSlot(startTime, endTime, scheduleTaskName, selectedColorHex)
                                    scheduleTaskName = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add to Plan", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Plan List
        items(slots.sortedBy { it.startTime }, key = { it.id }) { slot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(Color(android.graphics.Color.parseColor(slot.colorHex)))
                    )
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(slot.taskName, color = whiteText, fontWeight = FontWeight.Bold)
                            Text("${slot.startTime} - ${slot.endTime}", 
                                color = mutedText, 
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(
onClick = { 
                            viewModel.setSessionLabel(slot.taskName)
                            navController.navigate("study") 
  }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Study", tint = tealColor)
                        }
                        IconButton(onClick = { viewModel.deleteScheduleSlot(slot) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = redColor)
                        }
                    }
                }
            }
        }
        if (isLoadingSchedule && slots.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.height(52.dp).fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00DFC1))
                }
            }
        } else if (slots.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.DateRange,
                        message = "No plan yet!",
                        subtitle = "Plan your day 📅"
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Today's Goals",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Add goal Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Task", color = whiteText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = goalTaskName,
                        onValueChange = { goalTaskName = it },
                        placeholder = { Text("Task description...", color = mutedText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tealColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("High" to Color.White, "Medium" to tealColor, "Low" to Color.Gray).forEach { (p, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (priority == p) color else Color(0xFF1D2121))
                                    .clickable { priority = p },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, color = if (priority == p) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(tealColor, greenColor)))
                            .clickable {
                                if (goalTaskName.isNotBlank()) {
                                    viewModel.addTask(goalTaskName, priority)
                                    goalTaskName = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No goals for today.", color = mutedText)
                }
            }
        } else {
            items(activeTasks, key = { it.id }) { task ->
                val priorityColor = when (task.priority) {
                    "High" -> MaterialTheme.colorScheme.error
                    "Medium" -> MaterialTheme.colorScheme.tertiary
                    "Low" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primary
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                        } else {
                            priorityColor.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTaskComplete(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = priorityColor,
                                uncheckedColor = priorityColor.copy(alpha = 0.6f),
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(priorityColor)
                                )
                                Text(
                                    text = "${task.priority.uppercase()} PRIORITY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = priorityColor
                                )
                            }
                        }
                        IconButton(
onClick = { viewModel.deleteTask(task) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
   )
                        }
                    }
                }
            }
            if (completedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCompletedTasks = !showCompletedTasks }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Completed (${completedTasks.size})", color = mutedText, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (showCompletedTasks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Completed Tasks",
                            tint = mutedText
                        )
                    }
                }
                if (showCompletedTasks) {
                    items(completedTasks, key = { it.id }) { task ->
                        val priorityColor = when (task.priority) {
                            "High" -> MaterialTheme.colorScheme.error
                            "Medium" -> MaterialTheme.colorScheme.tertiary
                            "Low" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), // Added bottom padding to separate items in the visual list 
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                } else {
                                    priorityColor.copy(alpha = 0.4f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskComplete(task) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = priorityColor,
                                        uncheckedColor = priorityColor.copy(alpha = 0.6f),
                                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                        color = if (task.isCompleted) {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(priorityColor)
                                        )
                                        Text(
                                            text = "${task.priority.uppercase()} PRIORITY",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = priorityColor
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteTask(task) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Task",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
   )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: MainViewModel) {
    val notes by viewModel.notes.observeAsState(initial = emptyList())
    val isLoadingNotes by viewModel.isLoadingNotes.observeAsState(initial = false)
    // Colors
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val whiteText = MaterialTheme.colorScheme.onBackground
    val redColor = MaterialTheme.colorScheme.error
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Quick & Detailed Note Cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f).height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, tealColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Edit, "Quick Note", tint = tealColor, modifier = Modifier.size(32.dp))
                        Text("Quick Note", color = whiteText, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, tealColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Edit, "Detailed Note", tint = tealColor, modifier = Modifier.size(32.dp))
                        Text("Detailed Note", color = whiteText, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        // Saved Notes List
        if (notes.isEmpty() && !isLoadingNotes) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Edit,
                    message = "No notes yet!",
                    subtitle = "Start writing 📝"
                )
            }
        } else {
            items(notes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, color = whiteText, fontWeight = FontWeight.Bold)
                            Text(note.content, color = mutedText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { viewModel.deleteNote(note) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = redColor)
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentSetupScreen(viewModel: MainViewModel) {
    var selectedOption by remember { mutableStateOf<String?>(null) } // "1m", "2m", "3m", "custom"
    var customDaysString by remember { mutableStateOf("45") }
    var isError by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("commitment_setup_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Logo-like indicator
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "CHOOSE YOUR COMMITMENT",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Welcome to LockedIn. Establish your study commitment goal. This locks your focus until the countdown completes.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        // Option 1 month
        CommitOptionButton(
            title = "1 Month",
            subtitle = "Recommended for moderate sprints",
            daysText = "30 Days",
            selected = selectedOption == "1m",
            onClick = { selectedOption = "1m" },
            modifier = Modifier.height(52.dp).testTag("btn_1_month")
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Option 2 months
        CommitOptionButton(
            title = "2 Months",
            subtitle = "Ideal for exam preparation cycles",
            daysText = "60 Days",
            selected = selectedOption == "2m",
            onClick = { selectedOption = "2m" },
            modifier = Modifier.height(52.dp).testTag("btn_2_months")
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Option 3 months
        CommitOptionButton(
            title = "3 Months",
            subtitle = "Mastery and long-term skill acquisition",
            daysText = "90 Days",
            selected = selectedOption == "3m",
            onClick = { selectedOption = "3m" },
            modifier = Modifier.height(52.dp).testTag("btn_3_months")
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Custom option
        CommitOptionButton(
            title = "Custom Days",
            subtitle = "Define your own study marathon",
            daysText = if (selectedOption == "custom") "$customDaysString Days" else "Flexible",
            selected = selectedOption == "custom",
            onClick = { selectedOption = "custom" },
            modifier = Modifier.height(52.dp).testTag("btn_custom_days")
        )
        if (selectedOption == "custom") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = customDaysString,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        customDaysString = input
                        val days = input.toIntOrNull() ?: 0
                        isError = days <= 0 || days > 365
                    }
                },
                label = { Text("Numbers of days (1 - 365)") },
                isError = isError,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_days_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            if (isError) {
                Text(
                    text = "Please enter a valid duration between 1 and 365 days.",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // CTA Lock in button
        Button(
onClick = {
                val days = when (selectedOption) {
                    "1m" -> 30
                    "2m" -> 60
                    "3m" -> 90
                    "custom" -> customDaysString.toIntOrNull() ?: 30
                    else -> 0
                }
                if (days in 1..365) {
                    viewModel.saveCommitment(days)
                }
            },
            enabled = selectedOption != null && (!isError || selectedOption != "custom"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
   ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_save_commitment")
        ) {
            Text(
                text = "LOCK IN MY COMMITMENT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
@Composable
fun CommitOptionButton(
    title: String,
    subtitle: String,
    daysText: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = daysText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignupScreen(viewModel: MainViewModel, onPrivacyPolicyClick: () -> Unit = {}) {
    var isLoginMode by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoadingAuth.observeAsState(initial = false)
    val error by viewModel.authError.observeAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.signInWithGoogle(token)
                }
            } catch (e: com.google.android.gms.common.api.ApiException) {
                // handle exceptions
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Rounded square container for lock icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF00DFC1),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "LockedIn",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Deep focus for the digital generation.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE2E2E2)
        )
        Spacer(modifier = Modifier.height(32.dp))
        // Toggle Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1D2121)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pillModifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
            
            Box(
                modifier = pillModifier
                    .background(if (isLoginMode) Color(0xFF00DFC1) else Color.Transparent)
                    .clickable { isLoginMode = true },
                contentAlignment = Alignment.Center
            ) {
                Text("Login", color = if (isLoginMode) Color.Black else Color.White)
            }
            Box(
                modifier = pillModifier
                    .background(if (!isLoginMode) Color(0xFF00DFC1) else Color.Transparent)
                    .clickable { isLoginMode = false },
                contentAlignment = Alignment.Center
            ) {
                Text("Signup", color = if (!isLoginMode) Color.Black else Color.White)
            }
        }
        if (error != null) {
            MessageCard(message = error!!, isError = true)
        }
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.animation.AnimatedVisibility(
            visible = !isLoginMode,
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
        ) {
            Column {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; viewModel.dismissAuthError() },
                    label = { Text("Full Name", color = Color(0xFFE2E2E2)) },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().testTag("name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00DFC1),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.dismissAuthError() },
            label = { Text("Email", color = Color(0xFFE2E2E2)) },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().testTag("email_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00DFC1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.dismissAuthError() },
            label = { Text("Password", color = Color(0xFFE2E2E2)) },
            maxLines = 1,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("password_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00DFC1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        if (isLoginMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text("Forgot Password?", color = Color(0xFF00DFC1))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
        // Gradient Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF00DFC1), Color(0xFF6FFE00))))
                .clickable(enabled = !isLoading) {
                    if (isLoginMode) {
                        viewModel.signIn(email, password)
                    } else {
                        viewModel.signUp(email, password, fullName)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = { 
                val activity = context as? MainActivity
                activity?.let {
                    googleSignInLauncher.launch(it.googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.height(52.dp).fillMaxWidth().border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
        ) {
            Text("Continue with Google", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onPrivacyPolicyClick,
            modifier = Modifier.height(52.dp).fillMaxWidth()
        ) {
            Text("Privacy Policy", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
        }
        if (showForgotPasswordDialog) {
            var resetEmail by remember { mutableStateOf("") }
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Reset Password") },
                text = {
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
onClick = {
                        viewModel.sendPasswordResetEmail(resetEmail)
                        showForgotPasswordDialog = false
  }) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(
modifier = Modifier.fillMaxWidth().height(52.dp), 
onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
   )
        }
    }
}
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val dots = listOf(
        androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) },
        androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) },
        androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    )
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        dots.forEachIndexed { index, animatable ->
            launch {
                kotlinx.coroutines.delay(index * 200L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(
                            durationMillis = 600,
                            easing = androidx.compose.animation.core.LinearEasing
                        ),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    )
                )
            }
        }
    }
    
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer { translationY = -animatable.value * 12f }
                    .background(Color(0xFF00DFC1), CircleShape)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(viewModel: MainViewModel) {
    val tasks by viewModel.tasks.observeAsState(initial = emptyList())
    val notes by viewModel.notes.observeAsState(initial = emptyList())
    val streak by viewModel.streak.observeAsState(initial = 0)
    val userXP by viewModel.userXP.observeAsState(initial = 0)
    val userName by viewModel.userName.observeAsState(initial = "User")
    val scheduleSlots by viewModel.scheduleSlots.observeAsState(initial = emptyList())
    
    val currentLevel = (userXP / 100) + 1
    
    var messageText by remember { mutableStateOf("") }
    val lastChatMessages by viewModel.lastChatMessages.observeAsState(initial = emptyList())
    var isLoading by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val rawResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = rawResults?.firstOrNull()
            if (recognizedText != null) {
                messageText = recognizedText
            }
        }
    }
    if (showClearConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Chat History", color = Color.White) },
            text = { Text("Are you sure you want to clear the entire conversation? This action cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                androidx.compose.material3.TextButton(
onClick = {
                        viewModel.clearChatMessages()
                        showClearConfirmDialog = false
                    }
   ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
onClick = { showClearConfirmDialog = false }
   ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
    // Black Theme
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val tealColor = MaterialTheme.colorScheme.primary
    val whiteText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .imePadding()
    ) {
        // Redesigned Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Star, "Sparkle", tint = tealColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Harry", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Your study coach", color = mutedText, style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = { showClearConfirmDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Chat History", tint = mutedText)
            }
        }
        // Chat messages
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.height(52.dp)
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Icon(Icons.Default.Person, "Harry", tint = Color(0xFF00DFC1), modifier = Modifier.size(24.dp).padding(end = 4.dp))
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF1E2020),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .widthIn(max = 250.dp)
                        ) {
                            TypingIndicator()
                        }
                    }
                }
            }
            items(lastChatMessages, key = { it.id }) { msg ->
                MessageBubble(text = msg.text, isUser = msg.isUser)
            }
            if (lastChatMessages.isEmpty()) {
                item {
                    MessageBubble(text = "Hey! I am Harry, your study coach. I can see your progress. Ask me anything or pick a suggestion below.", isUser = false)
                }
            }
        }
        // Suggested Chips
        if (lastChatMessages.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf("Make me a study plan", "Quiz me on a topic", "How am I doing?")
                suggestions.forEach { suggestion ->
                    Surface(
                        onClick = { messageText = suggestion },
                        color = cardColor,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, tealColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = suggestion,
                            color = whiteText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask me anything...", color = mutedText) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor,
                    focusedTextColor = whiteText,
                    unfocusedTextColor = whiteText,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    }
                    speechRecognizerLauncher.launch(intent)
                },
                modifier = Modifier.height(52.dp).background(Color(0xFF333333), shape = androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
onClick = {
                    if (messageText.isNotBlank() && !isLoading) {
                        val userMsg = messageText
                        viewModel.addChatMessage(userMsg, true)
                        messageText = ""
                        isLoading = true
                        
                        coroutineScope.launch {
                            try {
                                val systemPrompt = """
                                    You are Harry, a dedicated, energetic, and encouraging study coach. You use Gen Z friendly language, and keep your responses concise, helpful, and hype.
                                    Here is context about the user's current data:
                                    Name: $userName
                                    Level: $currentLevel
                                    Streak: $streak
                                    XP: $userXP
                                    
                                    Tasks:
                                    ${tasks.joinToString("\n") { "- ${it.name} (completed: ${it.isCompleted}, priority: ${it.priority})" }}
                                    
                                    Today's Planned Tasks (Schedule):
                                    ${scheduleSlots.joinToString("\n") { "- ${it.taskName} (${it.startTime} - ${it.endTime})" }}
                                    
                                    Notes:
                                    ${notes.joinToString("\n") { "- ${it.title}: ${it.content.take(50)}..." }}
                                """.trimIndent() + "\nIf the user wants to add a task, respond ONLY with this JSON format: {\"action\": \"addTask\", \"taskName\": \"<task_name>\"}. If the user wants to add a plan, respond ONLY with this JSON format: {\"action\": \"addSchedule\", \"taskName\": \"<task_name>\", \"startTime\": \"<start_time>\", \"endTime\": \"<end_time>\"}. If the user wants to add a note, respond ONLY with this JSON format: {\"action\": \"addNote\", \"title\": \"<title>\", \"content\": \"<content>\"}. If the user wants to create a personalized study plan, generate a natural language plan, and if the user approves, output ONLY this JSON format: {\"action\": \"addScheduleSlots\", \"slots\": [{\"taskName\": \"<task_name>\", \"startTime\": \"<start_time>\", \"endTime\": \"<end_time>\"}, ...]}. For these actions, do not include any other text. If the user asks for motivation, provide a personalized, encouraging message based on their current streak and XP. Otherwise, provide a helpful, natural language response, keeping the 'Harry' persona in mind."
                                val request = GenerateContentRequest(
                                    contents = listOf(Content(
                                        parts = listOf(Part(text = userMsg))
  )),
                                    systemInstruction = Content(
                                        parts = listOf(Part(text = systemPrompt))
                                    )
                                )
                                val apiKey = BuildConfig.GEMINI_API_KEY
                                val response = RetrofitClient.service.generateContent(apiKey, request)
                                val aiMsg = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
                                
                                var handled = false
                                try {
                                    var cleanMsg = aiMsg.trim()
                                    if (cleanMsg.startsWith("```json")) {
                                        cleanMsg = cleanMsg.removePrefix("```json").removeSuffix("```").trim()
                                    } else if (cleanMsg.startsWith("```")) {
                                        cleanMsg = cleanMsg.removePrefix("```").removeSuffix("```").trim()
                                    }
                                    if (cleanMsg.startsWith("{") && cleanMsg.endsWith("}")) {
                                        val json = JSONObject(cleanMsg)
                                        if (json.optString("action") == "addTask") {
                                            val taskName = json.optString("taskName")
                                            if (taskName.isNotEmpty()) {
                                                viewModel.addTask(taskName, "Medium")
                                                viewModel.addChatMessage("Added task: $taskName", false)
                                                handled = true
                                            }
                                        } else if (json.optString("action") == "addSchedule") {
                                            val taskName = json.optString("taskName")
                                            val startTime = json.optString("startTime")
                                            val endTime = json.optString("endTime")
                                            if (taskName.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                                                viewModel.addScheduleSlot(startTime, endTime, taskName, "#E57373")
                                                viewModel.addChatMessage("Added plan: $taskName from $startTime to $endTime", false)
                                                handled = true
                                            }
                                        } else if (json.optString("action") == "addNote") {
                                            val title = json.optString("title")
                                            val content = json.optString("content")
                                            if (content.isNotEmpty()) {
                                                viewModel.addNote("Quick", title.ifEmpty { "Note from AI" }, content)
                                                viewModel.addChatMessage("Added note: ${title.ifEmpty { "Untitled" }}", false)
                                                handled = true
                                            }
                                        } else if (json.optString("action") == "addScheduleSlots") {
                                            val slots = json.optJSONArray("slots")
                                            if (slots != null) {
                                                for (i in 0 until slots.length()) {
                                                    val slot = slots.getJSONObject(i)
                                                    val taskName = slot.optString("taskName")
                                                    val startTime = slot.optString("startTime")
                                                    val endTime = slot.optString("endTime")
                                                    if (taskName.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                                                        viewModel.addScheduleSlot(startTime, endTime, taskName, "#66BB6A")
                                                    }
                                                }
                                                viewModel.addChatMessage("Added your study plan!", false)
                                                handled = true
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                }
                                if (!handled) {
                                    viewModel.addChatMessage(aiMsg, false)
                                }
                            } catch (e: Exception) {
                                viewModel.addChatMessage("Error: ${e.message}", false)
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.height(52.dp).background(tealColor, shape = androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}
@Composable
fun MessageBubble(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Icon(Icons.Default.Person, "Harry", tint = Color(0xFF00DFC1), modifier = Modifier.size(24.dp).padding(end = 4.dp))
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .background(
                    if (isUser) Color(0xFF00DFC1) else Color(0xFF1E2020),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) Color.Black else Color(0xFFE2E2E2),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}