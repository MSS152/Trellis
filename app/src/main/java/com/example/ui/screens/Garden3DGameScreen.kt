package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.ui.viewmodel.HabitFlowViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// ============================================================================
// 1. DATA MODELS (3D GROUND PLANE X, Z, GROWTH STATES, HABIT DETAILS & WEATHER)
// ============================================================================

enum class PlantState(
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val trunkColor: Color
) {
    SPROUT(
        label = "Sprout",
        primaryColor = Color(0xFF81C784), // Bioluminescent Mint Green
        secondaryColor = Color(0xFFC8E6C9),
        trunkColor = Color(0xFF558B2F)
    ),
    SAPLING(
        label = "Sapling",
        primaryColor = Color(0xFF4CAF50), // Vibrant Sapling Green
        secondaryColor = Color(0xFFA5D6A7),
        trunkColor = Color(0xFF6D4C41)
    ),
    FLORA(
        label = "Flora",
        primaryColor = Color(0xFF2E7D32), // Lush Emerald Canopy
        secondaryColor = Color(0xFFFF80AB), // Luminous Blossom Pink
        trunkColor = Color(0xFF5D4037)
    ),
    GROVE(
        label = "Grove",
        primaryColor = Color(0xFF1B5E20), // Ancient Forest Canopy
        secondaryColor = Color(0xFFFFD54F), // Golden Spirit Orbs
        trunkColor = Color(0xFF4E342E)
    );

    fun next(): PlantState? = when (this) {
        SPROUT -> SAPLING
        SAPLING -> FLORA
        FLORA -> GROVE
        GROVE -> null
    }
}

/**
 * Represents a 3D Ground Tile at (x, z) linked to a specific Habit.
 */
data class FarmTile3D(
    val x: Int,
    val z: Int,
    val habitId: String?,
    val state: PlantState,
    val habitTitle: String = "Sanctuary Habit",
    val habitEmoji: String = "🌱",
    val habitCategory: String = "Wellness",
    val habitFrequency: String = "DAILY",
    val streakCount: Int = 1,
    val momentumScore: Int = 100,
    val isCompletedToday: Boolean = false
)

/**
 * Dynamic Weather States driven by Particle Emitters, reacting to Time of Day or App Health Score.
 */
enum class SanctuaryWeather(
    val label: String,
    val subtitle: String,
    val badgeColor: Color,
    val ambientTint: Color
) {
    SUNBEAMS(
        label = "Sunbeams",
        subtitle = "Radiant vitality • Golden rays & pollen motes",
        badgeColor = Color(0xFFFFD54F),
        ambientTint = Color(0xFF26321E)
    ),
    RAIN(
        label = "Rain Shower",
        subtitle = "Hydrating downpour • Rain streaks & 3D ripples",
        badgeColor = Color(0xFF4FC3F7),
        ambientTint = Color(0xFF14222E)
    ),
    SNOWFALL(
        label = "Snowfall",
        subtitle = "Frosty serenity • Drifting snowflakes & ice mist",
        badgeColor = Color(0xFFB3E5FC),
        ambientTint = Color(0xFF1A202C)
    )
}

enum class WeatherControlMode(val label: String) {
    AUTO("Auto"),
    SUNBEAMS("Sunbeams"),
    RAIN("Rain"),
    SNOWFALL("Snow")
}

/**
 * Weather & Watering 3D/2D Particle representation for the 60FPS emitter system.
 */
private data class WeatherParticle(
    var x: Float,          // Normalized screen X [0f..1f]
    var y: Float,          // Normalized screen Y [0f..1f]
    var zDepth: Float,     // Depth factor [0.3f..1.2f] for parallax & size scaling
    var vx: Float,         // Horizontal velocity
    var vy: Float,         // Vertical velocity
    var size: Float,       // Base radius or streak length
    var alpha: Float,      // Opacity
    var phase: Float       // Sinusoidal sway phase for snowflakes & sun motes
)

/**
 * Expanding 3D water/rain ripple ring on the ground plane (X, Z).
 */
private data class GroundRipple3D(
    val worldX: Float,
    val worldZ: Float,
    var progress: Float,   // 0f -> 1f
    val maxRadius: Float,
    val color: Color
)

/**
 * 3D Water droplet fountain particle emitted when a plant is watered, planted, or moved.
 */
private data class WaterDroplet3D(
    var worldX: Float,
    var worldY: Float,
    var worldZ: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var life: Float,       // 1f -> 0f
    val color: Color
)

private data class HabitDisplayInfo(
    val idKey: String,
    val dbId: Long?,
    val title: String,
    val emoji: String,
    val category: String,
    val frequency: String,
    val streak: Int,
    val momentum: Int,
    val completedToday: Boolean
)

// ============================================================================
// 2. STATEFUL 3D GARDEN SCREEN ENTRY POINT (CONNECTED TO HABITFLOWVIEWMODEL)
// ============================================================================

@Composable
fun Garden3DGameScreen(
    modifier: Modifier = Modifier,
    viewModel: HabitFlowViewModel? = null,
    gridSize: Int = 8,
    initialXp: Int = 250,
    availableHabits: List<Pair<String, String>> = listOf(
        "habit_hydration" to "Morning Hydration",
        "habit_reading" to "Deep Reading (30m)",
        "habit_meditation" to "Mindful Breathing",
        "habit_mobility" to "Evening Mobility"
    )
) {
    // Collect real habits, logs, and garden health state from HabitFlowViewModel when provided
    val dbHabits by viewModel?.habits?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val dbLogs by viewModel?.habitLogs?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val dbGardenState by viewModel?.gardenState?.collectAsState() ?: remember { mutableStateOf(null) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Build unified list of habits so tapping any plant shows its full Habit details in the popup
    val unifiedHabits: List<HabitDisplayInfo> = remember(dbHabits, dbLogs, availableHabits, todayStr) {
        if (dbHabits.isNotEmpty()) {
            dbHabits.map { h ->
                val doneToday = dbLogs.any { it.habitId == h.id && it.dateString == todayStr && it.completed }
                HabitDisplayInfo(
                    idKey = "habit_${h.id}",
                    dbId = h.id,
                    title = h.title,
                    emoji = h.emoji.ifBlank { "🌿" },
                    category = h.category,
                    frequency = h.targetFrequency,
                    streak = h.currentStreak,
                    momentum = h.momentumScore,
                    completedToday = doneToday
                )
            }
        } else {
            val defaultEmojis = listOf("💧", "📚", "🧘", "🤸", "🏃", "🥗")
            val defaultCategories = listOf("Hydration", "Mind", "Mindfulness", "Fitness")
            availableHabits.mapIndexed { index, (id, title) ->
                HabitDisplayInfo(
                    idKey = id,
                    dbId = null,
                    title = title,
                    emoji = defaultEmojis[index % defaultEmojis.size],
                    category = defaultCategories[index % defaultCategories.size],
                    frequency = "DAILY",
                    streak = (index + 1) * 3,
                    momentum = 85,
                    completedToday = index % 2 == 0
                )
            }
        }
    }

    // Compute overall Sanctuary Health Score (0..100) from GardenState + Habit Momentum for Auto Weather
    val overallHealthScore: Int = remember(dbGardenState, unifiedHabits) {
        val gardenHealth = dbGardenState?.gardenHealthScore
        val avgMomentum = if (unifiedHabits.isNotEmpty()) {
            unifiedHabits.map { it.momentum }.average().roundToInt()
        } else 88
        gardenHealth ?: avgMomentum.coerceIn(0, 100)
    }

    // Detect current hour of day (0..23) for automatic weather calculation
    val currentHour = remember {
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    // Hoisted 3D Farm state keyed by (x to z) ground coordinates
    val plantedTiles = remember {
        mutableStateMapOf<Pair<Int, Int>, FarmTile3D>()
    }

    var lastUpgradedCoord by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var upgradeTriggerCount by remember { mutableIntStateOf(0) }

    // Seed initial 3D plots with real habits and automatically water/upgrade any plant whose habit gets completed
    LaunchedEffect(unifiedHabits) {
        val defaultCoords = listOf(
            3 to 3, 4 to 3, 3 to 4, 4 to 4,
            2 to 3, 5 to 3, 2 to 4, 5 to 4,
            3 to 2, 4 to 2, 3 to 5, 4 to 5
        )
        if (plantedTiles.isEmpty() && unifiedHabits.isNotEmpty()) {
            unifiedHabits.forEachIndexed { idx, habit ->
                val coord = defaultCoords.getOrElse(idx) { (idx % gridSize) to ((idx / gridSize) % gridSize) }
                val initialStage = when {
                    habit.streak >= 14 -> PlantState.GROVE
                    habit.streak >= 7 -> PlantState.FLORA
                    habit.streak >= 3 || habit.completedToday -> PlantState.SAPLING
                    else -> PlantState.entries[idx % PlantState.entries.size]
                }
                plantedTiles[coord] = FarmTile3D(
                    x = coord.first,
                    z = coord.second,
                    habitId = habit.idKey,
                    state = initialStage,
                    habitTitle = habit.title,
                    habitEmoji = habit.emoji,
                    habitCategory = habit.category,
                    habitFrequency = habit.frequency,
                    streakCount = habit.streak,
                    momentumScore = habit.momentum,
                    isCompletedToday = habit.completedToday
                )
            }
        } else if (unifiedHabits.isNotEmpty()) {
            val existingHabitIds = plantedTiles.values.mapNotNull { it.habitId }.toSet()
            plantedTiles.keys.toList().forEach { coord ->
                val tile = plantedTiles[coord] ?: return@forEach
                val matched = unifiedHabits.firstOrNull { it.idKey == tile.habitId }
                    ?: unifiedHabits.firstOrNull { it.title == tile.habitTitle }
                if (matched != null) {
                    val newlyCompleted = !tile.isCompletedToday && matched.completedToday
                    val updatedStage = if (newlyCompleted) {
                        tile.state.next() ?: tile.state
                    } else {
                        tile.state
                    }
                    plantedTiles[coord] = tile.copy(
                        habitId = matched.idKey,
                        state = updatedStage,
                        habitTitle = matched.title,
                        habitEmoji = matched.emoji,
                        habitCategory = matched.category,
                        habitFrequency = matched.frequency,
                        streakCount = matched.streak,
                        momentumScore = matched.momentum,
                        isCompletedToday = matched.completedToday
                    )
                    if (newlyCompleted) {
                        lastUpgradedCoord = coord
                        upgradeTriggerCount++
                    }
                }
            }
            unifiedHabits.forEach { habit ->
                if (habit.idKey !in existingHabitIds && plantedTiles.values.none { it.habitTitle == habit.title }) {
                    val freeCoord = defaultCoords.firstOrNull { !plantedTiles.containsKey(it) }
                    if (freeCoord != null) {
                        plantedTiles[freeCoord] = FarmTile3D(
                            x = freeCoord.first,
                            z = freeCoord.second,
                            habitId = habit.idKey,
                            state = if (habit.completedToday) PlantState.SAPLING else PlantState.SPROUT,
                            habitTitle = habit.title,
                            habitEmoji = habit.emoji,
                            habitCategory = habit.category,
                            habitFrequency = habit.frequency,
                            streakCount = habit.streak,
                            momentumScore = habit.momentum,
                            isCompletedToday = habit.completedToday
                        )
                    }
                }
            }
        }
    }

    var localXpBonus by remember { mutableIntStateOf(0) }
    val availableXp = (dbGardenState?.totalGardenXp ?: initialXp) + localXpBonus

    var selectedEmptyCoord by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedOccupiedCoord by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    // Tree Relocation ("Move Tree") mode state: holds the source (x, z) of the tree being moved
    var movingTreeSourceCoord by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Weather Control Mode: defaults to AUTO (reacting to overallHealthScore & time of day)
    var weatherMode by remember { mutableStateOf(WeatherControlMode.AUTO) }

    val activeWeather: SanctuaryWeather = remember(weatherMode, overallHealthScore, currentHour) {
        when (weatherMode) {
            WeatherControlMode.SUNBEAMS -> SanctuaryWeather.SUNBEAMS
            WeatherControlMode.RAIN -> SanctuaryWeather.RAIN
            WeatherControlMode.SNOWFALL -> SanctuaryWeather.SNOWFALL
            WeatherControlMode.AUTO -> {
                when {
                    overallHealthScore < 45 || currentHour >= 22 || currentHour < 5 -> SanctuaryWeather.SNOWFALL
                    overallHealthScore < 75 || currentHour in 17..21 -> SanctuaryWeather.RAIN
                    else -> SanctuaryWeather.SUNBEAMS
                }
            }
        }
    }

    Garden3DGameContent(
        modifier = modifier,
        gridSize = gridSize,
        plantedTiles = plantedTiles,
        availableXp = availableXp,
        currentHour = currentHour,
        weatherMode = weatherMode,
        activeWeather = activeWeather,
        onWeatherModeChange = { weatherMode = it },
        unifiedHabits = unifiedHabits,
        selectedEmptyCoord = selectedEmptyCoord,
        selectedOccupiedCoord = selectedOccupiedCoord,
        movingTreeSourceCoord = movingTreeSourceCoord,
        lastUpgradedCoord = lastUpgradedCoord,
        upgradeTriggerCount = upgradeTriggerCount,
        onGridCoordTapped = { x, z ->
            val targetCoord = x to z
            val moveSource = movingTreeSourceCoord

            if (moveSource != null) {
                // We are currently moving a tree from moveSource to targetCoord on the full garden grid
                if (moveSource == targetCoord) {
                    // Tapping the same box cancels move mode
                    movingTreeSourceCoord = null
                } else {
                    val sourceTile = plantedTiles[moveSource]
                    if (sourceTile != null) {
                        val destTile = plantedTiles[targetCoord]
                        if (destTile != null) {
                            // Swap the two trees between boxes
                            plantedTiles[targetCoord] = sourceTile.copy(x = x, z = z)
                            plantedTiles[moveSource] = destTile.copy(x = moveSource.first, z = moveSource.second)
                        } else {
                            // Move tree to the empty destination box
                            plantedTiles.remove(moveSource)
                            plantedTiles[targetCoord] = sourceTile.copy(x = x, z = z)
                        }
                        lastUpgradedCoord = targetCoord
                        upgradeTriggerCount++
                        movingTreeSourceCoord = null
                        selectedEmptyCoord = null
                        // Keep popup closed after moving so the user sees the relocated plant in the garden
                        selectedOccupiedCoord = null
                    } else {
                        movingTreeSourceCoord = null
                    }
                }
            } else {
                // Normal tap behavior: open focused 3D Plant Popup if occupied, or Plant Sheet if empty
                if (plantedTiles.containsKey(targetCoord)) {
                    selectedEmptyCoord = null
                    selectedOccupiedCoord = targetCoord
                } else {
                    selectedOccupiedCoord = null
                    selectedEmptyCoord = targetCoord
                }
            }
        },
        onStartMoveTree = { x, z ->
            // Close the plant popup and return to the full garden grid so the user can pick a destination plot
            selectedOccupiedCoord = null
            movingTreeSourceCoord = x to z
        },
        onCancelMoveTree = {
            movingTreeSourceCoord = null
        },
        onDismissEmptySheet = {
            selectedEmptyCoord = null
        },
        onDismissOccupiedFloatingUi = {
            selectedOccupiedCoord = null
            movingTreeSourceCoord = null
        },
        onPlantHabit = { x, z, habitInfo, customTitle, customEmoji ->
            if (customTitle != null && customTitle.isNotBlank()) {
                val emojiToUse = customEmoji?.ifBlank { "🌱" } ?: "🌱"
                viewModel?.addHabit(
                    title = customTitle,
                    emoji = emojiToUse,
                    category = "Sanctuary",
                    goalCount = 30
                )
                plantedTiles[x to z] = FarmTile3D(
                    x = x,
                    z = z,
                    habitId = "custom_${x}_${z}",
                    state = PlantState.SPROUT,
                    habitTitle = customTitle,
                    habitEmoji = emojiToUse,
                    habitCategory = "Sanctuary",
                    habitFrequency = "DAILY",
                    streakCount = 1,
                    momentumScore = 100,
                    isCompletedToday = false
                )
            } else if (habitInfo != null) {
                plantedTiles[x to z] = FarmTile3D(
                    x = x,
                    z = z,
                    habitId = habitInfo.idKey,
                    state = PlantState.SPROUT,
                    habitTitle = habitInfo.title,
                    habitEmoji = habitInfo.emoji,
                    habitCategory = habitInfo.category,
                    habitFrequency = habitInfo.frequency,
                    streakCount = habitInfo.streak,
                    momentumScore = habitInfo.momentum,
                    isCompletedToday = habitInfo.completedToday
                )
            }
            lastUpgradedCoord = x to z
            upgradeTriggerCount++
            selectedEmptyCoord = null
            selectedOccupiedCoord = x to z
        },
        onCompleteHabitFromGarden = { x, z ->
            val currentTile = plantedTiles[x to z] ?: return@Garden3DGameContent
            if (currentTile.isCompletedToday) {
                // Water is only used once when the habit is completed for today
                return@Garden3DGameContent
            }
            val matchedDbHabit: HabitEntity? = dbHabits.firstOrNull {
                "habit_${it.id}" == currentTile.habitId || it.title == currentTile.habitTitle
            }
            if (matchedDbHabit != null && viewModel != null) {
                viewModel.toggleHabit(matchedDbHabit.id, todayStr, true)
            } else {
                viewModel?.nurtureGarden()
            }
            localXpBonus += 30
            val nextState = currentTile.state.next() ?: currentTile.state
            plantedTiles[x to z] = currentTile.copy(
                state = nextState,
                isCompletedToday = true,
                streakCount = currentTile.streakCount + 1,
                momentumScore = min(100, currentTile.momentumScore + 10)
            )
            lastUpgradedCoord = x to z
            upgradeTriggerCount++
        },
        onEarnBonusXp = {
            localXpBonus += 50
        }
    )
}

// ============================================================================
// 3. NEVER-BLANK 3D PERSPECTIVE/ISOMETRIC CANVAS ENGINE + PARTICLE EMITTERS
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Garden3DGameContent(
    modifier: Modifier = Modifier,
    gridSize: Int,
    plantedTiles: Map<Pair<Int, Int>, FarmTile3D>,
    availableXp: Int,
    currentHour: Int,
    weatherMode: WeatherControlMode,
    activeWeather: SanctuaryWeather,
    onWeatherModeChange: (WeatherControlMode) -> Unit,
    unifiedHabits: List<HabitDisplayInfo>,
    selectedEmptyCoord: Pair<Int, Int>?,
    selectedOccupiedCoord: Pair<Int, Int>?,
    movingTreeSourceCoord: Pair<Int, Int>?,
    lastUpgradedCoord: Pair<Int, Int>?,
    upgradeTriggerCount: Int,
    onGridCoordTapped: (Int, Int) -> Unit,
    onStartMoveTree: (Int, Int) -> Unit,
    onCancelMoveTree: () -> Unit,
    onDismissEmptySheet: () -> Unit,
    onDismissOccupiedFloatingUi: () -> Unit,
    onPlantHabit: (Int, Int, HabitDisplayInfo?, String?, String?) -> Unit,
    onCompleteHabitFromGarden: (Int, Int) -> Unit,
    onEarnBonusXp: () -> Unit
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    // 3D Orbit & Zoom Camera State:
    // - cameraYaw: horizontal orbit angle in radians around the 3D Sanctuary center
    // - cameraPitch: vertical elevation angle in radians
    // - cameraZoom: zoom multiplier (supports smooth pinch-to-zoom from 0.55x to 3.0x)
    // - cameraPan: 2D screen-space pan offset
    var cameraYaw by remember { mutableFloatStateOf(0.785f) } // 45 deg isometric-3D angle
    var cameraPitch by remember { mutableFloatStateOf(0.62f) } // ~35 deg elevation pitch
    var cameraZoom by remember { mutableFloatStateOf(1.05f) }
    var cameraPan by remember { mutableStateOf(Offset.Zero) }

    // Smooth scale-up animation for newly planted, watered, or relocated 3D trees
    val animatedPlantScale = remember { Animatable(1f) }

    // 3D Water Droplet Fountain Emitter when watering, planting, or relocating a habit tree
    val waterDroplets = remember { mutableStateListOf<WaterDroplet3D>() }
    val groundRipples = remember { mutableStateListOf<GroundRipple3D>() }

    LaunchedEffect(lastUpgradedCoord, upgradeTriggerCount) {
        if (lastUpgradedCoord != null && upgradeTriggerCount > 0) {
            val (gx, gz) = lastUpgradedCoord
            val halfOffset = (gridSize - 1) * 0.5f
            val wx = gx - halfOffset
            val wz = gz - halfOffset

            repeat(24) { i ->
                val angle = (i / 24f) * (2f * PI.toFloat()) + Random.nextFloat() * 0.3f
                val speed = 0.8f + Random.nextFloat() * 1.1f
                waterDroplets.add(
                    WaterDroplet3D(
                        worldX = wx,
                        worldY = 0.25f,
                        worldZ = wz,
                        vx = cos(angle) * speed,
                        vy = 2.6f + Random.nextFloat() * 1.4f,
                        vz = sin(angle) * speed,
                        life = 1f,
                        color = if (i % 3 == 0) Color(0xFF80DEEA) else Color(0xFF00E5FF)
                    )
                )
            }
            groundRipples.add(
                GroundRipple3D(
                    worldX = wx,
                    worldZ = wz,
                    progress = 0f,
                    maxRadius = 0.85f,
                    color = Color(0xFF4DD0E1)
                )
            )

            animatedPlantScale.snapTo(0.35f)
            animatedPlantScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing)
            )
        }
    }

    // Dynamic Weather Particle Pool (Rain streaks, Sunbeam pollen motes, or Snowflakes)
    val weatherParticles = remember { mutableStateListOf<WeatherParticle>() }

    LaunchedEffect(activeWeather) {
        weatherParticles.clear()
        val count = when (activeWeather) {
            SanctuaryWeather.RAIN -> 95
            SanctuaryWeather.SNOWFALL -> 80
            SanctuaryWeather.SUNBEAMS -> 55
        }
        repeat(count) {
            weatherParticles.add(createInitialWeatherParticle(activeWeather, randomY = true))
        }
    }

    // Continuous 60 FPS Animation & Particle Physics Loop
    var animTimeSec by remember { mutableFloatStateOf(0f) }
    var lastFrameMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(activeWeather, gridSize) {
        while (true) {
            withFrameMillis { frameMs ->
                val dt = if (lastFrameMs == 0L) 0.016f else ((frameMs - lastFrameMs) / 1000f).coerceIn(0.001f, 0.05f)
                lastFrameMs = frameMs
                animTimeSec += dt

                // 1. Update Weather Particles
                for (i in weatherParticles.indices) {
                    val p = weatherParticles[i]
                    when (activeWeather) {
                        SanctuaryWeather.RAIN -> {
                            p.x += p.vx * dt * p.zDepth
                            p.y += p.vy * dt * p.zDepth
                            if (p.y > 1.05f || p.x < -0.1f || p.x > 1.1f) {
                                if (groundRipples.size < 18 && Random.nextFloat() < 0.22f) {
                                    groundRipples.add(
                                        GroundRipple3D(
                                            worldX = (Random.nextFloat() - 0.5f) * (gridSize - 0.5f),
                                            worldZ = (Random.nextFloat() - 0.5f) * (gridSize - 0.5f),
                                            progress = 0f,
                                            maxRadius = 0.38f,
                                            color = Color(0xFF81D4FA)
                                        )
                                    )
                                }
                                weatherParticles[i] = createInitialWeatherParticle(activeWeather, randomY = false)
                            }
                        }

                        SanctuaryWeather.SNOWFALL -> {
                            val sway = sin(animTimeSec * 1.8f + p.phase) * 0.035f
                            p.x += (p.vx + sway) * dt * p.zDepth
                            p.y += p.vy * dt * p.zDepth
                            if (p.y > 1.05f || p.x < -0.1f || p.x > 1.1f) {
                                weatherParticles[i] = createInitialWeatherParticle(activeWeather, randomY = false)
                            }
                        }

                        SanctuaryWeather.SUNBEAMS -> {
                            val floatX = cos(animTimeSec * 1.2f + p.phase) * 0.02f
                            val floatY = sin(animTimeSec * 1.5f + p.phase) * 0.015f
                            p.x += (p.vx + floatX) * dt
                            p.y += (p.vy + floatY) * dt
                            if (p.y < -0.05f || p.y > 1.05f || p.x < -0.05f || p.x > 1.05f) {
                                weatherParticles[i] = createInitialWeatherParticle(activeWeather, randomY = true)
                            }
                        }
                    }
                }

                // 2. Update 3D Ground Ripples
                val rippleIter = groundRipples.listIterator()
                while (rippleIter.hasNext()) {
                    val r = rippleIter.next()
                    r.progress += dt * 1.45f
                    if (r.progress >= 1f) {
                        rippleIter.remove()
                    }
                }

                // 3. Update 3D Water Fountain Droplets
                val dropIter = waterDroplets.listIterator()
                while (dropIter.hasNext()) {
                    val d = dropIter.next()
                    d.worldX += d.vx * dt
                    d.worldY += d.vy * dt
                    d.worldZ += d.vz * dt
                    d.vy -= 5.8f * dt
                    d.life -= dt * 1.35f
                    if (d.life <= 0f || d.worldY < 0f) {
                        dropIter.remove()
                    }
                }
            }
        }
    }

    // When a plant is tapped (and we're not in Move Mode), we isolate that single plant's 3D view on screen!
    val occupiedTile = selectedOccupiedCoord?.let { plantedTiles[it] }
    val isSinglePlantFocusMode = occupiedTile != null && movingTreeSourceCoord == null
    val activeHighlightCoord = movingTreeSourceCoord ?: selectedOccupiedCoord ?: selectedEmptyCoord
    val isMoveModeActive = movingTreeSourceCoord != null

    // Keep latest references for our unified multi-touch zoom + orbit + tap gesture handler
    val currentGridTapCallback by rememberUpdatedState(onGridCoordTapped)
    val currentPlantedTiles by rememberUpdatedState(plantedTiles.toMap())
    val currentIsSinglePlantFocus by rememberUpdatedState(isSinglePlantFocusMode)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .onSizeChanged { viewportSize = it }
            .testTag("garden_3d_game_screen")
    ) {
        // ====================================================================
        // HARDWARE-ACCELERATED 3D SANCTUARY & WEATHER PARTICLE CANVAS
        // - In Full Garden Mode: renders the 8x8 3D grid; pinch to zoom, drag to orbit, tap to select/move
        // - In Single-Plant Focus Mode (when a plant is tapped): renders ONLY that selected plant's
        //   3D model large and centered in the upper screen above the popup card!
        // ====================================================================
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("garden_3d_scene")
                .pointerInput(gridSize) {
                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        val downPos = firstDown.position
                        var totalMoveDist = 0f
                        var wasMultiTouch = false

                        do {
                            val event = awaitPointerEvent()
                            val activeChanges = event.changes.filter { it.pressed }
                            if (activeChanges.size >= 2) {
                                wasMultiTouch = true
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (zoomChange != 1f && zoomChange > 0f) {
                                    cameraZoom = (cameraZoom * zoomChange).coerceIn(0.55f, 3.0f)
                                }
                                if (!currentIsSinglePlantFocus) {
                                    val maxPan = 360f * cameraZoom
                                    cameraPan = Offset(
                                        x = (cameraPan.x + panChange.x).coerceIn(-maxPan, maxPan),
                                        y = (cameraPan.y + panChange.y).coerceIn(-maxPan, maxPan)
                                    )
                                }
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            } else if (activeChanges.size == 1 && !wasMultiTouch) {
                                val panChange = event.calculatePan()
                                val stepDist = hypot(panChange.x, panChange.y)
                                totalMoveDist += stepDist
                                if (totalMoveDist > 10f) {
                                    cameraYaw += panChange.x * 0.0068f
                                    cameraPitch = (cameraPitch + panChange.y * 0.0048f).coerceIn(0.35f, 1.18f)
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // If finger lifted without dragging or pinching in Full Garden Mode, hit-test the 3D grid!
                        if (!wasMultiTouch && totalMoveDist <= 14f && !currentIsSinglePlantFocus) {
                            val hitCoord = hitTest3DSanctuary(
                                tapOffset = downPos,
                                canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                                gridSize = gridSize,
                                plantedTiles = currentPlantedTiles,
                                cameraYaw = cameraYaw,
                                cameraPitch = cameraPitch,
                                cameraZoom = cameraZoom,
                                cameraPan = cameraPan
                            )
                            if (hitCoord != null) {
                                currentGridTapCallback(hitCoord.first, hitCoord.second)
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val halfOffset = (gridSize - 1) * 0.5f

            // 1. Atmospheric Sky & Sanctuary Glow Backdrop (#121212 dark core with weather tint)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        activeWeather.ambientTint,
                        Color(0xFF121212),
                        Color(0xFF0D110E)
                    )
                )
            )

            // 2. If activeWeather == SUNBEAMS, render background volumetric golden light shafts
            if (activeWeather == SanctuaryWeather.SUNBEAMS) {
                drawVolumetricSunbeams(
                    canvasWidth = w,
                    canvasHeight = h,
                    animTimeSec = animTimeSec
                )
            }

            if (isSinglePlantFocusMode && occupiedTile != null) {
                // ============================================================
                // FOCUSED SINGLE-PLANT 3D VIEW (ONLY THE SELECTED PLANT!)
                // ============================================================
                val focusCenterScreen = Offset(
                    x = w * 0.5f,
                    y = h * 0.36f
                )
                val focusTileScale = min(w, h) * 0.30f * cameraZoom.coerceIn(0.75f, 1.65f)

                // Bioluminescent spotlight aura behind the focused 3D plant
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            occupiedTile.state.primaryColor.copy(alpha = 0.28f),
                            activeWeather.badgeColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = focusCenterScreen,
                        radius = min(w, h) * 0.52f
                    ),
                    center = focusCenterScreen,
                    radius = min(w, h) * 0.52f
                )

                // Single-plot 3D island pedestal & ground block at origin (0f, 0f)
                draw3DIslandPedestal(
                    halfGrid = 0.65f,
                    centerScreen = focusCenterScreen,
                    baseTileScale = focusTileScale,
                    cameraYaw = cameraYaw,
                    cameraPitch = cameraPitch,
                    activeWeather = activeWeather
                )

                draw3DGroundTileBlock(
                    wx = 0f,
                    wz = 0f,
                    isEven = true,
                    isSelected = true,
                    isMoveDestinationCandidate = false,
                    isPlanted = true,
                    activeWeather = activeWeather,
                    centerScreen = focusCenterScreen,
                    baseTileScale = focusTileScale,
                    cameraYaw = cameraYaw,
                    cameraPitch = cameraPitch
                )

                // Glowing selection ring around the focused plant's 3D plot
                val pulse = 0.88f + 0.12f * sin(animTimeSec * 4.5f)
                drawOval(
                    color = Color(0xFF00E676).copy(alpha = 0.85f),
                    topLeft = Offset(
                        focusCenterScreen.x - focusTileScale * 0.44f * pulse,
                        focusCenterScreen.y - focusTileScale * 0.25f * pulse
                    ),
                    size = Size(focusTileScale * 0.88f * pulse, focusTileScale * 0.50f * pulse),
                    style = Stroke(width = 3.dp.toPx())
                )

                val isAnimated = lastUpgradedCoord?.first == occupiedTile.x && lastUpgradedCoord.second == occupiedTile.z
                val scaleFactor = if (isAnimated) animatedPlantScale.value else 1f
                val swayOffset = sin(animTimeSec * 2.2f) * 0.045f

                // Render ONLY this selected plant's 3D model
                draw3DPlantHierarchy(
                    wx = 0f,
                    wz = 0f,
                    tile = occupiedTile,
                    scaleFactor = scaleFactor,
                    swayOffset = swayOffset,
                    animTimeSec = animTimeSec,
                    activeWeather = activeWeather,
                    centerScreen = focusCenterScreen,
                    baseTileScale = focusTileScale,
                    cameraYaw = cameraYaw,
                    cameraPitch = cameraPitch
                )

                // Render 3D Water Fountain Droplets relative to the focused plant when watered
                val originWx = occupiedTile.x - halfOffset
                val originWz = occupiedTile.z - halfOffset
                for (drop in waterDroplets) {
                    val pScreen = project3DToScreen(
                        wx = drop.worldX - originWx,
                        wy = drop.worldY,
                        wz = drop.worldZ - originWz,
                        centerScreen = focusCenterScreen,
                        scale = focusTileScale,
                        yaw = cameraYaw,
                        pitch = cameraPitch
                    )
                    drawCircle(
                        color = drop.color.copy(alpha = drop.life.coerceIn(0f, 1f)),
                        radius = (6.5f * drop.life).coerceAtLeast(2.5f),
                        center = pScreen
                    )
                }
            } else {
                // ============================================================
                // FULL 3D GARDEN GRID VIEW (DEFAULT & MOVE DESTINATION PICKER)
                // ============================================================
                val baseTileScale = min(w, h) / (gridSize * 1.55f) * cameraZoom
                val centerScreen = Offset(
                    x = w * 0.5f + cameraPan.x,
                    y = h * 0.52f + cameraPan.y
                )

                // Bioluminescent radial sanctuary aura under the floating 3D island
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeWeather.badgeColor.copy(alpha = 0.16f),
                            Color(0xFF00E676).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = centerScreen,
                        radius = min(w, h) * 0.65f * cameraZoom
                    ),
                    center = centerScreen,
                    radius = min(w, h) * 0.65f * cameraZoom
                )

                // 3. Draw 3D Floating Sanctuary Bedrock Pedestal
                val halfGrid = gridSize * 0.5f
                draw3DIslandPedestal(
                    halfGrid = halfGrid,
                    centerScreen = centerScreen,
                    baseTileScale = baseTileScale,
                    cameraYaw = cameraYaw,
                    cameraPitch = cameraPitch,
                    activeWeather = activeWeather
                )

                // 4. Depth-sort all (x, z) 3D tiles from back-to-front (Painter's Algorithm)
                val sortedCoords = ArrayList<Pair<Int, Int>>(gridSize * gridSize)
                for (gx in 0 until gridSize) {
                    for (gz in 0 until gridSize) {
                        sortedCoords.add(gx to gz)
                    }
                }
                sortedCoords.sortBy { (gx, gz) ->
                    val wx = gx - halfOffset
                    val wz = gz - halfOffset
                    computeDepthKey(wx, 0f, wz, cameraYaw, cameraPitch)
                }

                // 5. Render each 3D Extruded Ground Block + 3D Plant Hierarchy
                for ((gx, gz) in sortedCoords) {
                    val wx = gx - halfOffset
                    val wz = gz - halfOffset
                    val isSelected = activeHighlightCoord?.first == gx && activeHighlightCoord.second == gz
                    val isMoveSource = movingTreeSourceCoord?.first == gx && movingTreeSourceCoord.second == gz
                    val plantedTile = plantedTiles[gx to gz]
                    val isEven = (gx + gz) % 2 == 0

                    draw3DGroundTileBlock(
                        wx = wx,
                        wz = wz,
                        isEven = isEven,
                        isSelected = isSelected,
                        isMoveDestinationCandidate = isMoveModeActive && !isMoveSource,
                        isPlanted = plantedTile != null,
                        activeWeather = activeWeather,
                        centerScreen = centerScreen,
                        baseTileScale = baseTileScale,
                        cameraYaw = cameraYaw,
                        cameraPitch = cameraPitch
                    )

                    // Draw selection or move beacon ring on selected tile
                    if (isSelected) {
                        val topCenter = project3DToScreen(
                            wx = wx,
                            wy = 0.06f,
                            wz = wz,
                            centerScreen = centerScreen,
                            scale = baseTileScale,
                            yaw = cameraYaw,
                            pitch = cameraPitch
                        )
                        val pulse = 0.85f + 0.15f * sin(animTimeSec * 5f)
                        val ringColor = if (isMoveSource) Color(0xFFFFD54F) else Color(0xFF00E676)
                        drawOval(
                            color = ringColor.copy(alpha = 0.90f),
                            topLeft = Offset(
                                topCenter.x - baseTileScale * 0.42f * pulse,
                                topCenter.y - baseTileScale * 0.24f * pulse
                            ),
                            size = Size(baseTileScale * 0.84f * pulse, baseTileScale * 0.48f * pulse),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Render 3D Plant Node if occupied
                    if (plantedTile != null) {
                        val isAnimated = lastUpgradedCoord?.first == gx && lastUpgradedCoord.second == gz
                        val scaleFactor = if (isAnimated) animatedPlantScale.value else 1f
                        val swayOffset = sin(animTimeSec * 2.2f + gx * 0.7f + gz * 0.9f) * 0.045f

                        draw3DPlantHierarchy(
                            wx = wx,
                            wz = wz,
                            tile = plantedTile,
                            scaleFactor = scaleFactor,
                            swayOffset = swayOffset,
                            animTimeSec = animTimeSec,
                            activeWeather = activeWeather,
                            centerScreen = centerScreen,
                            baseTileScale = baseTileScale,
                            cameraYaw = cameraYaw,
                            cameraPitch = cameraPitch
                        )
                    }
                }

                // 6. Render 3D Expanding Ground Ripples (from Rain or Watering)
                for (ripple in groundRipples) {
                    val screenPos = project3DToScreen(
                        wx = ripple.worldX,
                        wy = 0.04f,
                        wz = ripple.worldZ,
                        centerScreen = centerScreen,
                        scale = baseTileScale,
                        yaw = cameraYaw,
                        pitch = cameraPitch
                    )
                    val rx = ripple.maxRadius * ripple.progress * baseTileScale
                    val ry = rx * sin(cameraPitch).coerceAtLeast(0.35f)
                    val alpha = (1f - ripple.progress).coerceIn(0f, 1f)
                    drawOval(
                        color = ripple.color.copy(alpha = alpha * 0.75f),
                        topLeft = Offset(screenPos.x - rx, screenPos.y - ry),
                        size = Size(rx * 2f, ry * 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 7. Render 3D Water Fountain Droplets
                for (drop in waterDroplets) {
                    val pScreen = project3DToScreen(
                        wx = drop.worldX,
                        wy = drop.worldY,
                        wz = drop.worldZ,
                        centerScreen = centerScreen,
                        scale = baseTileScale,
                        yaw = cameraYaw,
                        pitch = cameraPitch
                    )
                    drawCircle(
                        color = drop.color.copy(alpha = drop.life.coerceIn(0f, 1f)),
                        radius = (4.5f * cameraZoom * drop.life).coerceAtLeast(2f),
                        center = pScreen
                    )
                }
            }

            // 8. Render Foreground Weather Particle Emitters (Rain streaks, Snowflakes, or Sun Motes)
            drawWeatherParticlesOverlay(
                activeWeather = activeWeather,
                particles = weatherParticles,
                canvasWidth = w,
                canvasHeight = h,
                animTimeSec = animTimeSec
            )
        }

        // ====================================================================
        // TOP 2D HUD HEADER:
        // - In Single-Plant Focus Mode: shows a clean "Back to Garden" bar so the 3D plant is unobstructed
        // - In Move Mode: shows ONLY a compact Move prompt banner so the entire garden grid is visible
        // - In Default Garden Mode: shows the HUD header + Weather emitter strip
        // ====================================================================
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSinglePlantFocusMode && occupiedTile != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1E1E).copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, occupiedTile.state.primaryColor.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onDismissOccupiedFloatingUi,
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("back_to_garden_3d_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Back to Garden",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Back to Garden",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "Drag to rotate 3D plant",
                            fontSize = 11.sp,
                            color = Color(0xFFBDBDBD)
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF00ACC1).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF00ACC1).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Available XP",
                                    tint = Color(0xFF4DD0E1),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$availableXp XP",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF80DEEA)
                                )
                            }
                        }
                    }
                }
            } else if (movingTreeSourceCoord != null) {
                // Compact Move Banner ONLY (popup is closed and top HUD is hidden so full garden is visible!)
                val movingTile = plantedTiles[movingTreeSourceCoord]
                if (movingTile != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2E2612).copy(alpha = 0.95f),
                        border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("move_tree_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenWith,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Select a plot for ${movingTile.habitEmoji} ${movingTile.habitTitle}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFF59D)
                                    )
                                    Text(
                                        text = "Tap any highlighted plot on the garden to move there",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE0E0E0)
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = onCancelMoveTree,
                                border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = Color(0xFFFFD54F))
                            }
                        }
                    }
                }
            } else {
                Garden3DHudHeader(
                    availableXp = availableXp,
                    plantedCount = plantedTiles.size,
                    totalTiles = gridSize * gridSize,
                    onRotateCamera90 = {
                        cameraYaw += (PI / 4.0).toFloat()
                    },
                    onResetOrbitCamera = {
                        cameraYaw = 0.785f
                        cameraPitch = 0.62f
                        cameraZoom = 1.05f
                        cameraPan = Offset.Zero
                    },
                    onEarnBonusXp = onEarnBonusXp
                )

                WeatherControlStrip(
                    weatherMode = weatherMode,
                    activeWeather = activeWeather,
                    currentHour = currentHour,
                    onWeatherModeChange = onWeatherModeChange
                )
            }
        }

        // ====================================================================
        // COMPACT ZOOM CONTROLS (+ / -) ON RIGHT EDGE TO COMPLEMENT PINCH ZOOM
        // ====================================================================
        if (!isSinglePlantFocusMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E1E1E).copy(alpha = 0.88f),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = { cameraZoom = (cameraZoom * 1.22f).coerceIn(0.55f, 3.0f) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("zoom_in_3d_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${(cameraZoom * 100).roundToInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF81C784)
                    )
                    IconButton(
                        onClick = { cameraZoom = (cameraZoom / 1.22f).coerceIn(0.55f, 3.0f) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("zoom_out_3d_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ====================================================================
        // FLOATING HABIT INSPECTOR POPUP (SHOWN ONLY IN SINGLE-PLANT FOCUS MODE;
        // CLOSES IMMEDIATELY WHEN USER SELECTS "MOVE" SO GARDEN PLOTS ARE VISIBLE)
        // ====================================================================
        AnimatedVisibility(
            visible = isSinglePlantFocusMode,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            if (occupiedTile != null) {
                FloatingHabitPlantInspectorCard(
                    tile = occupiedTile,
                    isMovingThisTree = false,
                    onToggleMoveTree = {
                        // Clicking Move closes this popup and switches back to the full garden grid
                        onStartMoveTree(occupiedTile.x, occupiedTile.z)
                    },
                    onCompleteHabitToday = { onCompleteHabitFromGarden(occupiedTile.x, occupiedTile.z) },
                    onDismiss = onDismissOccupiedFloatingUi
                )
            }
        }

        // Bottom 3D Growth Stage Legend Pill (shown only when popup is not open)
        if (!isSinglePlantFocusMode) {
            Garden3DStageLegend(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }

    // ====================================================================
    // MODAL BOTTOM SHEET TO PLANT AN EXISTING OR NEW HABIT ON EMPTY 3D TILE
    // ====================================================================
    if (selectedEmptyCoord != null) {
        val (selX, selZ) = selectedEmptyCoord
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onDismissEmptySheet,
            sheetState = sheetState,
            containerColor = Color(0xFF1E1E1E),
            contentColor = Color.White
        ) {
            PlantHabit3DSheetContent(
                x = selX,
                z = selZ,
                unifiedHabits = unifiedHabits,
                onConfirmPlant = { chosenHabit, customTitle, customEmoji ->
                    onPlantHabit(selX, selZ, chosenHabit, customTitle, customEmoji)
                },
                onCancel = onDismissEmptySheet
            )
        }
    }
}

// ============================================================================
// 4. 3D PROJECTION, SHADING, PLANT PRIMITIVES & WEATHER PARTICLE RENDERERS
// ============================================================================

private fun project3DToScreen(
    wx: Float,
    wy: Float,
    wz: Float,
    centerScreen: Offset,
    scale: Float,
    yaw: Float,
    pitch: Float
): Offset {
    val cosY = cos(yaw)
    val sinY = sin(yaw)
    val rx = wx * cosY - wz * sinY
    val rz = wx * sinY + wz * cosY

    val sinP = sin(pitch).coerceIn(0.32f, 0.92f)
    val cosP = cos(pitch).coerceIn(0.35f, 0.95f)

    val perspective = 1f + (rz * 0.028f).coerceIn(-0.18f, 0.22f)
    val sx = centerScreen.x + rx * scale * perspective
    val sy = centerScreen.y + (rz * sinP - wy * cosP) * scale * perspective
    return Offset(sx, sy)
}

private fun computeDepthKey(
    wx: Float,
    wy: Float,
    wz: Float,
    yaw: Float,
    pitch: Float
): Float {
    val sinY = sin(yaw)
    val cosY = cos(yaw)
    val rz = wx * sinY + wz * cosY
    return rz * cos(pitch) + wy * sin(pitch)
}

private fun DrawScope.draw3DIslandPedestal(
    halfGrid: Float,
    centerScreen: Offset,
    baseTileScale: Float,
    cameraYaw: Float,
    cameraPitch: Float,
    activeWeather: SanctuaryWeather
) {
    val pad = halfGrid + 0.35f
    val topY = -0.18f
    val botY = -0.78f

    val topCorners = listOf(
        project3DToScreen(-pad, topY, -pad, centerScreen, baseTileScale, cameraYaw, cameraPitch),
        project3DToScreen(pad, topY, -pad, centerScreen, baseTileScale, cameraYaw, cameraPitch),
        project3DToScreen(pad, topY, pad, centerScreen, baseTileScale, cameraYaw, cameraPitch),
        project3DToScreen(-pad, topY, pad, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    )
    val botCorners = listOf(
        project3DToScreen(-pad * 0.86f, botY, -pad * 0.86f, centerScreen, baseTileScale, cameraYaw, cameraPitch),
        project3DToScreen(pad * 0.86f, botY, -pad * 0.86f, centerScreen, baseTileScale, cameraYaw, cameraPitch),
        project3DToScreen(pad * 0.86f, botY, pad * 0.86f, centerScreen, baseTileScale, cameraYaw, cameraPitch),
        project3DToScreen(-pad * 0.86f, botY, pad * 0.86f, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    )

    for (i in 0 until 4) {
        val next = (i + 1) % 4
        val facePath = Path().apply {
            moveTo(topCorners[i].x, topCorners[i].y)
            lineTo(topCorners[next].x, topCorners[next].y)
            lineTo(botCorners[next].x, botCorners[next].y)
            lineTo(botCorners[i].x, botCorners[i].y)
            close()
        }
        val shade = if (i % 2 == 0) Color(0xFF141C16) else Color(0xFF0E1410)
        drawPath(facePath, color = shade)
    }

    val rimPath = Path().apply {
        moveTo(topCorners[0].x, topCorners[0].y)
        for (i in 1..3) lineTo(topCorners[i].x, topCorners[i].y)
        close()
    }
    drawPath(rimPath, color = Color(0xFF161D17))
    drawPath(
        path = rimPath,
        color = activeWeather.badgeColor.copy(alpha = 0.35f),
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * Renders an individual 3D extruded ground tile block at (wx, wz).
 */
private fun DrawScope.draw3DGroundTileBlock(
    wx: Float,
    wz: Float,
    isEven: Boolean,
    isSelected: Boolean,
    isMoveDestinationCandidate: Boolean,
    isPlanted: Boolean,
    activeWeather: SanctuaryWeather,
    centerScreen: Offset,
    baseTileScale: Float,
    cameraYaw: Float,
    cameraPitch: Float
) {
    val half = 0.44f
    val topY = 0.0f
    val botY = -0.18f

    val t0 = project3DToScreen(wx - half, topY, wz - half, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    val t1 = project3DToScreen(wx + half, topY, wz - half, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    val t2 = project3DToScreen(wx + half, topY, wz + half, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    val t3 = project3DToScreen(wx - half, topY, wz + half, centerScreen, baseTileScale, cameraYaw, cameraPitch)

    val b0 = project3DToScreen(wx - half, botY, wz - half, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    val b1 = project3DToScreen(wx + half, botY, wz - half, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    val b2 = project3DToScreen(wx + half, botY, wz + half, centerScreen, baseTileScale, cameraYaw, cameraPitch)
    val b3 = project3DToScreen(wx - half, botY, wz + half, centerScreen, baseTileScale, cameraYaw, cameraPitch)

    val topPoints = listOf(t0, t1, t2, t3)
    val botPoints = listOf(b0, b1, b2, b3)

    val baseTopColor = when {
        isSelected -> Color(0xFF2E7D32)
        isMoveDestinationCandidate && !isPlanted -> Color(0xFF233226)
        isPlanted -> if (isEven) Color(0xFF243326) else Color(0xFF1F2D21)
        isEven -> Color(0xFF1D241E)
        else -> Color(0xFF171D18)
    }

    val weatherAdjustedTop = when (activeWeather) {
        SanctuaryWeather.SNOWFALL -> lerpColor(baseTopColor, Color(0xFFCFD8DC), if (isPlanted) 0.14f else 0.22f)
        SanctuaryWeather.RAIN -> lerpColor(baseTopColor, Color(0xFF102830), 0.18f)
        SanctuaryWeather.SUNBEAMS -> lerpColor(baseTopColor, Color(0xFF334729), 0.15f)
    }

    for (edge in 0 until 4) {
        val next = (edge + 1) % 4
        val midTopY = (topPoints[edge].y + topPoints[next].y) * 0.5f
        val midBotY = (botPoints[edge].y + botPoints[next].y) * 0.5f
        if (midBotY > midTopY - 1f) {
            val sidePath = Path().apply {
                moveTo(topPoints[edge].x, topPoints[edge].y)
                lineTo(topPoints[next].x, topPoints[next].y)
                lineTo(botPoints[next].x, botPoints[next].y)
                lineTo(botPoints[edge].x, botPoints[edge].y)
                close()
            }
            val sideShade = if (edge % 2 == 0) {
                darkenColor(weatherAdjustedTop, 0.62f)
            } else {
                darkenColor(weatherAdjustedTop, 0.46f)
            }
            drawPath(sidePath, color = sideShade)
        }
    }

    val topPath = Path().apply {
        moveTo(t0.x, t0.y)
        lineTo(t1.x, t1.y)
        lineTo(t2.x, t2.y)
        lineTo(t3.x, t3.y)
        close()
    }
    drawPath(topPath, color = weatherAdjustedTop)

    val borderColor = when {
        isSelected -> Color(0xFF69F0AE)
        isMoveDestinationCandidate -> Color(0xFFFFD54F).copy(alpha = 0.55f)
        isPlanted -> Color(0xFF388E3C).copy(alpha = 0.65f)
        else -> Color(0xFF333333)
    }
    drawPath(
        path = topPath,
        color = borderColor,
        style = Stroke(width = if (isSelected) 2.2.dp.toPx() else 1.dp.toPx())
    )
}

/**
 * Renders the 3D Plant Node hierarchy (soil mound, 3D cylinder trunk, shaded 3D foliage spheres,
 * blossoms, and orbiting spirit orbs) for SPROUT, SAPLING, FLORA, and GROVE.
 */
private fun DrawScope.draw3DPlantHierarchy(
    wx: Float,
    wz: Float,
    tile: FarmTile3D,
    scaleFactor: Float,
    swayOffset: Float,
    animTimeSec: Float,
    activeWeather: SanctuaryWeather,
    centerScreen: Offset,
    baseTileScale: Float,
    cameraYaw: Float,
    cameraPitch: Float
) {
    val s = scaleFactor.coerceAtLeast(0.2f)
    val baseScreen = project3DToScreen(wx, 0.02f, wz, centerScreen, baseTileScale, cameraYaw, cameraPitch)

    // 1. Rich 3D Soil Mound at the base of the plant
    val moundRx = baseTileScale * 0.30f * s
    val moundRy = moundRx * sin(cameraPitch).coerceAtLeast(0.38f)
    drawOval(
        color = Color(0xFF3E2723),
        topLeft = Offset(baseScreen.x - moundRx, baseScreen.y - moundRy),
        size = Size(moundRx * 2f, moundRy * 2f)
    )
    drawOval(
        color = Color(0xFF4E342E),
        topLeft = Offset(baseScreen.x - moundRx * 0.8f, baseScreen.y - moundRy * 0.85f),
        size = Size(moundRx * 1.6f, moundRy * 1.5f)
    )

    // Subtle bioluminescent ground ring when habit is completed today
    if (tile.isCompletedToday) {
        drawOval(
            color = Color(0xFF00E676).copy(alpha = 0.55f),
            topLeft = Offset(baseScreen.x - moundRx * 1.18f, baseScreen.y - moundRy * 1.18f),
            size = Size(moundRx * 2.36f, moundRy * 2.36f),
            style = Stroke(width = 2.dp.toPx())
        )
    }

    when (tile.state) {
        PlantState.SPROUT -> {
            val stemTop = project3DToScreen(
                wx + swayOffset * 0.4f,
                0.26f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            drawLine(
                color = tile.state.trunkColor,
                start = baseScreen,
                end = stemTop,
                strokeWidth = (baseTileScale * 0.09f * s).coerceAtLeast(3f),
                cap = StrokeCap.Round
            )
            drawShaded3DSphere(
                center = stemTop,
                radius = baseTileScale * 0.20f * s,
                baseColor = tile.state.primaryColor,
                activeWeather = activeWeather
            )
            val budCenter = project3DToScreen(
                wx + 0.09f + swayOffset * 0.6f,
                0.42f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            drawShaded3DSphere(
                center = budCenter,
                radius = baseTileScale * 0.12f * s,
                baseColor = tile.state.secondaryColor,
                activeWeather = activeWeather
            )
        }

        PlantState.SAPLING -> {
            val trunkTop = project3DToScreen(
                wx + swayOffset * 0.6f,
                0.56f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            draw3DCylinderTrunk(
                bottom = baseScreen,
                top = trunkTop,
                radiusPx = baseTileScale * 0.075f * s,
                trunkColor = tile.state.trunkColor
            )
            val sideCanopy = project3DToScreen(
                wx + 0.16f + swayOffset * 0.8f,
                0.54f * s,
                wz + 0.08f,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            drawShaded3DSphere(
                center = sideCanopy,
                radius = baseTileScale * 0.19f * s,
                baseColor = tile.state.secondaryColor,
                activeWeather = activeWeather
            )
            val mainCanopy = project3DToScreen(
                wx + swayOffset,
                0.68f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            drawShaded3DSphere(
                center = mainCanopy,
                radius = baseTileScale * 0.27f * s,
                baseColor = tile.state.primaryColor,
                activeWeather = activeWeather
            )
        }

        PlantState.FLORA -> {
            val trunkTop = project3DToScreen(
                wx + swayOffset * 0.7f,
                0.74f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            draw3DCylinderTrunk(
                bottom = baseScreen,
                top = trunkTop,
                radiusPx = baseTileScale * 0.105f * s,
                trunkColor = tile.state.trunkColor
            )

            val crownCenter = project3DToScreen(
                wx + swayOffset,
                0.84f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            drawShaded3DSphere(
                center = crownCenter,
                radius = baseTileScale * 0.34f * s,
                baseColor = tile.state.primaryColor,
                activeWeather = activeWeather
            )

            val blossomOffsets = listOf(
                Triple(0f, 1.14f, 0f),
                Triple(0.24f, 0.90f, 0.16f),
                Triple(-0.24f, 0.90f, -0.16f),
                Triple(-0.16f, 0.86f, 0.24f)
            )
            blossomOffsets.forEach { (bx, by, bz) ->
                val bPos = project3DToScreen(
                    wx + bx * s + swayOffset * 1.1f,
                    by * s,
                    wz + bz * s,
                    centerScreen,
                    baseTileScale,
                    cameraYaw,
                    cameraPitch
                )
                drawShaded3DSphere(
                    center = bPos,
                    radius = baseTileScale * 0.095f * s,
                    baseColor = tile.state.secondaryColor,
                    activeWeather = activeWeather
                )
            }
        }

        PlantState.GROVE -> {
            val trunkTop = project3DToScreen(
                wx + swayOffset * 0.6f,
                0.95f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            draw3DCylinderTrunk(
                bottom = baseScreen,
                top = trunkTop,
                radiusPx = baseTileScale * 0.135f * s,
                trunkColor = tile.state.trunkColor
            )

            val lowerCanopy = project3DToScreen(
                wx + swayOffset * 0.9f,
                0.96f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            drawShaded3DSphere(
                center = lowerCanopy,
                radius = baseTileScale * 0.39f * s,
                baseColor = tile.state.primaryColor,
                activeWeather = activeWeather
            )

            val upperCanopy = project3DToScreen(
                wx + swayOffset * 1.25f,
                1.34f * s,
                wz,
                centerScreen,
                baseTileScale,
                cameraYaw,
                cameraPitch
            )
            drawShaded3DSphere(
                center = upperCanopy,
                radius = baseTileScale * 0.29f * s,
                baseColor = Color(0xFF2E7D32),
                activeWeather = activeWeather
            )

            for (orbIdx in 0..2) {
                val angle = animTimeSec * 1.8f + orbIdx * (2f * PI.toFloat() / 3f)
                val ox = cos(angle) * 0.38f * s
                val oz = sin(angle) * 0.38f * s
                val oy = (1.12f + 0.12f * sin(animTimeSec * 3f + orbIdx)) * s
                val orbScreen = project3DToScreen(
                    wx + ox,
                    oy,
                    wz + oz,
                    centerScreen,
                    baseTileScale,
                    cameraYaw,
                    cameraPitch
                )
                drawCircle(
                    color = tile.state.secondaryColor.copy(alpha = 0.32f),
                    radius = baseTileScale * 0.14f * s,
                    center = orbScreen
                )
                drawShaded3DSphere(
                    center = orbScreen,
                    radius = baseTileScale * 0.075f * s,
                    baseColor = tile.state.secondaryColor,
                    activeWeather = activeWeather
                )
            }
        }
    }
}

private fun DrawScope.drawShaded3DSphere(
    center: Offset,
    radius: Float,
    baseColor: Color,
    activeWeather: SanctuaryWeather
) {
    val r = radius.coerceAtLeast(2f)
    drawCircle(
        color = Color.Black.copy(alpha = 0.25f),
        radius = r * 0.96f,
        center = Offset(center.x + r * 0.08f, center.y + r * 0.14f)
    )

    val highlightTint = when (activeWeather) {
        SanctuaryWeather.SUNBEAMS -> Color(0xFFFFF59D)
        SanctuaryWeather.SNOWFALL -> Color(0xFFE1F5FE)
        SanctuaryWeather.RAIN -> Color(0xFFB2EBF2)
    }

    val highlightColor = lerpColor(baseColor, highlightTint, 0.48f)
    val shadowColor = darkenColor(baseColor, 0.52f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(highlightColor, baseColor, shadowColor),
            center = Offset(center.x - r * 0.32f, center.y - r * 0.32f),
            radius = r * 1.35f
        ),
        radius = r,
        center = center
    )

    if (activeWeather == SanctuaryWeather.SNOWFALL) {
        drawArc(
            color = Color.White.copy(alpha = 0.38f),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(center.x - r * 0.82f, center.y - r * 0.85f),
            size = Size(r * 1.64f, r * 1.2f),
            style = Stroke(width = (r * 0.18f).coerceAtLeast(1.5f), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.draw3DCylinderTrunk(
    bottom: Offset,
    top: Offset,
    radiusPx: Float,
    trunkColor: Color
) {
    val r = radiusPx.coerceAtLeast(2.5f)
    drawLine(
        color = darkenColor(trunkColor, 0.55f),
        start = Offset(bottom.x + r * 0.25f, bottom.y),
        end = Offset(top.x + r * 0.2f, top.y),
        strokeWidth = r * 2f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = trunkColor,
        start = Offset(bottom.x - r * 0.15f, bottom.y),
        end = Offset(top.x - r * 0.1f, top.y),
        strokeWidth = r * 1.35f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawVolumetricSunbeams(
    canvasWidth: Float,
    canvasHeight: Float,
    animTimeSec: Float
) {
    val rayCount = 5
    for (i in 0 until rayCount) {
        val shimmer = 0.5f + 0.5f * sin(animTimeSec * 0.9f + i * 1.3f)
        val topX = canvasWidth * (0.12f + i * 0.20f) + sin(animTimeSec * 0.4f + i) * 24f
        val botX = topX + canvasWidth * 0.28f
        val beamWidthTop = canvasWidth * 0.09f
        val beamWidthBot = canvasWidth * 0.22f

        val rayPath = Path().apply {
            moveTo(topX, 0f)
            lineTo(topX + beamWidthTop, 0f)
            lineTo(botX + beamWidthBot, canvasHeight)
            lineTo(botX, canvasHeight)
            close()
        }
        drawPath(
            path = rayPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFE082).copy(alpha = 0.13f * shimmer),
                    Color(0xFFA5D6A7).copy(alpha = 0.06f * shimmer),
                    Color.Transparent
                )
            )
        )
    }
}

private fun DrawScope.drawWeatherParticlesOverlay(
    activeWeather: SanctuaryWeather,
    particles: List<WeatherParticle>,
    canvasWidth: Float,
    canvasHeight: Float,
    animTimeSec: Float
) {
    for (p in particles) {
        val px = p.x * canvasWidth
        val py = p.y * canvasHeight
        when (activeWeather) {
            SanctuaryWeather.RAIN -> {
                val streakLen = p.size * p.zDepth
                val dx = p.vx * 0.045f * canvasWidth
                val dy = streakLen
                drawLine(
                    color = Color(0xFF81D4FA).copy(alpha = (p.alpha * p.zDepth).coerceIn(0.15f, 0.75f)),
                    start = Offset(px, py),
                    end = Offset(px + dx, py + dy),
                    strokeWidth = (1.8f * p.zDepth).coerceAtLeast(1f),
                    cap = StrokeCap.Round
                )
            }

            SanctuaryWeather.SNOWFALL -> {
                val r = (p.size * p.zDepth).coerceAtLeast(2f)
                val alpha = (p.alpha * p.zDepth).coerceIn(0.2f, 0.9f)
                drawCircle(
                    color = Color(0xFFE1F5FE).copy(alpha = alpha * 0.35f),
                    radius = r * 1.7f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = r,
                    center = Offset(px, py)
                )
            }

            SanctuaryWeather.SUNBEAMS -> {
                val pulse = 0.65f + 0.35f * sin(animTimeSec * 2.6f + p.phase)
                val r = (p.size * p.zDepth * pulse).coerceAtLeast(2f)
                val alpha = (p.alpha * pulse).coerceIn(0.15f, 0.85f)
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = alpha * 0.35f),
                    radius = r * 2.2f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color(0xFFFFF59D).copy(alpha = alpha),
                    radius = r,
                    center = Offset(px, py)
                )
            }
        }
    }
}

private fun createInitialWeatherParticle(
    weather: SanctuaryWeather,
    randomY: Boolean
): WeatherParticle {
    return when (weather) {
        SanctuaryWeather.RAIN -> WeatherParticle(
            x = Random.nextFloat() * 1.15f - 0.05f,
            y = if (randomY) Random.nextFloat() else -Random.nextFloat() * 0.15f,
            zDepth = 0.55f + Random.nextFloat() * 0.65f,
            vx = -0.14f - Random.nextFloat() * 0.06f,
            vy = 1.15f + Random.nextFloat() * 0.55f,
            size = 22f + Random.nextFloat() * 18f,
            alpha = 0.38f + Random.nextFloat() * 0.35f,
            phase = Random.nextFloat() * 6.28f
        )

        SanctuaryWeather.SNOWFALL -> WeatherParticle(
            x = Random.nextFloat(),
            y = if (randomY) Random.nextFloat() else -Random.nextFloat() * 0.12f,
            zDepth = 0.45f + Random.nextFloat() * 0.75f,
            vx = -0.02f + Random.nextFloat() * 0.04f,
            vy = 0.14f + Random.nextFloat() * 0.14f,
            size = 3.2f + Random.nextFloat() * 3.8f,
            alpha = 0.45f + Random.nextFloat() * 0.45f,
            phase = Random.nextFloat() * 6.28f
        )

        SanctuaryWeather.SUNBEAMS -> WeatherParticle(
            x = Random.nextFloat(),
            y = if (randomY) Random.nextFloat() else Random.nextFloat(),
            zDepth = 0.5f + Random.nextFloat() * 0.7f,
            vx = 0.015f + Random.nextFloat() * 0.025f,
            vy = -0.025f - Random.nextFloat() * 0.03f,
            size = 3.0f + Random.nextFloat() * 3.5f,
            alpha = 0.40f + Random.nextFloat() * 0.45f,
            phase = Random.nextFloat() * 6.28f
        )
    }
}

// ============================================================================
// 5. 3D HIT TESTING (PLANT BOUNDING SPHERES + GROUND PLANE UNPROJECTION)
// ============================================================================

private fun hitTest3DSanctuary(
    tapOffset: Offset,
    canvasSize: Size,
    gridSize: Int,
    plantedTiles: Map<Pair<Int, Int>, FarmTile3D>,
    cameraYaw: Float,
    cameraPitch: Float,
    cameraZoom: Float,
    cameraPan: Offset
): Pair<Int, Int>? {
    if (canvasSize.width <= 0f || canvasSize.height <= 0f) return null

    val baseTileScale = min(canvasSize.width, canvasSize.height) / (gridSize * 1.55f) * cameraZoom
    val centerScreen = Offset(
        x = canvasSize.width * 0.5f + cameraPan.x,
        y = canvasSize.height * 0.50f + cameraPan.y
    )
    val halfOffset = (gridSize - 1) * 0.5f

    // 1st priority: Check planted 3D trees from front to back
    val plantedSortedFrontFirst = plantedTiles.values.sortedByDescending { tile ->
        computeDepthKey(tile.x - halfOffset, 0.5f, tile.z - halfOffset, cameraYaw, cameraPitch)
    }

    for (tile in plantedSortedFrontFirst) {
        val wx = tile.x - halfOffset
        val wz = tile.z - halfOffset
        val canopyHeight = when (tile.state) {
            PlantState.SPROUT -> 0.28f
            PlantState.SAPLING -> 0.58f
            PlantState.FLORA -> 0.78f
            PlantState.GROVE -> 1.05f
        }
        val canopyScreen = project3DToScreen(
            wx = wx,
            wy = canopyHeight,
            wz = wz,
            centerScreen = centerScreen,
            scale = baseTileScale,
            yaw = cameraYaw,
            pitch = cameraPitch
        )
        val baseScreen = project3DToScreen(
            wx = wx,
            wy = 0f,
            wz = wz,
            centerScreen = centerScreen,
            scale = baseTileScale,
            yaw = cameraYaw,
            pitch = cameraPitch
        )

        val distToCanopy = hypot(tapOffset.x - canopyScreen.x, tapOffset.y - canopyScreen.y)
        val distToBase = hypot(tapOffset.x - baseScreen.x, tapOffset.y - baseScreen.y)
        if (distToCanopy <= baseTileScale * 0.52f || distToBase <= baseTileScale * 0.46f) {
            return tile.x to tile.z
        }
    }

    // 2nd priority: Find closest 3D ground tile top center
    var bestCoord: Pair<Int, Int>? = null
    var bestDist = Float.MAX_VALUE
    val maxAcceptRadius = baseTileScale * 0.58f

    for (gx in 0 until gridSize) {
        for (gz in 0 until gridSize) {
            val wx = gx - halfOffset
            val wz = gz - halfOffset
            val tileScreen = project3DToScreen(
                wx = wx,
                wy = 0f,
                wz = wz,
                centerScreen = centerScreen,
                scale = baseTileScale,
                yaw = cameraYaw,
                pitch = cameraPitch
            )
            val d = hypot(tapOffset.x - tileScreen.x, tapOffset.y - tileScreen.y)
            if (d < bestDist && d <= maxAcceptRadius) {
                bestDist = d
                bestCoord = gx to gz
            }
        }
    }

    return bestCoord
}

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val f = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * f,
        green = a.green + (b.green - a.green) * f,
        blue = a.blue + (b.blue - a.blue) * f,
        alpha = a.alpha + (b.alpha - a.alpha) * f
    )
}

private fun darkenColor(c: Color, factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    return Color(
        red = c.red * f,
        green = c.green * f,
        blue = c.blue * f,
        alpha = c.alpha
    )
}

// ============================================================================
// 6. 2D COMPOSE HUD, WEATHER EMITTER BAR, HABIT POPUP & PLANTING SHEET
// ============================================================================

@Composable
private fun Garden3DHudHeader(
    availableXp: Int,
    plantedCount: Int,
    totalTiles: Int,
    onRotateCamera90: () -> Unit,
    onResetOrbitCamera: () -> Unit,
    onEarnBonusXp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E).copy(alpha = 0.92f)
        ),
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2E7D32).copy(alpha = 0.25f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "3D Sanctuary Garden",
                            tint = Color(0xFF81C784)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Trellis 3D Sanctuary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$plantedCount/$totalTiles plots • Pinch to zoom, drag to orbit",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF00ACC1).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF00ACC1).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onEarnBonusXp() }
                        .testTag("xp_badge_3d_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Available XP",
                            tint = Color(0xFF4DD0E1),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$availableXp XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF80DEEA)
                        )
                    }
                }

                IconButton(
                    onClick = onRotateCamera90,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("rotate_orbit_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Rotate 3D Camera",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onResetOrbitCamera,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reset_orbit_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Reset 3D Orbit Camera",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherControlStrip(
    weatherMode: WeatherControlMode,
    activeWeather: SanctuaryWeather,
    currentHour: Int,
    onWeatherModeChange: (WeatherControlMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val timePeriodLabel = when (currentHour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..20 -> "Evening"
        else -> "Night"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A1F1C).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, activeWeather.badgeColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherControlMode.entries.forEach { mode ->
                val selected = weatherMode == mode
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) {
                        activeWeather.badgeColor.copy(alpha = 0.24f)
                    } else {
                        Color(0xFF252A27)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (selected) activeWeather.badgeColor else Color(0xFF3A3F3C)
                    ),
                    modifier = Modifier
                        .clickable { onWeatherModeChange(mode) }
                        .testTag("weather_mode_${mode.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                WeatherControlMode.AUTO -> Icons.Default.AutoAwesome
                                WeatherControlMode.SUNBEAMS -> Icons.Default.WbSunny
                                WeatherControlMode.RAIN -> Icons.Default.Thunderstorm
                                WeatherControlMode.SNOWFALL -> Icons.Default.AcUnit
                            },
                            contentDescription = null,
                            tint = if (selected) activeWeather.badgeColor else Color(0xFF9E9E9E),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (mode == WeatherControlMode.AUTO) "Auto ($timePeriodLabel)" else mode.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else Color(0xFFBDBDBD)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Popup Card displayed when a user taps any 3D Plant on the grid.
 * Includes full Habit details, "Move Tree" button to relocate to any other box,
 * and automatic one-time watering when the habit is marked completed for today.
 */
@Composable
private fun FloatingHabitPlantInspectorCard(
    tile: FarmTile3D,
    isMovingThisTree: Boolean,
    onToggleMoveTree: () -> Unit,
    onCompleteHabitToday: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stageProgress = (tile.state.ordinal + 1) / PlantState.entries.size.toFloat()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_water_plant_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B201D).copy(alpha = 0.97f)
        ),
        border = BorderStroke(
            1.5.dp,
            if (isMovingThisTree) Color(0xFFFFD54F) else tile.state.primaryColor.copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Habit Emoji + Habit Title + Move Button + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(tile.state.primaryColor.copy(alpha = 0.25f))
                            .border(1.dp, tile.state.primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tile.habitEmoji,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tile.state.primaryColor.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = tile.habitCategory.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA5D6A7),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Box (${tile.x}, ${tile.z})",
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tile.habitTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Move Tree Button right in the popup header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isMovingThisTree) Color(0xFFFFD54F) else Color(0xFF28302B),
                    border = BorderStroke(
                        1.dp,
                        if (isMovingThisTree) Color(0xFFFFF59D) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .clickable { onToggleMoveTree() }
                        .testTag("move_tree_3d_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Move Tree to another box",
                            tint = if (isMovingThisTree) Color.Black else Color(0xFFFFD54F),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMovingThisTree) "Tap Box…" else "Move",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMovingThisTree) Color.Black else Color(0xFFFFD54F)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close habit inspector",
                        tint = Color(0xFF9E9E9E)
                    )
                }
            }

            if (isMovingThisTree) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap any other box on the 3D grid to move this tree (or tap another tree to swap boxes).",
                    fontSize = 11.sp,
                    color = Color(0xFFFFE082)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Habit Telemetry Badges: Streak, Stage, Frequency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF262D28)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tile.streakCount}d Streak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFE0B2)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF262D28)
                ) {
                    Text(
                        text = "Stage: ${tile.state.label} (${tile.state.ordinal + 1}/4)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF81C784),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF262D28)
                ) {
                    Text(
                        text = tile.habitFrequency,
                        fontSize = 11.sp,
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { stageProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = tile.state.primaryColor,
                trackColor = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Single Action Button: Water is only used once when the habit is completed for today
            Button(
                onClick = onCompleteHabitToday,
                enabled = !tile.isCompletedToday,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complete_habit_3d_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1),
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF00E676).copy(alpha = 0.18f),
                    disabledContentColor = Color(0xFF00E676)
                )
            ) {
                Icon(
                    imageVector = if (tile.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (tile.isCompletedToday) {
                        "Habit Done • Watered Today ✓"
                    } else {
                        "Complete Habit & Water Plant (+30 XP)"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun Garden3DStageLegend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E1E1E).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantState.entries.forEach { state ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(state.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.label,
                        fontSize = 12.sp,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantHabit3DSheetContent(
    x: Int,
    z: Int,
    unifiedHabits: List<HabitDisplayInfo>,
    onConfirmPlant: (HabitDisplayInfo?, String?, String?) -> Unit,
    onCancel: () -> Unit
) {
    var selectedHabitIndex by remember { mutableIntStateOf(0) }
    var customHabitTitle by remember { mutableStateOf("") }
    var customHabitEmoji by remember { mutableStateOf("🌱") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("plant_habit_3d_sheet")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                tint = Color(0xFF81C784),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Plant a Habit Here",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Box Coordinate: (X: $x, Z: $z) • Links Tree to Your Habit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Choose an Existing Habit to Grow on This Box",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFBDBDBD)
        )

        Spacer(modifier = Modifier.height(8.dp))

        unifiedHabits.forEachIndexed { index, habit ->
            val isSelected = customHabitTitle.isBlank() && selectedHabitIndex == index
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        customHabitTitle = ""
                        selectedHabitIndex = index
                    },
                color = if (isSelected) {
                    Color(0xFF2E7D32).copy(alpha = 0.28f)
                } else {
                    Color(0xFF2A2A2A)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFF81C784) else Color(0xFF3A3A3A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.emoji,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White
                            )
                            Text(
                                text = "${habit.category} • ${habit.frequency} • 🔥 ${habit.streak}d streak",
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }

                    if (habit.completedToday) {
                        Text(
                            text = "Done ✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Or Plant a New Habit",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFBDBDBD)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customHabitEmoji,
                onValueChange = { if (it.length <= 4) customHabitEmoji = it },
                label = { Text("Icon") },
                singleLine = true,
                modifier = Modifier.width(78.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF81C784),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = customHabitTitle,
                onValueChange = { customHabitTitle = it },
                label = { Text("New habit name (optional)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF81C784),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cancel_plant_3d_button"),
                border = BorderStroke(1.dp, Color(0xFF424242))
            ) {
                Text(text = "Cancel", color = Color(0xFFBDBDBD))
            }

            Button(
                onClick = {
                    val chosen = unifiedHabits.getOrNull(selectedHabitIndex)
                    onConfirmPlant(chosen, customHabitTitle.trim().takeIf { it.isNotEmpty() }, customHabitEmoji)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("confirm_plant_3d_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Plant Sprout",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
