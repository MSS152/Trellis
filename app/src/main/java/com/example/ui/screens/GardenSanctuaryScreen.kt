package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GardenStateEntity
import com.example.ui.components.PlantSpecies
import com.example.ui.components.SanctuaryAnimatedPlant
import com.example.ui.components.SanctuaryGardenCard
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedFlameOrange
import com.example.ui.theme.GamifiedGold
import com.example.ui.viewmodel.HabitFlowViewModel

data class GardenPlotItem(
    val id: Int,
    val name: String,
    val emoji: String,
    val healthPercent: Int,
    val stage: String,
    val isLocked: Boolean = false,
    val color: Color
)

@Composable
fun GardenSanctuaryScreen(
    viewModel: HabitFlowViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()
    val gardenState by viewModel.gardenState.collectAsState()

    val totalLogsCount = logs.count { it.completed }
    val completedTasksCount = tasks.count { it.status.equals("Done", ignoreCase = true) }
    val totalXp = (totalLogsCount * 50) + (completedTasksCount * 25)
    val availableShields = habits.sumOf { it.streakShields }.coerceAtLeast(1)

    var activePlantSpecies by remember { mutableStateOf(PlantSpecies.FLOWER) }
    var selectedPlot by remember { mutableStateOf<GardenPlotItem?>(null) }
    var gardenPlots by remember {
        mutableStateOf(
            listOf(
                GardenPlotItem(1, "Emerald Sprout", "🌱", 100, "Sprout", false, GamifiedEmerald),
                GardenPlotItem(2, "Silver Birch", "🌿", 100, "Sapling", false, GamifiedCyan),
                GardenPlotItem(3, "Sakura Tree", "🌸", 75, "Thirsty 💧", false, Color(0xFFEC4899)),
                GardenPlotItem(4, "Sunshine Flora", "🌻", 100, "Blooming", false, GamifiedGold),
                GardenPlotItem(5, "Ancient Oak Grove", "🌳", 100, "Ancient", false, GamifiedElectricBlue),
                GardenPlotItem(6, "Sacred Lotus", "🌺", 60, "Needs Water 💧", false, GamifiedFlameOrange),
                GardenPlotItem(7, "Autumn Maple", "🍁", 0, "Unlock Lvl 5", true, Color(0xFF64748B)),
                GardenPlotItem(8, "Evergreen Pine", "🌲", 0, "Unlock Lvl 6", true, Color(0xFF64748B)),
                GardenPlotItem(9, "Crystal Shrub", "🔮", 0, "Unlock Lvl 7", true, Color(0xFF64748B))
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Trellis Sanctuary Garden",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Interactive 2D Canvas: Plant, arrange, and water your sanctuary flora",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        // Interactive Animated Plant Visualizer Component
        item {
            SanctuaryAnimatedPlant(
                xp = totalXp,
                onNurtureClick = {
                    viewModel.nurtureGarden()
                    gardenPlots = gardenPlots.map { p -> if (!p.isLocked) p.copy(healthPercent = 100, stage = "Thriving ✨") else p }
                },
                selectedSpecies = activePlantSpecies,
                onSpeciesChange = { activePlantSpecies = it }
            )
        }

        // Sanctuary Garden Card
        item {
            SanctuaryGardenCard(
                gardenState = gardenState ?: GardenStateEntity(id = 1, gardenLevel = 1, totalGardenXp = totalXp, sproutStage = "SPROUT", floraCount = 1, gardenHealthScore = 100),
                totalXp = totalXp,
                availableShields = availableShields,
                onNurtureClick = {
                    viewModel.nurtureGarden()
                    // Restore health of all plots
                    gardenPlots = gardenPlots.map { p -> if (!p.isLocked) p.copy(healthPercent = 100, stage = "Thriving ✨") else p }
                }
            )
        }

        // Interactive 2D Grid Sanctuary Canvas
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GamifiedEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("interactive_garden_canvas_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗺️ 2D Sanctuary Grid Plot Canvas",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(GamifiedEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("6 Planted", color = GamifiedEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Tap any plot to inspect, water, or nurture individual plants",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3x3 Isometric Plot Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        gardenPlots.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (item.isLocked) MaterialTheme.colorScheme.surfaceVariant
                                                else if (item.healthPercent < 80) GamifiedFlameOrange.copy(alpha = 0.15f)
                                                else item.color.copy(alpha = 0.15f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (item.isLocked) MaterialTheme.colorScheme.outline
                                                else if (item.healthPercent < 80) GamifiedFlameOrange
                                                else item.color,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedPlot = item }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (item.isLocked) "🔒" else item.emoji,
                                                fontSize = 26.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (item.isLocked) "Locked" else "${item.healthPercent}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isLocked) MaterialTheme.colorScheme.onSurfaceVariant
                                                else if (item.healthPercent < 80) GamifiedFlameOrange
                                                else GamifiedEmerald
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

        // Streak Shield Vault
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GamifiedElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("shield_vault_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛡️ Streak Shield Vault",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(GamifiedElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("$availableShields Active", color = GamifiedElectricBlue, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Streak Shields protect your habit momentum if you ever miss a day due to travel or rest.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Inspect & Water Plot Dialog
    selectedPlot?.let { plot ->
        AlertDialog(
            onDismissRequest = { selectedPlot = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plot.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(plot.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (plot.isLocked) {
                        Text("This plot slot is locked. Complete more daily habits to unlock new flora seeds!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    } else {
                        Text("Hydration Health: ${plot.healthPercent}%", fontWeight = FontWeight.Bold, color = if (plot.healthPercent < 80) GamifiedFlameOrange else GamifiedEmerald, fontSize = 13.sp)
                        Text("Growth Stage: ${plot.stage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("💧 Plants are automatically watered whenever you complete your daily habits.", fontSize = 11.sp, color = GamifiedEmerald)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPlot = null }) {
                    Text("Close", color = GamifiedEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
