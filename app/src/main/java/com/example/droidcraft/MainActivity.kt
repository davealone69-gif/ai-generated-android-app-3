package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data class to represent a single habit
data class Habit(
    val id: Int,
    var name: String,
    var description: String,
    var completedToday: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply a simple Material3 theme
            MaterialTheme {
                HabitTrackerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits
    val habits = remember { mutableStateListOf<Habit>() }
    // State to control the visibility of the "Add New Habit" dialog
    var showAddHabitDialog by remember { mutableStateOf(false) }
    // Simple ID generator for new habits
    var nextHabitId by remember { mutableStateOf(0) }

    // Pre-populate some habits when the screen first loads
    LaunchedEffect(Unit) {
        if (habits.isEmpty()) {
            habits.add(Habit(nextHabitId++, "Drink 8 glasses of water", "Stay hydrated throughout the day.", false))
            habits.add(Habit(nextHabitId++, "Read 30 minutes", "Expand your knowledge daily.", false))
            habits.add(Habit(nextHabitId++, "Exercise for 20 minutes", "Boost your energy and health.", false))
            habits.add(Habit(nextHabitId++, "Meditate for 10 minutes", "Calm your mind and reduce stress.", false))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Daily Habits", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (habits.isEmpty()) {
                Text(
                    text = "No habits yet! Click the '+' button to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = { toggledHabit ->
                                // Find the habit in the list and update its 'completedToday' status
                                val index = habits.indexOfFirst { it.id == toggledHabit.id }
                                if (index != -1) {
                                    habits[index] = habits[index].copy(completedToday = !habits[index].completedToday)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Show the dialog for adding a new habit
        if (showAddHabitDialog) {
            AddHabitDialog(
                onAddHabit = { name, description ->
                    habits.add(Habit(nextHabitId++, name, description, false))
                    showAddHabitDialog = false
                },
                onDismiss = { showAddHabitDialog = false }
            )
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggleComplete: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // Change card color based on completion status
            containerColor = if (habit.completedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleComplete(habit) } // Make the whole card clickable
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = habit.completedToday,
                onCheckedChange = { _ -> onToggleComplete(habit) }, // Checkbox also toggles completion
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (habit.completedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (habit.completedToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(onAddHabit: (String, String) -> Unit, onDismiss: () -> Unit) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = habitDescription,
                    onValueChange = { habitDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) { // Only add if habit name is not empty
                        onAddHabit(habitName.trim(), habitDescription.trim())
                    }
                },
                enabled = habitName.isNotBlank() // Enable button only if habit name is entered
            ) {
                Text("Add Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}