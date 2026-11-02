package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GardenStateEntity
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedGold
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class PlantGrowthState {
    SPROUT, SAPLING, FLORA, GROVE
}

enum class PlantSpecies(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val description: String,
    val sproutEmoji: String,
    val saplingEmoji: String,
    val floraEmoji: String,
    val groveEmoji: String,
    val sproutLabel: String = "Sprout (0-24 XP)",
    val saplingLabel: String = "Sapling (25-49 XP)",
    val floraLabel: String = "Flora (50-99 XP)",
    val groveLabel: String = "Grove (100+ XP)"
) {
    FLOWER(
        id = "FLOWER",
        displayName = "Classic Flora",
        subtitle = "Lush Blossoming Garden",
        description = "Vibrant flowers & majestic trees that flourish with daily habit consistency.",
        sproutEmoji = "🌱",
        saplingEmoji = "🌿",
        floraEmoji = "🌸",
        groveEmoji = "🌳",
        sproutLabel = "Sprout (0-24 XP)",
        saplingLabel = "Sapling (25-49 XP)",
        floraLabel = "Sakura Bloom (50-99 XP)",
        groveLabel = "Ancient Grove (100+ XP)"
    ),
    CACTUS(
        id = "CACTUS",
        displayName = "Desert Succulent",
        subtitle = "Resilient Golden Cactus",
        description = "Resilient desert flora that blooms with vivid flowers under dedicated care.",
        sproutEmoji = "🌱",
        saplingEmoji = "🌵",
        floraEmoji = "🏜️",
        groveEmoji = "🌲",
        sproutLabel = "Succulent Sprout (0-24 XP)",
        saplingLabel = "Baby Cactus (25-49 XP)",
        floraLabel = "Flowering Saguaro (50-99 XP)",
        groveLabel = "Grand Oasis (100+ XP)"
    ),
    FERN(
        id = "FERN",
        displayName = "Lush Fern",
        subtitle = "Tropical Forest Canopy",
        description = "Deep emerald fronds and ancient tree ferns that thrive on daily nurture.",
        sproutEmoji = "🌱",
        saplingEmoji = "🌿",
        floraEmoji = "🪴",
        groveEmoji = "🌴",
        sproutLabel = "Fern Sprout (0-24 XP)",
        saplingLabel = "Emerald Frond (25-49 XP)",
        floraLabel = "Bushy Fern (50-99 XP)",
        groveLabel = "Ancient Fern Canopy (100+ XP)"
    ),
    BONSAI(
        id = "BONSAI",
        displayName = "Zen Bonsai",
        subtitle = "Mindful Sculpted Tree",
        description = "Hand-sculpted bonsai embodying patience, focus, and inner harmony.",
        sproutEmoji = "🌱",
        saplingEmoji = "🪴",
        floraEmoji = "🪵",
        groveEmoji = "⛩️",
        sproutLabel = "Bonsai Seedling (0-24 XP)",
        saplingLabel = "Miniature Bonsai (25-49 XP)",
        floraLabel = "Sculpted Bonsai (50-99 XP)",
        groveLabel = "Imperial Bonsai (100+ XP)"
    ),
    BAMBOO(
        id = "BAMBOO",
        displayName = "Lucky Bamboo",
        subtitle = "Serene Bamboo Grove",
        description = "Fast-growing stalks symbolizing endurance, prosperity, and rapid momentum.",
        sproutEmoji = "🌱",
        saplingEmoji = "🎍",
        floraEmoji = "🎋",
        groveEmoji = "🎋",
        sproutLabel = "Bamboo Shoot (0-24 XP)",
        saplingLabel = "Rising Stalk (25-49 XP)",
        floraLabel = "Lucky Bamboo (50-99 XP)",
        groveLabel = "Majestic Bamboo Forest (100+ XP)"
    );

    fun getEmoji(state: PlantGrowthState): String = when (state) {
        PlantGrowthState.SPROUT -> sproutEmoji
        PlantGrowthState.SAPLING -> saplingEmoji
        PlantGrowthState.FLORA -> floraEmoji
        PlantGrowthState.GROVE -> groveEmoji
    }

    fun getLabel(state: PlantGrowthState): String = when (state) {
        PlantGrowthState.SPROUT -> sproutLabel
        PlantGrowthState.SAPLING -> saplingLabel
        PlantGrowthState.FLORA -> floraLabel
        PlantGrowthState.GROVE -> groveLabel
    }
}

fun getGrowthStateForXp(xp: Int): PlantGrowthState = when {
    xp < 25 -> PlantGrowthState.SPROUT
    xp < 50 -> PlantGrowthState.SAPLING
    xp < 100 -> PlantGrowthState.FLORA
    else -> PlantGrowthState.GROVE
}

data class WaterBurstParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    var scale: Float = 1f,
    val radius: Float = 6f,
    val maxLife: Float = 1f,
    var currentLife: Float = 0f
)

@Composable
fun WaterParticleSystem(
    particles: List<WaterBurstParticle>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
    ) {
        particles.forEach { particle ->
            if (particle.alpha > 0f && particle.scale > 0f) {
                drawCircle(
                    color = GamifiedCyan.copy(alpha = particle.alpha.coerceIn(0f, 1f)),
                    radius = (particle.radius * particle.scale).coerceAtLeast(0.5f),
                    center = Offset(particle.x, particle.y)
                )
            }
        }
    }
}

// Interactive Animated Plant Visualizer with Withering Mechanics, Stage Downgrades, & Canvas Particle Water Emitter
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SanctuaryAnimatedPlant(
    xp: Int,
    onNurtureClick: () -> Unit,
    selectedSpecies: PlantSpecies = PlantSpecies.FLOWER,
    onSpeciesChange: (PlantSpecies) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var plantHealth by remember { mutableIntStateOf(100) }
    var currentXp by remember(xp) { mutableIntStateOf(xp) }
    var isShivering by remember { mutableStateOf(false) }
    var isReviving by remember { mutableStateOf(false) }
    var showSpeciesDialog by remember { mutableStateOf(false) }

    var cardWidthPx by remember { mutableStateOf(0f) }
    var cardHeightPx by remember { mutableStateOf(0f) }

    val buttonPressAnimatable = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    val plantState = remember(currentXp) { getGrowthStateForXp(currentXp) }
    val burstParticles = remember { mutableStateListOf<WaterBurstParticle>() }

    // Sound effect trigger on Plant Growth Tier Level-Up
    val previousPlantState = remember { mutableStateOf(plantState) }
    LaunchedEffect(plantState) {
        if (previousPlantState.value != plantState && plantState.ordinal > previousPlantState.value.ordinal) {
            com.example.util.GardenSoundEffects.playLevelUpChime()
        }
        previousPlantState.value = plantState
    }

    // Trigger Shiver reset after downgrade
    LaunchedEffect(isShivering) {
        if (isShivering) {
            delay(600)
            isShivering = false
        }
    }

    // Trigger Revival animation reset
    LaunchedEffect(isReviving) {
        if (isReviving) {
            delay(600)
            isReviving = false
        }
    }

    // Physics Loop for Water Droplets Emitter using awaitFrame()
    LaunchedEffect(burstParticles.size) {
        while (burstParticles.isNotEmpty()) {
            awaitFrame()
            val iterator = burstParticles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.currentLife += 0.025f
                val lifeProgress = (p.currentLife / p.maxLife).coerceIn(0f, 1f)
                p.x += p.vx
                p.y += p.vy
                p.vy += 0.38f // Gravity deceleration arc towards top of plant
                p.alpha = (1f - lifeProgress).coerceIn(0f, 1f)
                p.scale = (1f - lifeProgress).coerceIn(0f, 1f)
                if (lifeProgress >= 1f || p.alpha <= 0f) {
                    iterator.remove()
                }
            }
        }
    }

    // Animation values driven by plantHealth using animateFloatAsState
    val healthRatio = (plantHealth / 100f).coerceIn(0f, 1f)

    // Saturation target: 1.0f when healthy, down to 0.0f when health reaches 0
    val targetSaturation = healthRatio
    val animatedSaturation by animateFloatAsState(
        targetValue = targetSaturation,
        animationSpec = tween(durationMillis = 600),
        label = "plant_desaturation"
    )

    // Scale target: 1.0f when health >= 50, shrinking down to 0.80f when health is low
    val targetScale = if (plantHealth < 50) 0.80f + (healthRatio * 0.40f) else 1.0f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "plant_scale_wilt"
    )

    // Drooping rotation angle: 0deg when healthy, drooping down to -16deg when wilted
    val targetRotation = if (plantHealth < 50) -16f * (1f - (plantHealth / 50f)) else 0f
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 500),
        label = "plant_rotation_wilt"
    )

    // Bouncy Revival Scale pulse
    val animatedRevivalScale by animateFloatAsState(
        targetValue = if (isReviving) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "plant_revival_scale"
    )

    // Horizontal Shiver offset during stage downgrade
    val animatedShiverOffset by animateFloatAsState(
        targetValue = if (isShivering) 14f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "plant_shiver_offset"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    if (plantHealth < 50) listOf(Color(0xFF8B5A2B), Color(0xFF64748B), Color(0xFF334155))
                    else listOf(GamifiedEmerald, GamifiedCyan, GamifiedElectricBlue)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("animated_sanctuary_plant_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    cardWidthPx = size.width.toFloat()
                    cardHeightPx = size.height.toFloat()
                }
        ) {
            // Self-contained Canvas Water Particle Emitter Overlay
            WaterParticleSystem(
                particles = burstParticles,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${selectedSpecies.groveEmoji} Sanctuary Plant",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GamifiedCyan.copy(alpha = 0.2f))
                                .border(1.dp, GamifiedCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable { showSpeciesDialog = true }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("swap_species_button")
                        ) {
                            Text(
                                text = "🌱 Swap",
                                color = GamifiedCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = when {
                                    plantHealth >= 80 -> GamifiedEmerald.copy(alpha = 0.2f)
                                    plantHealth >= 40 -> GamifiedGold.copy(alpha = 0.2f)
                                    else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when {
                                plantHealth >= 80 -> "✨ Vibrant ($plantHealth%)"
                                plantHealth >= 40 -> "💧 Thirsty ($plantHealth%)"
                                else -> "🥀 Withered ($plantHealth%)"
                            },
                            color = when {
                                plantHealth >= 80 -> GamifiedEmerald
                                plantHealth >= 40 -> GamifiedGold
                                else -> Color(0xFFEF4444)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Species: ${selectedSpecies.displayName} • XP: $currentXp • Tier: ${plantState.name}",
                    color = if (plantHealth < 50) Color(0xFFD97706) else GamifiedCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Plant Vector Display Box with Desaturation, Scale, Drooping, Shiver & Revival Animations
                val haloColor = when {
                    plantHealth < 30 -> Color(0xFF5A4D41)
                    plantState == PlantGrowthState.SPROUT -> GamifiedEmerald
                    plantState == PlantGrowthState.SAPLING -> GamifiedCyan
                    plantState == PlantGrowthState.FLORA -> Color(0xFFEC4899)
                    else -> GamifiedElectricBlue
                }

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        haloColor.copy(alpha = (0.15f + 0.25f * animatedSaturation).coerceIn(0.1f, 0.4f)),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = if (plantHealth < 50) Color(0xFFB45309) else haloColor,
                                shape = CircleShape
                            )
                            .graphicsLayer {
                                val combinedScale = animatedScale * animatedRevivalScale
                                scaleX = combinedScale
                                scaleY = combinedScale
                                rotationZ = animatedRotation + (if (isShivering) (if (animatedShiverOffset > 5f) 8f else -8f) else 0f)
                                translationX = if (isShivering) (if (animatedShiverOffset > 5f) 10f else -10f) else 0f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = plantState,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(500)) + scaleIn(
                                    initialScale = 0.5f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )).togetherWith(
                                    fadeOut(animationSpec = tween(400)) + scaleOut(
                                        targetScale = 1.2f,
                                        animationSpec = tween(400)
                                    )
                                )
                            },
                            label = "plant_growth_transition"
                        ) { state ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.drawWithContent {
                                    val matrix = ColorMatrix().apply {
                                        setToSaturation(animatedSaturation)
                                    }
                                    val paint = Paint().apply {
                                        colorFilter = ColorFilter.colorMatrix(matrix)
                                    }
                                    drawIntoCanvas { canvas ->
                                        canvas.saveLayer(size.toRect(), paint)
                                        drawContent()
                                        canvas.restore()
                                    }
                                }
                            ) {
                                val baseColor = when (state) {
                                    PlantGrowthState.SPROUT -> GamifiedEmerald
                                    PlantGrowthState.SAPLING -> GamifiedCyan
                                    PlantGrowthState.FLORA -> Color(0xFFEC4899)
                                    PlantGrowthState.GROVE -> GamifiedElectricBlue
                                }
                                val emoji = selectedSpecies.getEmoji(state)
                                val label = selectedSpecies.getLabel(state)
                                val displayColor = if (plantHealth < 50) Color(0xFFA16207) else baseColor
                                val displayEmoji = if (plantHealth < 20) "🥀" else emoji

                                Text(displayEmoji, fontSize = 54.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (plantHealth < 50) "$label • Wilted" else label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = displayColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Automatic Habit-Driven Watering Status Pill (no manual water button needed)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GamifiedEmerald.copy(alpha = 0.14f))
                        .border(1.dp, GamifiedEmerald.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💧 Automatically watered each time you complete a habit",
                        color = GamifiedEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showSpeciesDialog) {
        PlantSpeciesSelectionDialog(
            currentSpecies = selectedSpecies,
            onSpeciesSelected = { newSpecies ->
                onSpeciesChange(newSpecies)
            },
            onDismissRequest = { showSpeciesDialog = false }
        )
    }
}

// Micro Garden Banner for Overview Tab (no manual +Water button; auto-watered when habits are done)
@Composable
fun MicroGardenBanner(
    totalXp: Int,
    onWaterClick: () -> Unit = {},
    onNavigateToGarden: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val level = (totalXp / 100) + 1
    val stageEmoji = when (level) {
        1 -> "🌱"
        2 -> "🌿"
        3 -> "🌸"
        else -> "🌳"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GamifiedEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onNavigateToGarden() }
            .testTag("micro_garden_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GamifiedEmerald.copy(alpha = 0.2f))
                        .border(1.dp, GamifiedEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stageEmoji, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sanctuary Garden Lvl $level",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "🌱 Auto-waters & grows as you complete habits",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GamifiedEmerald.copy(alpha = 0.16f))
                    .border(1.dp, GamifiedEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💧 Auto-Watered",
                    color = GamifiedEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Full Sanctuary Garden Header Card
@Composable
fun SanctuaryGardenCard(
    gardenState: GardenStateEntity,
    totalXp: Int,
    availableShields: Int,
    onNurtureClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val level = (totalXp / 100) + 1
    val progress = (totalXp % 100) / 100f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "garden_xp_progress"
    )

    val (stageName, stageEmoji, gardenDesc) = when (level) {
        1 -> Triple("Sprout Haven", "🌱", "Your seeds are taking root in the Trellis soil")
        2 -> Triple("Rising Sapling", "🌿", "Strong stems and vibrant leaves are flourishing")
        3 -> Triple("Blossoming Flora", "🌸", "Trees are blooming with radiant energy")
        else -> Triple("Ancient Sanctuary", "🌳", "A majestic, self-sustaining ecosystem grove")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(GamifiedEmerald, GamifiedCyan, GamifiedElectricBlue)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("sanctuary_garden_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(GamifiedEmerald.copy(alpha = 0.3f), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, GamifiedEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stageEmoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stageName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(GamifiedEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .border(1.dp, GamifiedEmerald, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lvl $level",
                                    color = GamifiedEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Text(
                            text = gardenDesc,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Growth Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ecosystem Growth to Lvl ${level + 1}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% (${totalXp % 100}/100 XP)",
                        fontSize = 10.sp,
                        color = GamifiedEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GamifiedEmerald,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

// Plant Species Selection Dialog
@Composable
fun PlantSpeciesSelectionDialog(
    currentSpecies: PlantSpecies,
    onSpeciesSelected: (PlantSpecies) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedTemp by remember { mutableStateOf(currentSpecies) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🌱 Select Active Species",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Swap your active sanctuary plant species. Growth tiers & XP thresholds (Sprout 0-24 XP, Sapling 25-49 XP, Flora 50-99 XP, Grove 100+ XP) are preserved.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                PlantSpecies.entries.forEach { species ->
                    val isSelected = species == selectedTemp
                    val cardBorderColor = if (isSelected) GamifiedEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    val cardBgColor = if (isSelected) GamifiedEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = if (isSelected) 2.dp else 1.dp, color = cardBorderColor, shape = RoundedCornerShape(14.dp))
                            .clickable { selectedTemp = species }
                            .testTag("species_option_${species.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Preview Grid
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) GamifiedEmerald.copy(alpha = 0.25f) else Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(species.groveEmoji, fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = species.displayName,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• Active",
                                            color = GamifiedEmerald,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Text(
                                    text = species.subtitle,
                                    color = GamifiedCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${species.sproutEmoji} → ${species.saplingEmoji} → ${species.floraEmoji} → ${species.groveEmoji}",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSpeciesSelected(selectedTemp)
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GamifiedEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_species_button")
            ) {
                Text("Confirm Selection", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        },
        modifier = Modifier.testTag("plant_species_dialog")
    )
}

