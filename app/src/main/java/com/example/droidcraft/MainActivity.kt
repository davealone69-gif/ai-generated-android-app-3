package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

// Data class for a habit
data class Habit(
    val id: Int,
    val name: String,
    val isCompletedToday: Boolean = false,
    val streak: Int = 0,
    val lastCompletionDate: LocalDate? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Provide a basic MaterialTheme for the single-file app
            HabitTrackerTheme {
                HabitTrackerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // State for the list of habits
    val habits: SnapshotStateList<Habit> = remember { mutableStateListOf() }

    // State for the new habit input field
    var newHabitName by remember { mutableStateOf("") }
    var nextHabitId by remember { mutableStateOf(0) } // Simple ID generator

    // Sample data for initial display
    LaunchedEffect(Unit) {
        if (habits.isEmpty()) {
            habits.add(Habit(nextHabitId++, "Drink 8 glasses of water"))
            habits.add(Habit(nextHabitId++, "Read for 30 minutes"))
            habits.add(Habit(nextHabitId++, "Exercise for 20 minutes"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Daily Habits", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main content area: Habit List
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits yet! Add one below.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = { toggledHabit ->
                                val index = habits.indexOfFirst { it.id == toggledHabit.id }
                                if (index != -1) {
                                    val today = LocalDate.now()
                                    val updatedHabit = if (!toggledHabit.isCompletedToday) {
                                        // Mark as complete
                                        val newStreak = if (toggledHabit.lastCompletionDate == null || toggledHabit.lastCompletionDate?.plusDays(1) != today) {
                                            1 // Reset or start new streak
                                        } else {
                                            toggledHabit.streak + 1 // Continue streak
                                        }
                                        toggledHabit.copy(
                                            isCompletedToday = true,
                                            streak = newStreak,
                                            lastCompletionDate = today
                                        )
                                    } else {
                                        // Unmark as complete (only affects today's status/streak if marked today)
                                        if (toggledHabit.lastCompletionDate == today) {
                                            val newStreak = (toggledHabit.streak - 1).coerceAtLeast(0)
                                            toggledHabit.copy(
                                                isCompletedToday = false,
                                                streak = newStreak,
                                                lastCompletionDate = if (newStreak == 0) null else toggledHabit.lastCompletionDate // Simplified: reset date if streak goes to 0
                                            )
                                        } else {
                                            // If marked on a previous day, just unmark for today without changing historical streak
                                            toggledHabit.copy(isCompletedToday = false)
                                        }
                                    }
                                    habits[index] = updatedHabit
                                }
                            },
                            onDelete = { habitToDelete ->
                                habits.remove(habitToDelete)
                            }
                        )
                    }
                }
            }

            // Add new habit section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("New Habit Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newHabitName.isNotBlank()) {
                                habits.add(Habit(nextHabitId++, newHabitName.trim()))
                                newHabitName = "" // Clear input field
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newHabitName.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Habit")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Habit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItem(habit: Habit, onToggleComplete: (Habit) -> Unit, onDelete: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = { onToggleComplete(habit) },
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Streak: ${habit.streak} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onDelete(habit) }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Checkbox(
                    checked = habit.isCompletedToday,
                    onCheckedChange = { _ -> onToggleComplete(habit) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

/**
 * A basic MaterialTheme wrapper for a single-file application.
 * In a real project, this would typically be defined in `ui.theme.Theme.kt`.
 */
@Composable
fun HabitTrackerTheme(
    content: @Composable () -> Unit
) {
    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF4CAF50), // Green for habits
        primaryContainer = Color(0xFFC8E6C9), // Lighter green
        onPrimary = Color.White,
        onPrimaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF8BC34A),
        secondaryContainer = Color(0xFFDCEDC8),
        onSecondary = Color.White,
        onSecondaryContainer = Color(0xFF33691E),
        background = Color(0xFFF8F8F8),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        surfaceVariant = Color(0xFFE0E0E0),
        onSurfaceVariant = Color(0xFF424242),
        error = Color(0xFFB00020),
        onError = Color.White,
        outline = Color(0xFFBDBDBD)
    )

    MaterialTheme(
        colorScheme = lightColorScheme,
        typography = Typography(), // Use default Typography
        content = content
    )
}