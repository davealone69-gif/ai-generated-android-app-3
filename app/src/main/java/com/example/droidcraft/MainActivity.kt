package com.example.droidcraft

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Data class to represent a single habit
data class Habit(val id: Int, var name: String, var isCompleted: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply MaterialTheme for consistent look and feel
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
    // State for the text input field when adding a new habit
    var newHabitText by remember { mutableStateOf("") }
    // Counter to generate unique IDs for habits
    var habitIdCounter by remember { mutableStateOf(0) }

    // Add some initial habits when the composable is first launched
    LaunchedEffect(Unit) {
        if (habits.isEmpty()) {
            habits.add(Habit(habitIdCounter++, "Drink 8 glasses of water"))
            habits.add(Habit(habitIdCounter++, "Read for 30 minutes"))
            habits.add(Habit(habitIdCounter++, "Exercise for 15 minutes"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Habits") }
            )
        },
        floatingActionButton = {
            // Floating Action Button to add a new habit
            FloatingActionButton(onClick = {
                if (newHabitText.isNotBlank()) {
                    habits.add(Habit(habitIdCounter++, newHabitText.trim()))
                    newHabitText = "" // Clear the input field after adding
                }
            }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Input field for entering a new habit name
                OutlinedTextField(
                    value = newHabitText,
                    onValueChange = { newHabitText = it },
                    label = { Text("New Habit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Display the list of habits
                if (habits.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No habits yet! Add one using the + button.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Allow the list to take remaining space
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            HabitItem(habit = habit) { updatedHabit ->
                                // Find the habit in the list and update its state
                                val index = habits.indexOfFirst { it.id == updatedHabit.id }
                                if (index != -1) {
                                    habits[index] = updatedHabit
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun HabitItem(habit: Habit, onHabitChanged: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
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
                style = MaterialTheme.typography.bodyLarge,
                // Change text color if habit is completed
                color = if (habit.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f) // Text takes available space
            )
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = { isChecked ->
                    // When checkbox is toggled, call the callback with the updated habit
                    onHabitChanged(habit.copy(isCompleted = isChecked))
                }
            )
        }
    }
}