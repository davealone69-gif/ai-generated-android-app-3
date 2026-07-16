package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.UUID

// Data class for a habit
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var lastCompletedDate: LocalDate? = null
) {
    // Check if the habit was completed on a specific date (e.g., 'today')
    fun isCompletedOnDate(date: LocalDate): Boolean {
        return lastCompletedDate == date
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerScreen()
        }
    }
}

@Composable
fun HabitTrackerScreen() {
    // List of habits, using mutableStateListOf to allow adding/removing/updating items
    val habits: SnapshotStateList<Habit> = remember {
        mutableStateListOf(
            Habit(name = "Drink 8 glasses of water"),
            Habit(name = "Read 15 minutes"),
            Habit(name = "Exercise for 30 minutes", lastCompletedDate = LocalDate.now().minusDays(1)), // Completed yesterday
            Habit(name = "Meditate for 10 minutes", lastCompletedDate = LocalDate.now()) // Completed today
        )
    }

    var showAddHabitDialog by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() } // Get today's date once for the screen's scope

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Header section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Simple Habit Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track your daily progress for ${today.dayOfMonth} ${today.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Habits List or Empty State
                if (habits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Take available space
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No habits yet! Click '+' to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Take remaining space
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            HabitItem(habit = habit, today = today) { updatedHabit ->
                                // Find the index of the habit and update it in the list
                                val index = habits.indexOfFirst { it.id == updatedHabit.id }
                                if (index != -1) {
                                    habits[index] = updatedHabit // Replace with the updated habit
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    // Dialog for adding a new habit
    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onAddHabit = { newHabitName ->
                habits.add(Habit(name = newHabitName))
                showAddHabitDialog = false
            }
        )
    }
}

@Composable
fun HabitItem(habit: Habit, today: LocalDate, onHabitUpdated: (Habit) -> Unit) {
    // isChecked state is specific to this composable instance and its current 'today'
    // 'remember(habit.id, today)' ensures state resets if habit or 'today' changes
    var isChecked by remember(habit.id, today) { mutableStateOf(habit.isCompletedOnDate(today)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    isChecked = checked // Update local UI state
                    val updatedHabit = habit.copy() // Create a copy to update data
                    if (checked) {
                        updatedHabit.lastCompletedDate = today
                    } else {
                        updatedHabit.lastCompletedDate = null // Allow unchecking for today
                    }
                    onHabitUpdated(updatedHabit) // Notify parent to update the list
                }
            )
        }
    }
}

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAddHabit: (String) -> Unit) {
    var habitName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Habit") },
        text = {
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Habit Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        onAddHabit(habitName.trim())
                    }
                },
                enabled = habitName.isNotBlank() // Enable button only if text is not blank
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}