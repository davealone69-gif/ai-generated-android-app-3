package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.UUID

// Define the Habit data class to store habit information
data class Habit(
    val id: String = UUID.randomUUID().toString(), // Unique ID for each habit
    var name: String,
    var description: String = "",
    var currentCount: Int = 0, // How many times the habit has been completed today
    val targetCount: Int = 1, // The daily target for this habit
    var lastCompletionDate: LocalDate = LocalDate.now().minusDays(1) // Tracks the last day it was updated, initialized to yesterday
) {
    /**
     * Resets the daily completion count if the last update was on a previous day.
     * This ensures the habit tracker is ready for a new day.
     */
    fun resetIfNewDay() {
        if (lastCompletionDate < LocalDate.now()) {
            currentCount = 0
            lastCompletionDate = LocalDate.now() // Update lastCompletionDate to today
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Entry point for the Jetpack Compose UI
            HabitTrackerAppScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerAppScreen() {
    // State to hold the list of habits. mutableStateListOf triggers recomposition
    // when items are added, removed, or changed.
    val habits = remember {
        mutableStateListOf(
            Habit(name = "Drink Water", description = "8 glasses a day", targetCount = 8),
            Habit(name = "Read Book", description = "30 minutes", targetCount = 1),
            Habit(name = "Exercise", description = "Workout for 45 minutes", targetCount = 1)
        )
    }

    // State to control the visibility of the "Add Habit" dialog
    var showAddHabitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Top app bar for the application title
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Habit Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            // Floating action button to add new habits
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp), // Horizontal padding for content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (habits.isEmpty()) {
                // Display a message if there are no habits
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "No habits yet! Click '+' to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                // LazyColumn to efficiently display a scrollable list of habits
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp), // Spacing between habit cards
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(habits, key = { it.id }) { habit ->
                        // Before displaying or interacting, check if it's a new day and reset the count
                        habit.resetIfNewDay()
                        HabitCard(
                            habit = habit,
                            onIncrement = {
                                // Find the habit in the list and update its current count
                                val index = habits.indexOfFirst { it.id == habit.id }
                                if (index != -1) {
                                    // Only increment if the daily target has not been met
                                    if (habits[index].currentCount < habits[index].targetCount) {
                                        // Update the habit object using copy() to ensure recomposition
                                        habits[index] = habits[index].copy(
                                            currentCount = habits[index].currentCount + 1,
                                            lastCompletionDate = LocalDate.now() // Set last completion to today
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Show the Add Habit dialog if showAddHabitDialog is true
        if (showAddHabitDialog) {
            AddHabitDialog(
                onDismiss = { showAddHabitDialog = false }, // Dismiss the dialog
                onAddHabit = { name, description, targetCount ->
                    // Add a new habit to the list
                    habits.add(Habit(name = name, description = description, targetCount = targetCount))
                    showAddHabitDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitCard(habit: Habit, onIncrement: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Display habit name
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f) // Takes available space
                )
                // Display current progress towards daily target
                Text(
                    text = "${habit.currentCount} / ${habit.targetCount}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (habit.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                // Display habit description if available
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Button to mark the habit as complete
            Button(
                onClick = onIncrement,
                modifier = Modifier.fillMaxWidth(),
                // Disable button if daily target is met
                enabled = habit.currentCount < habit.targetCount
            ) {
                Text(if (habit.currentCount < habit.targetCount) "Mark Complete" else "Daily Target Met!")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAddHabit: (name: String, description: String, targetCount: Int) -> Unit) {
    // States for input fields in the dialog
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var habitTarget by remember { mutableStateOf("1") } // Default target is 1

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
                    minLines = 2,
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = habitTarget,
                    onValueChange = { newValue ->
                        // Only allow numeric input and limit length for simplicity
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                            habitTarget = newValue
                        }
                    },
                    label = { Text("Daily Target (e.g., 1, 3)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Numeric keyboard
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = habitTarget.toIntOrNull() ?: 1 // Convert target to Int, default to 1
                    // Only add if habit name is not blank and target is valid
                    if (habitName.isNotBlank() && target > 0) {
                        onAddHabit(habitName, habitDescription, target)
                    }
                },
                // Enable button only if habit name is provided and target is a positive number
                enabled = habitName.isNotBlank() && (habitTarget.toIntOrNull() ?: 0) > 0
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