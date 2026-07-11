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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

// Data class to represent a single habit
data class Habit(
    val id: Int, // Unique ID for each habit, good for LazyColumn keys
    var name: String,
    var isCompletedToday: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Replaced MapsAppScreen with the new HabitTrackerScreen
            HabitTrackerScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // State for managing the list of habits
    val habits = remember { mutableStateListOf<Habit>() }
    // State for controlling the visibility of the add habit dialog
    var showAddHabitDialog by remember { mutableStateOf(false) }
    // State for the text input in the add habit dialog
    var newHabitNameInput by remember { mutableStateOf("") }
    // Counter to generate unique IDs for new habits
    var nextHabitId by remember { mutableStateOf(0) }

    // Pre-populate with some initial habits when the screen first loads
    LaunchedEffect(Unit) {
        if (habits.isEmpty()) {
            habits.add(Habit(nextHabitId++, "Drink 8 glasses of water", false))
            habits.add(Habit(nextHabitId++, "Read for 30 minutes", false))
            habits.add(Habit(nextHabitId++, "Exercise for 1 hour", true))
        }
    }

    Scaffold(
        // Floating action button for adding new habits
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply system padding from Scaffold
                    .padding(24.dp), // Additional padding for content
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Simple Habit Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track your daily progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Habits List Section (Replaces the "Map Simulation Grid" from boilerplate)
                if (habits.isEmpty()) {
                    // Display a message if no habits are added yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes available space
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No habits yet! Click '+' to add one.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Display the list of habits using LazyColumn for efficient scrolling
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Takes available vertical space
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(items = habits, key = { it.id }) { habit ->
                            HabitCard(
                                habit = habit,
                                onToggleCompletion = { toggledHabit ->
                                    // Find the habit by ID and update its completion status
                                    val index = habits.indexOfFirst { it.id == toggledHabit.id }
                                    if (index != -1) {
                                        habits[index] = toggledHabit.copy(isCompletedToday = !toggledHabit.isCompletedToday)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Habit Dialog (Displayed when showAddHabitDialog is true)
                if (showAddHabitDialog) {
                    AddHabitDialog(
                        onAddHabit = { name ->
                            if (name.isNotBlank()) {
                                habits.add(Habit(nextHabitId++, name.trim(), false))
                                newHabitNameInput = "" // Clear input after adding
                            }
                            showAddHabitDialog = false // Close dialog
                        },
                        onDismiss = {
                            newHabitNameInput = "" // Clear input on dismiss
                            showAddHabitDialog = false // Close dialog
                        },
                        newHabitName = newHabitNameInput,
                        onNewHabitNameChange = { newHabitNameInput = it }
                    )
                }
            }
        }
    )
}

/**
 * Composable for displaying a single habit card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(habit: Habit, onToggleCompletion: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Change card color based on completion status
            containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onToggleCompletion(habit) } // Make the whole card clickable to toggle
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
                fontWeight = FontWeight.SemiBold,
                color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f) // Allow text to take most space
            )
            // Checkbox to visually represent and interact with habit completion
            Checkbox(
                checked = habit.isCompletedToday,
                onCheckedChange = { onToggleCompletion(habit) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Composable for an alert dialog to add a new habit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onAddHabit: (String) -> Unit,
    onDismiss: () -> Unit,
    newHabitName: String,
    onNewHabitNameChange: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add New Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Text field for entering the new habit name
                OutlinedTextField(
                    value = newHabitName,
                    onValueChange = onNewHabitNameChange,
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAddHabit(newHabitName) },
                        // Enable the "Add" button only if the habit name input is not blank
                        enabled = newHabitName.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}