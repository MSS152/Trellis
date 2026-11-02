package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedFlameOrange
import com.example.ui.theme.GamifiedFlameRed
import com.example.ui.viewmodel.HabitFlowViewModel

// Hero Search scoring algorithm
fun calculateTaskMatchScore(task: TaskEntity, query: String): Double {
    if (query.isBlank()) return 1.0
    val q = query.trim().lowercase()
    val title = task.title.lowercase()
    val priority = task.priority.lowercase()
    val category = task.category.lowercase()
    val desc = task.description.lowercase()
    val assignee = task.assignedTo.lowercase()

    if (title == q || priority == q) return 1.0
    if (title.contains(q)) return 0.95
    if (priority.contains(q)) return 0.9
    if (category.contains(q)) return 0.85
    if (desc.contains(q) || assignee.contains(q)) return 0.8

    if ((q.startsWith("hi") || q == "h") && priority.startsWith("hi")) return 0.88
    if ((q.startsWith("med") || q == "m") && priority.startsWith("med")) return 0.88
    if ((q.startsWith("lo") || q == "l") && priority.startsWith("low")) return 0.88

    val queryTokens = q.split(" ", "-", "_").filter { it.isNotBlank() }
    val titleTokens = title.split(" ", "-", "_").filter { it.isNotBlank() }
    val matchedTokens = queryTokens.count { qt ->
        titleTokens.any { tt -> tt.startsWith(qt) || tt.contains(qt) } ||
        priority.contains(qt) || category.contains(qt)
    }
    if (matchedTokens > 0) {
        return 0.75 * (matchedTokens.toDouble() / queryTokens.size)
    }

    var qIdx = 0
    var matchedChars = 0
    for (ch in title) {
        if (qIdx < q.length && ch == q[qIdx]) {
            qIdx++
            matchedChars++
        }
    }
    if (qIdx == q.length && q.length >= 2) {
        return 0.6
    }

    return 0.0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksWorkspaceScreen(
    viewModel: HabitFlowViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedPriority by remember { mutableStateOf("All") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var deletingTask by remember { mutableStateOf<TaskEntity?>(null) }

    // Filter tasks based on status, priority, and Hero Search matching
    val filteredTasks = remember(tasks, selectedFilter, selectedPriority, searchQuery) {
        tasks.filter { task ->
            val statusOk = selectedFilter == "All" || task.status.equals(selectedFilter, ignoreCase = true)
            val priorityOk = selectedPriority == "All" || task.priority.equals(selectedPriority, ignoreCase = true)
            statusOk && priorityOk
        }.map { task ->
            task to calculateTaskMatchScore(task, searchQuery)
        }.filter { (_, score) ->
            searchQuery.isBlank() || score > 0.0
        }.sortedByDescending { it.second }
         .map { it.first }
    }

    // Fallback Hero Search: if strict filter gives 0 results
    val heroSimilarTasks = remember(tasks, searchQuery, filteredTasks) {
        if (searchQuery.isNotBlank() && filteredTasks.isEmpty()) {
            tasks.map { task -> task to calculateTaskMatchScore(task, searchQuery) }
                .filter { (_, score) -> score > 0.0 }
                .sortedByDescending { it.second }
                .map { it.first }
        } else {
            emptyList()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = GamifiedElectricBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Tasks Board",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "💡 Slide Right to Edit • Slide Left to Delete",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_input"),
                placeholder = {
                    Text(
                        text = "Search",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GamifiedElectricBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = GamifiedElectricBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Combined Filter Bar: Priority & Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Tag Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "High", "Medium", "Low").forEach { prio ->
                        val isSelected = selectedPriority.equals(prio, ignoreCase = true)
                        val chipColor = when (prio) {
                            "High" -> GamifiedFlameRed
                            "Medium" -> GamifiedFlameOrange
                            "Low" -> GamifiedEmerald
                            else -> GamifiedElectricBlue
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) chipColor else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isSelected) chipColor else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                .clickable { selectedPriority = prio }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("priority_chip_$prio")
                        ) {
                            Text(
                                text = if (prio == "All") "Priority: All" else prio,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Todo", "In Progress", "Done").forEach { filterName ->
                    val isSelected = selectedFilter == filterName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) GamifiedElectricBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedFilter = filterName }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filterName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Result summary bar
            if (searchQuery.isNotBlank() || selectedPriority != "All" || selectedFilter != "All") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${filteredTasks.size} task(s)",
                        color = GamifiedCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Reset filters",
                        color = GamifiedElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            selectedPriority = "All"
                            selectedFilter = "All"
                        }
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                if (heroSimilarTasks.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GamifiedElectricBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, GamifiedElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "✨ Hero Search: Similar Matches Found",
                                    color = GamifiedElectricBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "No exact matches in '$selectedFilter' status, but here are similar tasks:",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(heroSimilarTasks, key = { it.id }) { task ->
                                SwipeableTaskCard(
                                    task = task,
                                    onStatusChange = { newStatus ->
                                        viewModel.updateTaskStatus(task, newStatus)
                                    },
                                    onEdit = { editingTask = task },
                                    onDelete = { deletingTask = task }
                                )
                            }
                        }
                    }
                } else {
                    // Actionable Gamified Empty State with 1-Tap Quick Suggestions
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎯 Action Board Ready",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap a 1-tap suggestion below to immediately populate your task board:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            listOf(
                                "✨ Plan Tomorrow's Priorities" to "High",
                                "⚡ Strategy & Weekly Review" to "Medium",
                                "🌱 Organize Workspace & Desk" to "Low"
                            ).forEach { (sugTitle, sugPriority) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(GamifiedElectricBlue.copy(alpha = 0.15f))
                                        .border(1.dp, GamifiedElectricBlue, RoundedCornerShape(14.dp))
                                        .clickable {
                                            viewModel.addTask(sugTitle, "Quick suggestion created", sugPriority, "You", "Today")
                                        }
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sugTitle,
                                        color = GamifiedElectricBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        SwipeableTaskCard(
                            task = task,
                            onStatusChange = { newStatus ->
                                viewModel.updateTaskStatus(task, newStatus)
                            },
                            onEdit = { editingTask = task },
                            onDelete = { deletingTask = task }
                        )
                    }
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { title, desc, priority, assignee, dueDate ->
                    viewModel.addTask(title, desc, priority, assignee, dueDate)
                    showAddTaskDialog = false
                }
            )
        }

        editingTask?.let { targetTask ->
            EditTaskDialog(
                task = targetTask,
                onDismiss = { editingTask = null },
                onConfirm = { updatedTask ->
                    viewModel.updateTask(updatedTask)
                    editingTask = null
                }
            )
        }

        deletingTask?.let { targetTask ->
            AlertDialog(
                onDismissRequest = { deletingTask = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text("Delete Task?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Are you sure you want to delete \"${targetTask.title}\"? This action will permanently remove this task.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTask(targetTask.id)
                            deletingTask = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("confirm_delete_task_button")
                    ) {
                        Text("Delete Task", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingTask = null }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    task: TaskEntity,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    key(task.id) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onEdit()
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDelete()
                        false
                    }
                    else -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val bgColor = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> GamifiedElectricBlue
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF4444)
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    if (direction == SwipeToDismissBoxValue.StartToEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Task", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.White)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            TaskItemCard(
                task = task,
                onStatusChange = onStatusChange
            )
        }
    }
}

@Composable
fun EditTaskDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onConfirm: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var desc by remember { mutableStateOf(task.description) }
    var priority by remember { mutableStateOf(task.priority) }
    var assignee by remember { mutableStateOf(task.assignedTo) }
    var dueDate by remember { mutableStateOf(task.dueDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Edit Task", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_task_title_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_task_desc_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority (High/Med/Low)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GamifiedElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = assignee,
                        onValueChange = { assignee = it },
                        label = { Text("Tag / Assignee") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GamifiedElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            task.copy(
                                title = title,
                                description = desc,
                                priority = priority.ifBlank { "Medium" },
                                assignedTo = assignee.ifBlank { "You" },
                                dueDate = dueDate.ifBlank { "Today" }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GamifiedElectricBlue),
                modifier = Modifier.testTag("save_edit_task_button")
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, priority: String, assignee: String, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }
    var assignee by remember { mutableStateOf("You") }
    var dueDate by remember { mutableStateOf("Tomorrow") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Create Task", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority (High/Med)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GamifiedElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = assignee,
                        onValueChange = { assignee = it },
                        label = { Text("Tag / Category") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GamifiedElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, priority, assignee, dueDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GamifiedElectricBlue),
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text("Create Task", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}
