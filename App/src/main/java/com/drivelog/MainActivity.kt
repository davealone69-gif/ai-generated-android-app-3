package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2E7D32), // Forest Green
                    onPrimary = Color.White,
                    secondary = Color(0xFF81C784), // Light Sage Green
                    background = Color(0xFFF1F8E9), // Pale Greenish White
                    surface = Color.White
                )
            ) {
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
    val id: String,
    val name: String,
    val streak: Int,
    val isCompletedToday: Boolean,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit("1", "Drink 8 glasses of water", 5, true, "💧"),
                Habit("2", "Read 10 pages", 3, false, "📚"),
                Habit("3", "Exercise for 30 mins", 0, false, "🏃‍♂️"),
                Habit("4", "Meditate & breathe", 12, true, "🧘")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "HabitGrow",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🌿", fontSize = 24.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Reset all habits to incomplete for testing/new day
                        habits = habits.map { it.copy(isCompletedToday = false) }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Progress",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Dashboard Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$completedCount of $totalCount habits done!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "progress")
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    )

                    val motivationalQuote = when {
                        progressFraction == 0f -> "Start your day strong! Click below to complete a habit."
                        progressFraction < 0.5f -> "Good start! You're making steady progress."
                        progressFraction < 1.0f -> "Almost there! Keep pushing to hit 100%!"
                        else -> "Amazing job! You finished all of today's habits! 🎉"
                    }

                    Text(
                        text = motivationalQuote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Habits Section Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Habits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Streaks",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Habits List
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No habits tracked yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the + button to add your first habit!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = {
                                habits = habits.map {
                                    if (it.id == habit.id) {
                                        val nextCompletedState = !it.isCompletedToday
                                        val nextStreak = if (nextCompletedState) it.streak + 1 else maxOf(0, it.streak - 1)
                                        it.copy(isCompletedToday = nextCompletedState, streak = nextStreak)
                                    } else {
                                        it
                                    }
                                }
                            },
                            onDelete = {
                                habits = habits.filter { it.id != habit.id }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAddHabit = { name, emoji ->
                val newHabit = Habit(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    streak = 0,
                    isCompletedToday = false,
                    icon = emoji
                )
                habits = habits + newHabit
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (habit.isCompletedToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surface,
        label = "containerColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Emoji Icon Circular Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = if (habit.isCompletedToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            else Color(0xFFF1F8E9),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.icon, fontSize = 24.sp)
                }

                // Habit Name and Streak
                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (habit.isCompletedToday) Color.Gray else Color.Black
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔥 ${habit.streak} day streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (habit.streak > 0) Color(0xFFE65100) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Actions: Complete and Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Complete Toggle Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (habit.isCompletedToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark Complete",
                        tint = if (habit.isCompletedToday) Color.White else MaterialTheme.colorScheme.primary
                    )
                }

                // Delete Button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (name: String, emoji: String) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("💧") }
    val emojiOptions = listOf("💧", "📚", "🏃‍♂️", "🧘", "🍎", "💤", "💻", "🎨", "🌿", "🏋️")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add New Habit ✨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Read physical books") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose an Icon Badge:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    // Emoji Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojiOptions.take(5).forEach { emoji ->
                            EmojiBadge(
                                emoji = emoji,
                                isSelected = selectedEmoji == emoji,
                                onClick = { selectedEmoji = emoji }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojiOptions.drop(5).forEach { emoji ->
                            EmojiBadge(
                                emoji = emoji,
                                isSelected = selectedEmoji == emoji,
                                onClick = { selectedEmoji = emoji }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (habitName.isNotBlank()) {
                                onAddHabit(habitName, selectedEmoji)
                            }
                        },
                        enabled = habitName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun EmojiBadge(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "border"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color(0xFFF1F8E9)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 22.sp)
    }
}