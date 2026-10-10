package com.example.traildex

import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.traildex.theme.SurfaceCream
import com.example.traildex.theme.TrailDexTheme
import com.example.traildex.ui.components.RetroBottomNav
import com.example.traildex.ui.components.RetroTopBar
import com.example.traildex.ui.screens.BattleScreen
import com.example.traildex.ui.screens.CardsScreen
import com.example.traildex.ui.screens.ScoutScreen
import com.example.traildex.ui.screens.TrailScreen
import com.example.traildex.data.FieldCard
import com.example.traildex.data.FieldCardStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TrailDexTheme {
                var currentTab by remember { mutableIntStateOf(0) }
                val appContext = applicationContext
                val scoutPrefs = remember { getSharedPreferences("traildex_scout", MODE_PRIVATE) }
                var guideVisible by remember { mutableStateOf(!scoutPrefs.getBoolean("intro_complete", false)) }
                var guidePage by remember { mutableIntStateOf(0) }
                var fieldCards by remember { mutableStateOf(FieldCardStore.load(appContext)) }
                var trailDistanceMeters by remember { mutableFloatStateOf(getSharedPreferences("trail_session", MODE_PRIVATE).getFloat("distance_m", 0f)) }
                var duelWins by remember { mutableIntStateOf(scoutPrefs.getInt("wins", 0)) }
                var duelLosses by remember { mutableIntStateOf(scoutPrefs.getInt("losses", 0)) }
                DisposableEffect(appContext) {
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            trailDistanceMeters = intent.getFloatExtra(TrailLocationService.EXTRA_DISTANCE, trailDistanceMeters)
                        }
                    }
                    ContextCompat.registerReceiver(appContext, receiver, IntentFilter(TrailLocationService.ACTION_UPDATE), ContextCompat.RECEIVER_NOT_EXPORTED)
                    onDispose { appContext.unregisterReceiver(receiver) }
                }

                val screenTitles = listOf(
                    "TRAIL",
                    "DEX & CARDS",
                    "BATTLE",
                    "SCOUT LOG"
                )

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    topBar = {
                        RetroTopBar(screenTitle = screenTitles[currentTab])
                    },
                    bottomBar = {
                        RetroBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    },
                    containerColor = SurfaceCream
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(SurfaceCream)
                    ) {
                        when (currentTab) {
                            0 -> TrailScreen(
                                onNavigateToDex = { currentTab = 1 },
                                onDistanceChanged = { trailDistanceMeters = it }
                            )
                            1 -> CardsScreen(
                                cards = fieldCards,
                                onSaveCard = { card: FieldCard ->
                                    FieldCardStore.add(appContext, card)
                                    fieldCards = FieldCardStore.load(appContext)
                                }
                            )
                            2 -> BattleScreen(
                                cards = fieldCards,
                                onBattleComplete = { won ->
                                    if (won) duelWins++ else duelLosses++
                                    scoutPrefs.edit().putInt("wins", duelWins).putInt("losses", duelLosses).apply()
                                }
                            )
                            3 -> ScoutScreen(
                                cards = fieldCards,
                                trailDistanceKm = trailDistanceMeters / 1000f,
                                duelWins = duelWins,
                                duelLosses = duelLosses,
                                onNavigateToTrail = { currentTab = 0 },
                                onNavigateToCards = { currentTab = 1 }
                            )
                        }
                    }
                }
                if (guideVisible) {
                    val guide = listOf(
                        "STEP 1 • TAKE A WALK" to "Start a GPS walk. TrailDex draws your real route and saves it on this phone when you finish. You can pick two map points to open walking directions.",
                        "STEP 2 • COLLECT A FIELD CARD" to "Photograph or describe a bird, plant, insect, or other find. Save its card and haiku to your local collection; Ollama AI is optional.",
                        "STEP 3 • BATTLE & TRACK PROGRESS" to "Choose any saved card for an offline practice battle. Scout Log shows your cards, walks, wins, and badges."
                    )
                    AlertDialog(
                        onDismissRequest = { guideVisible = false; scoutPrefs.edit().putBoolean("intro_complete", true).apply() },
                        title = { Text("WELCOME TO TRAILDEX\n${guide[guidePage].first}") },
                        text = { Text(guide[guidePage].second) },
                        confirmButton = {
                            TextButton(onClick = {
                                if (guidePage < guide.lastIndex) guidePage++
                                else { guideVisible = false; scoutPrefs.edit().putBoolean("intro_complete", true).apply() }
                            }) { Text(if (guidePage < guide.lastIndex) "NEXT" else "START EXPLORING") }
                        },
                        dismissButton = { TextButton(onClick = { guideVisible = false; scoutPrefs.edit().putBoolean("intro_complete", true).apply() }) { Text("SKIP TOUR") } }
                    )
                }
            }
        }
    }
}
