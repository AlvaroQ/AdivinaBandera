package com.alvaroquintana.adivinabandera.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.lifecycle.lifecycleScope
import com.alvaroquintana.adivinabandera.BuildConfig
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.application.AdivinaApp
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.managers.XpSyncManager
import com.alvaroquintana.adivinabandera.ui.animation.NavTransitions
import com.alvaroquintana.adivinabandera.ui.composables.AdBannerView
import com.alvaroquintana.adivinabandera.ui.composables.GameAppBar
import com.alvaroquintana.adivinabandera.ui.composables.rememberRewardedAdState
import com.alvaroquintana.adivinabandera.ui.game.GameScreen
import com.alvaroquintana.adivinabandera.ui.game.GameViewModel
import com.alvaroquintana.adivinabandera.ui.leaderboard.XpLeaderboardScreen
import com.alvaroquintana.adivinabandera.ui.leaderboard.XpLeaderboardViewModel
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.toGameMode
import com.alvaroquintana.domain.toRouteString
import com.alvaroquintana.adivinabandera.notifications.NotificationScheduler
import com.alvaroquintana.adivinabandera.ui.main.MainScreen
import com.alvaroquintana.adivinabandera.ui.navigation.Game
import com.alvaroquintana.adivinabandera.ui.navigation.Main
import com.alvaroquintana.adivinabandera.ui.navigation.Practice
import com.alvaroquintana.adivinabandera.ui.navigation.Result
import com.alvaroquintana.adivinabandera.ui.navigation.Settings
import com.alvaroquintana.adivinabandera.ui.navigation.Shop
import com.alvaroquintana.adivinabandera.ui.navigation.Splash
import com.alvaroquintana.adivinabandera.ui.navigation.XpLeaderboard
import com.alvaroquintana.adivinabandera.ui.shop.ShopScreen
import com.alvaroquintana.adivinabandera.ui.shop.ShopViewModel
import com.alvaroquintana.adivinabandera.ui.result.ResultScreen
import com.alvaroquintana.adivinabandera.ui.result.ResultViewModel
import com.alvaroquintana.adivinabandera.ui.settings.SettingsScreen
import com.alvaroquintana.adivinabandera.ui.splash.SplashScreen
import com.alvaroquintana.adivinabandera.ui.theme.AdivinaBanderaTheme
import com.alvaroquintana.adivinabandera.ui.theme.ThemeMode
import com.alvaroquintana.adivinabandera.ui.theme.rememberWindowSizeClass
import com.alvaroquintana.adivinabandera.utils.Constants.TOTAL_COUNTRIES
import com.alvaroquintana.adivinabandera.utils.log
import com.alvaroquintana.adivinabandera.utils.rateApp
import com.alvaroquintana.adivinabandera.utils.shareApp
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private val isMobileAdsInitialized = AtomicBoolean(false)
    private val xpSyncManager: XpSyncManager
        get() = (application as AdivinaApp).appGraph.xpSyncManager
    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FirebaseFirestore.setLoggingEnabled(BuildConfig.DEBUG)

        auth = Firebase.auth
        Analytics.initialize(this)
        crashlytics.apply {
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("build_number", BuildConfig.VERSION_CODE)
            setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            log("session_started")
        }

        // Initialize ads immediately — no UMP/consent flow in this app
        initializeMobileAdsSdk()

        // Schedule daily reminder notification (no-op if already scheduled)
        NotificationScheduler.scheduleDailyReminder(this)

        val viewModelFactory = (application as AdivinaApp).appGraph.viewModelFactory

        setContent {
            val prefs = remember {
                getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
            }
            var themeMode by remember {
                mutableStateOf(
                    try {
                        ThemeMode.valueOf(
                            prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
                        )
                    } catch (e: Exception) {
                        crashlytics.log("theme_mode_fallback_main")
                        crashlytics.recordException(e)
                        ThemeMode.SYSTEM
                    }
                )
            }

            val windowSizeClass = rememberWindowSizeClass()

            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                AdivinaBanderaTheme(themeMode = themeMode, windowSizeClass = windowSizeClass) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        onThemeModeChanged = { mode ->
                            themeMode = mode
                            prefs.edit { putString("theme_mode", mode.name) }
                        }
                    )
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
        if (currentUser != null) {
            lifecycleScope.launch { xpSyncManager.syncPendingIfNeeded() }
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    log(tag, "signInAnonymously:success")
                    crashlytics.log("auth_anonymous_sign_in_success")
                    val user = auth.currentUser
                    updateUI(user)
                    lifecycleScope.launch { xpSyncManager.syncPendingIfNeeded() }
                } else {
                    log(tag, "signInAnonymously:failure", task.exception)
                    crashlytics.log("auth_anonymous_sign_in_failed")
                    crashlytics.recordException(
                        task.exception ?: IllegalStateException("Anonymous sign-in failed without exception")
                    )
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        val isSignedIn = user != null
        log(tag, "updateUI, isSignedIn = $isSignedIn")
        crashlytics.setCustomKey("user_signed_in", isSignedIn)

        if (!isSignedIn) {
            crashlytics.log("auth_user_not_signed_in")
            signInAnonymously()
        } else {
            crashlytics.log("auth_user_signed_in")
            crashlytics.setUserId(user.uid)
            log(tag, "updateUI: logged in")
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitialized.getAndSet(true)) return
        MobileAds.initialize(this)
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        FirebaseCrashlytics.getInstance().setCustomKey("current_screen", "NavHost")
    }

    NavHost(
        navController = navController,
        startDestination = Splash,
        enterTransition = { NavTransitions.enterTransition },
        exitTransition = { NavTransitions.exitTransition },
        popEnterTransition = { NavTransitions.popEnterTransition },
        popExitTransition = { NavTransitions.popExitTransition }
    ) {
        composable<Splash>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition }
        ) {
            SplashScreen(
                onNavigateToSelect = {
                    navController.navigate(Main) {
                        popUpTo<Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Main>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { NavTransitions.fadeEnterTransition },
            popExitTransition = { ExitTransition.None }
        ) {
            MainScreen(rootNavController = navController)
        }

        composable<Game> { backStackEntry ->
            val route: Game = backStackEntry.toRoute()
            GameRoute(navController = navController, mode = route.mode)
        }

        composable<Practice> { backStackEntry ->
            val route: Practice = backStackEntry.toRoute()
            PracticeRoute(navController = navController, countryIds = route.countryIds)
        }

        composable<Result>(
            enterTransition = { NavTransitions.resultEnterTransition },
            exitTransition = { NavTransitions.resultExitTransition },
            popEnterTransition = { NavTransitions.resultEnterTransition },
            popExitTransition = { NavTransitions.resultExitTransition }
        ) { backStackEntry ->
            val result: Result = backStackEntry.toRoute()
            ResultRoute(navController = navController, result = result)
        }

        composable<Settings>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition },
            popEnterTransition = { NavTransitions.fadeEnterTransition },
            popExitTransition = { NavTransitions.fadeExitTransition }
        ) {
            SettingsRoute(
                navController = navController,
                onThemeModeChanged = onThemeModeChanged
            )
        }

        composable<XpLeaderboard>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition },
            popEnterTransition = { NavTransitions.fadeEnterTransition },
            popExitTransition = { NavTransitions.fadeExitTransition }
        ) {
            XpLeaderboardRoute(navController = navController)
        }

        composable<Shop>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition },
            popEnterTransition = { NavTransitions.fadeEnterTransition },
            popExitTransition = { NavTransitions.fadeExitTransition }
        ) {
            ShopRoute(navController = navController)
        }
    }
}

// region Game Route

@Composable
private fun PracticeRoute(navController: NavHostController, countryIds: List<Int>) {
    val viewModel: GameViewModel = assistedMetroViewModel<GameViewModel, GameViewModel.Factory> {
        create(GameMode.Classic, countryIds)
    }
    val context = LocalContext.current

    var life by rememberSaveable { mutableIntStateOf(3) }
    var stage by rememberSaveable { mutableIntStateOf(1) }
    var points by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        FirebaseCrashlytics.getInstance().setCustomKey("current_screen", "Practice")
        FirebaseCrashlytics.getInstance().setCustomKey("game_mode", "ClassicPractice")
    }

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { navigation ->
            when (navigation) {
                is GameViewModel.Navigation.Result -> {
                    navController.navigate(
                        Result(
                            points = points,
                            correctAnswers = navigation.correctAnswers,
                            totalQuestions = navigation.totalQuestions,
                            bestStreak = navigation.bestStreak,
                            timePlayedMs = navigation.timePlayedMs,
                            completedAllQuestions = navigation.completedAllQuestions,
                            gameMode = "Classic"
                        )
                    ) {
                        popUpTo<Main>()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.streakMessage.collect { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    GameScreenLayout(
        title = stringResource(R.string.practice_title),
        onBackClick = { navController.popBackStack() },
        showLives = true,
        lives = life,
        showBanner = false
    ) {
        GameScreen(
            viewModel = viewModel,
            stage = stage,
            lives = life,
            points = points,
            onAnswerSelected = { selectedIndex, correctAnswer, options ->
                val selectedAnswer = options.getOrElse(selectedIndex) { "" }
                val isCorrect = selectedAnswer == correctAnswer

                if (isCorrect) {
                    points += 1
                    viewModel.onCorrectAnswer()
                } else {
                    life--
                    viewModel.onWrongAnswer()
                }
                stage += 1
            }
        )
    }

    LaunchedEffect(stage) {
        if (stage > 1) {
            delay(TimeUnit.MILLISECONDS.toMillis(1000))
            if (life < 1) {
                viewModel.navigateToResult(
                    points = points.toString(),
                    totalQuestions = stage - 1,
                    completedAllQuestions = false
                )
            } else {
                viewModel.generateNewStage()
            }
        }
    }
}

@Composable
private fun GameRoute(navController: NavHostController, mode: String = "Classic") {
    val gameMode = mode.toGameMode()
    val viewModel: GameViewModel = assistedMetroViewModel<GameViewModel, GameViewModel.Factory> {
        create(gameMode, emptyList())
    }
    val context = LocalContext.current

    var life by rememberSaveable { mutableIntStateOf(3) }
    var stage by rememberSaveable { mutableIntStateOf(1) }
    var points by rememberSaveable { mutableIntStateOf(0) }
    var showBanner by remember { mutableStateOf(false) }

    val rewardedAdState = rememberRewardedAdState(
        adUnitId = stringResource(R.string.BONIFICADO_GAME),
        adLocation = Analytics.AD_LOC_GAME
    )
    val gameModeTitle = when (gameMode) {
        is GameMode.Classic -> stringResource(R.string.mode_classic_title)
        is GameMode.CapitalByFlag -> stringResource(R.string.play_capital_by_flag)
        is GameMode.RegionSpain,
        is GameMode.RegionMexico,
        is GameMode.RegionArgentina,
        is GameMode.RegionBrazil,
        is GameMode.RegionGermany,
        is GameMode.RegionUSA -> stringResource(R.string.mode_regions_title)
        is GameMode.CurrencyDetective -> stringResource(R.string.play_currency_detective)
        is GameMode.PopulationChallenge -> stringResource(R.string.play_population_challenge)
        is GameMode.WorldMix -> stringResource(R.string.play_world_mix)
    }

    LaunchedEffect(mode) {
        FirebaseCrashlytics.getInstance().setCustomKey("current_screen", "Game")
        FirebaseCrashlytics.getInstance().setCustomKey("game_mode", mode)
        FirebaseCrashlytics.getInstance().log("game_route_opened")
    }

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { navigation ->
            when (navigation) {
                is GameViewModel.Navigation.Result -> {
                    navController.navigate(
                        Result(
                            points = points,
                            correctAnswers = navigation.correctAnswers,
                            totalQuestions = navigation.totalQuestions,
                            bestStreak = navigation.bestStreak,
                            timePlayedMs = navigation.timePlayedMs,
                            completedAllQuestions = navigation.completedAllQuestions,
                            gameMode = mode
                        )
                    ) {
                        popUpTo<Main>()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showingAds.collect { model ->
            when (model) {
                is GameViewModel.UiModel.ShowBannerAd -> showBanner = model.show
                is GameViewModel.UiModel.ShowRewardedAd -> rewardedAdState.show()
                else -> {}
            }
        }
    }

    // Mostrar mensajes de racha como Toast
    LaunchedEffect(Unit) {
        viewModel.streakMessage.collect { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    GameScreenLayout(
        title = gameModeTitle,
        onBackClick = { navController.popBackStack() },
        showLives = true,
        lives = life,
        showBanner = showBanner,
        bannerAdUnitId = stringResource(R.string.BANNER_GAME),
        bannerAdLocation = Analytics.AD_LOC_GAME
    ) {
        GameScreen(
            viewModel = viewModel,
            stage = stage,
            lives = life,
            points = points,
            onAnswerSelected = { selectedIndex, correctAnswer, options ->
                val selectedAnswer = options.getOrElse(selectedIndex) { "" }
                val isCorrect = selectedAnswer == correctAnswer

                Analytics.analyticsGameAnswer(isCorrect, stage)
                FirebaseCrashlytics.getInstance().apply {
                    setCustomKey("current_stage", stage)
                    setCustomKey("current_score", points)
                    setCustomKey("lives_remaining", life)
                }

                if (isCorrect) {
                    points += 1
                    viewModel.onCorrectAnswer()
                } else {
                    life--
                    viewModel.onWrongAnswer()
                }

                stage += 1
            }
        )
    }

    LaunchedEffect(stage) {
        if (stage > 1) {
            delay(TimeUnit.MILLISECONDS.toMillis(1000))
            if (stage > TOTAL_COUNTRIES || life < 1) {
                val completedAll = stage > TOTAL_COUNTRIES && life >= 1
                viewModel.navigateToResult(
                    points = points.toString(),
                    totalQuestions = stage - 1,
                    completedAllQuestions = completedAll
                )
            } else {
                viewModel.generateNewStage()
                if (stage != 0 && stage % 6 == 0) viewModel.showRewardedAd()
            }
        }
    }
}

// endregion

// region Result Route

@Composable
private fun ResultRoute(navController: NavHostController, result: Result) {
    val viewModel: ResultViewModel = metroViewModel()
    val context = LocalContext.current
    val gamePoints = result.points

    LaunchedEffect(result.gameMode) {
        FirebaseCrashlytics.getInstance().setCustomKey("current_screen", "Result")
        FirebaseCrashlytics.getInstance().setCustomKey("game_mode", result.gameMode)
    }

    LaunchedEffect(Unit) {
        viewModel.initWithGameMode(result.gameMode)
        viewModel.getPersonalRecord(gamePoints)
        viewModel.setPersonalRecordOnServer(gamePoints)
        viewModel.processEngagement(
            correctAnswers = result.correctAnswers,
            totalQuestions = result.totalQuestions,
            bestStreak = result.bestStreak,
            timePlayedMs = result.timePlayedMs,
            completedAllQuestions = result.completedAllQuestions,
            gameMode = result.gameMode
        )
    }

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { navigation ->
            when (navigation) {
                ResultViewModel.Navigation.Game -> {
                    navController.navigate(Main) {
                        popUpTo<Main> { inclusive = true }
                    }
                }
                ResultViewModel.Navigation.Rate -> rateApp(context)
                is ResultViewModel.Navigation.Share -> shareApp(context, navigation.points)
                ResultViewModel.Navigation.Ranking -> {
                    navController.navigate(Main) {
                        popUpTo<Main> { inclusive = false }
                    }
                }
                is ResultViewModel.Navigation.Dialog -> {
                    // Handled inside ResultScreen
                }
            }
        }
    }

    GameScreenLayout(
        title = stringResource(R.string.resultado_screen_title),
        onBackClick = {
            navController.navigate(Main) {
                popUpTo<Main> { inclusive = true }
            }
        },
        showLives = false,
        showBanner = false
    ) {
        ResultScreen(
            viewModel = viewModel,
            gamePoints = gamePoints,
            onPlayAgain = { viewModel.navigateToGame() },
            onShare = { viewModel.navigateToShare(gamePoints) },
            onRate = { viewModel.navigateToRate() },
            onViewRanking = { viewModel.navigateToRanking() }
        )
    }
}

// endregion

// region Settings Route

@Composable
private fun SettingsRoute(
    navController: NavHostController,
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_PRIVATE
        )
    }
    var isSoundEnabled by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
    var themeMode by remember {
        mutableStateOf(
            try {
                ThemeMode.valueOf(
                    prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
                )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log("theme_mode_fallback_settings")
                FirebaseCrashlytics.getInstance().recordException(e)
                ThemeMode.SYSTEM
            }
        )
    }
    val versionText = "${stringResource(R.string.settings_version)} ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"

    LaunchedEffect(Unit) {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_SETTINGS)
        FirebaseCrashlytics.getInstance().setCustomKey("current_screen", "Settings")
    }

    GameScreenLayout(
        title = stringResource(R.string.settings),
        onBackClick = { navController.popBackStack() },
        showLives = false,
        showBanner = false
    ) {
        SettingsScreen(
            isSoundEnabled = isSoundEnabled,
            themeMode = themeMode,
            versionText = versionText,
            showPrivacyOptions = false,
            onSoundToggle = { enabled ->
                isSoundEnabled = enabled
                prefs.edit { putBoolean("sound", enabled) }
            },
            onThemeModeChanged = { mode ->
                themeMode = mode
                prefs.edit { putString("theme_mode", mode.name) }
                onThemeModeChanged(mode)
            },
            onRateApp = { rateApp(context) },
            onShare = { shareApp(context, -1) },
            onPrivacyPolicy = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://sites.google.com/view/alvaroquintana-privacy-policy".toUri()
                    )
                )
            }
        )
    }
}

// endregion

// region XP Leaderboard Route

@Composable
private fun XpLeaderboardRoute(navController: NavHostController) {
    val viewModel: XpLeaderboardViewModel = metroViewModel()

    LaunchedEffect(Unit) {
        FirebaseCrashlytics.getInstance().setCustomKey("current_screen", "XpLeaderboard")
        FirebaseCrashlytics.getInstance().log("xp_leaderboard_opened")
    }

    GameScreenLayout(
        title = "Ranking de XP",
        onBackClick = { navController.popBackStack() },
        showLives = false,
        showBanner = false
    ) {
        XpLeaderboardScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }
}

// endregion

// region Shop Route

@Composable
private fun ShopRoute(navController: NavHostController) {
    val viewModel: ShopViewModel = metroViewModel()

    LaunchedEffect(Unit) {
        FirebaseCrashlytics.getInstance().setCustomKey("current_screen", "Shop")
    }

    GameScreenLayout(
        title = "Tienda",
        onBackClick = { navController.popBackStack() },
        showLives = false,
        showBanner = false
    ) {
        ShopScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }
}

// endregion

// region Shared Layout

@Composable
private fun GameScreenLayout(
    title: String,
    onBackClick: () -> Unit,
    showLives: Boolean,
    lives: Int = 0,
    showBanner: Boolean = false,
    bannerAdUnitId: String = "",
    bannerAdLocation: String = Analytics.AD_LOC_GAME,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        GameAppBar(
            title = title,
            onBackClick = onBackClick,
            showLives = showLives,
            lives = lives
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.widthIn(max = 680.dp)) {
                content()
            }
        }

        if (showBanner && bannerAdUnitId.isNotEmpty()) {
            AdBannerView(
                adUnitId = bannerAdUnitId,
                modifier = Modifier.fillMaxWidth(),
                adLocation = bannerAdLocation
            )
        }
    }
}

// endregion
