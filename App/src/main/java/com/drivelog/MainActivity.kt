package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerScreen()
                }
            }
        }
    }
}

data class Habit(
    val id: Int,
    val name: String,
    val category: String,
    val streak: Int,
    val isCompletedToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(1, "Drink 8 glasses of water", "Health", 5, true),
                Habit(2, "Read for 20 minutes", "Mind", 12, false),
                Habit(3, "30 mins workout", "Fitness", 3, false),
                Habit(4, "Morning Meditation", "Mind", 8, true)
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var filterSelected by remember { mutableStateOf("All") }

    val categories = listOf("All", "Health", "Mind", "Fitness", "Work")

    // Filtered list
    val filteredHabits = remember(habits, filterSelected) {
        if (filterSelected == "All") {
            habits
        } else {
            habits.filter { it.category == filterSelected }
        }
    }

    // Stats calculations
    val totalHabits = habits.size
    val completedCount = habits.count { it.isCompletedToday }
    val completionProgress = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Habit Builder",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Build discipline everyday",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) { 
            // Summary progress card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$completedCount of $totalHabits Completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${(completionProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = completionProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Categories horizontal picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = filterSelected == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { filterSelected = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Habits List
            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits to show for this filter. Press '+' to add!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        HabitItemCard(
                            habit = habit,
                            onToggleComplete = { toggledHabit ->
                                habits = habits.map {
                                    if (it.id == toggledHabit.id) {
                                        val nextCompleted = !it.isCompletedToday
                                        val newStreak = if (nextCompleted) it.streak + 1 else maxOf(0, it.streak - 1)
                                        it.copy(isCompletedToday = nextCompleted, streak = newStreak)
                                    } else {
                                        it
                                    }
                                }
                            },
                            onDelete = { habitId ->
                                habits = habits.filterNot { it.id == habitId }
                            }
                        )
                    }
                }
            }
        }
    }

    // New Habit Dialog
    if (showAddDialog) {
        var habitNameInput by remember { mutableStateOf("") }
        var selectedCategoryInput by remember { mutableStateOf("Health") }
        val formCategories = listOf("Health", "Mind", "Fitness", "Work")

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add New Habit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = habitNameInput,
                        onValueChange = { habitNameInput = it },
                        label = { Text("Habit Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        formCategories.forEach { categoryItem ->
                            val isSelectedInput = selectedCategoryInput == categoryItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelectedInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedCategoryInput = categoryItem }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = categoryItem,
                                    color = if (isSelectedInput) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (habitNameInput.isNotBlank()) {
                                    val nextId = if (habits.isNotEmpty()) habits.maxOf { it.id } + 1 else 1
                                    habits = habits + Habit(
                                        id = nextId,
                                        name = habitNameInput,
                                        category = selectedCategoryInput,
                                        streak = 0,
                                        isCompletedToday = false
                                    )
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItemCard(
    habit: Habit,
    onToggleComplete: (Habit) -> Unit,
    onDelete: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                when (habit.category) {
                                    "Health" -> Color(0xFFE8F5E9)
                                    "Mind" -> Color(0xFFE3F2FD)
                                    "Fitness" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFEDE7F6)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = habit.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (habit.category) {
                                "Health" -> Color(0xFF2E7D32)
                                "Mind" -> Color(0xFF1565C0)
                                "Fitness" -> Color(0xFFEF6C00)
                                else -> Color(0xFF651FFF)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔥 ${habit.streak} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6D00),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (habit.isCompletedToday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Delete Button
                IconButton(onClick = { onDelete(habit.id) }) {
                    Text(
                        text = "🗑️",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Custom Checkbox Container
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else Color.Gray.copy(
                                alpha = 0.2f
                            )
                        )
                        .clickable { onToggleComplete(habit) },
                    contentAlignment = Alignment.Center
                ) {
                    if (habit.isCompletedToday) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}