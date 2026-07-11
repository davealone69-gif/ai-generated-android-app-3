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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data class to represent a single habit
data class Habit(
    val name: String,
    var isCompletedToday: Boolean = false // Tracks if the habit is completed for the current day
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply Material 3 design system theme
            MaterialTheme {
                HabitTrackerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits. mutableStateListOf is used for observable list changes.
    val habits: SnapshotStateList<Habit> = remember {
        mutableStateListOf(
            Habit("Drink 8 glasses of water"),
            Habit("Read for 30 minutes"),
            Habit("Exercise for 45 minutes", isCompletedToday = true),
            Habit("Meditate for 10 minutes"),
            Habit("Learn a new language"),
            Habit("Journal for 15 minutes")
        )
    }

    // State to control the visibility of the Add Habit dialog
    var showAddHabitDialog by remember { mutableStateOf(false) }
    // State to hold the text input for a new habit name in the dialog
    var newHabitNameInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simple Habit Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                    text = "No habits added yet! Click the '+' button to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp)) // Space below TopAppBar
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between habit cards
                ) {
                    items(habits) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = { toggledHabit ->
                                // Find the habit in the list and update its 'isCompletedToday' status
                                val index = habits.indexOf(toggledHabit)
                                if (index != -1) {
                                    // Creating a copy triggers recomposition for the item
                                    habits[index] = habits[index].copy(isCompletedToday = !habits[index].isCompletedToday)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Show the dialog if showAddHabitDialog state is true
        if (showAddHabitDialog) {
            AddHabitDialog(
                newHabitName = newHabitNameInput,
                onNewHabitNameChange = { newHabitNameInput = it },
                onDismiss = {
                    showAddHabitDialog = false
                    newHabitNameInput = "" // Clear input when dialog is dismissed
                },
                onAddHabit = { habitName ->
                    habits.add(Habit(habitName)) // Add the new habit to the list
                    showAddHabitDialog = false
                    newHabitNameInput = "" // Clear input after adding habit
                }
            )
        }
    }
}

/**
 * Composable for displaying a single habit item.
 * Features a Card with habit name and a Switch to mark completion.
 */
@Composable
fun HabitItem(habit: Habit, onToggleComplete: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Change card background based on completion status
            containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
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
                // Change text color based on completion status
                color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f) // Allow text to take available space
            )
            Switch(
                checked = habit.isCompletedToday,
                onCheckedChange = { onToggleComplete(habit) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            )
        }
    }
}

/**
 * Composable for an AlertDialog to add a new habit.
 * Contains a TextField for input and buttons for confirm/cancel.
 */
@Composable
fun AddHabitDialog(
    newHabitName: String,
    onNewHabitNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddHabit: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Habit") },
        text = {
            TextField(
                value = newHabitName,
                onValueChange = onNewHabitNameChange,
                label = { Text("Habit Name") },
                singleLine = true, // Ensure input stays on one line
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newHabitName.isNotBlank()) {
                        onAddHabit(newHabitName.trim()) // Add habit, trimming whitespace
                    }
                },
                enabled = newHabitName.isNotBlank() // Enable button only if text field is not empty
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