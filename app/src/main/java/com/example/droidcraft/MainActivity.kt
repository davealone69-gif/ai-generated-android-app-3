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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.UUID

// Main Activity class
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply MaterialTheme to the entire app
            MaterialTheme {
                // A surface container that uses the 'background' color from the theme
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

// Data class to represent a single habit
data class Habit(
    val id: String = UUID.randomUUID().toString(), // Unique ID for list keys and identification
    var name: String,
    var description: String = "",
    var currentStreak: Int = 0,
    var lastCompletedDate: LocalDate? = null // Tracks the last date the habit was marked complete
) {
    // Helper function to check if the habit was completed on the current day
    fun isCompletedToday(): Boolean {
        return lastCompletedDate == LocalDate.now()
    }
}

// Main Composable for the Habit Tracker Screen
@OptIn(ExperimentalMaterial3Api::class) // Required for CenterAlignedTopAppBar
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits. mutableStateListOf allows observable changes to the list.
    val habits: SnapshotStateList<Habit> = remember { mutableStateListOf() }

    // State for managing the "Add New Habit" dialog visibility and its input fields
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitDescription by remember { mutableStateOf("") }

    // Function to handle the logic of marking a habit as complete
    fun markHabitComplete(habitId: String) {
        val index = habits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val habit = habits[index]
            val today = LocalDate.now()

            // If the habit is already completed today, do nothing
            if (habit.lastCompletedDate == today) {
                return
            }

            // Create a copy of the habit to modify, ensuring recomposition for the specific item
            val updatedHabit = habit.copy()

            // Streak logic:
            // - If it's the first completion or completed yesterday, increment streak.
            // - Otherwise (gap in completion), reset streak to 1.
            if (updatedHabit.lastCompletedDate == null || updatedHabit.lastCompletedDate == today.minusDays(1)) {
                updatedHabit.currentStreak++
            } else {
                updatedHabit.currentStreak = 1 // Streak broken, start new streak
            }
            updatedHabit.lastCompletedDate = today // Update the last completion date to today

            // Replace the old habit object with the updated one in the list to trigger recomposition
            habits[index] = updatedHabit
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Habit Tracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        }
    ) { paddingValues ->
        // Display a message if there are no habits, otherwise show the list
        if (habits.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No habits yet! Tap '+' to add your first habit.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null, // Visual element, no description needed for screen readers
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            // LazyColumn for efficient rendering of a scrollable list of habits
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp) // Space between habit cards
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitItem(
                        habit = habit,
                        onMarkComplete = { markHabitComplete(habit.id) }
                    )
                }
            }
        }

        // Dialog for adding a new habit
        if (showAddHabitDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Reset input fields and close dialog if dismissed
                    newHabitName = ""
                    newHabitDescription = ""
                    showAddHabitDialog = false
                },
                title = { Text("Add New Habit") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newHabitName,
                            onValueChange = { newHabitName = it },
                            label = { Text("Habit Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newHabitDescription,
                            onValueChange = { newHabitDescription = it },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newHabitName.isNotBlank()) {
                                habits.add(
                                    Habit(
                                        name = newHabitName.trim(),
                                        description = newHabitDescription.trim()
                                    )
                                )
                                // Clear input fields and close dialog
                                newHabitName = ""
                                newHabitDescription = ""
                                showAddHabitDialog = false
                            }
                        }
                    ) {
                        Text("Add Habit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        // Clear input fields and close dialog
                        newHabitName = ""
                        newHabitDescription = ""
                        showAddHabitDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// Composable for displaying a single habit item
@Composable
fun HabitItem(habit: Habit, onMarkComplete: () -> Unit) {
    // Determine if the habit has been completed today to update UI accordingly
    val isCompletedToday = habit.isCompletedToday()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Change card background based on completion status
            containerColor = if (isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Habit details (name, description, streak)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Streak indicator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Streak: ${habit.currentStreak} day${if (habit.currentStreak != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Button to mark the habit complete
            Button(
                onClick = onMarkComplete,
                // Disable the button if the habit is already completed today
                enabled = !isCompletedToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(if (isCompletedToday) "Completed!" else "Mark Complete")
            }
        }
    }
}

// Preview function for the HabitTrackerScreen
@Preview(showBackground = true)
@Composable
fun PreviewHabitTrackerScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HabitTrackerScreen()
        }
    }
}