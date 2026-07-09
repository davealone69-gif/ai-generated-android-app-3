package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.UUID

// Data class to represent a single habit
data class Habit(
    val id: String = UUID.randomUUID().toString(), // Unique ID for keying in LazyColumn
    var name: String,
    var completedToday: Boolean = false // Track if the habit is completed for the current day
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply MaterialTheme for consistent Material Design styling
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

    // Add some sample habits when the screen is first composed
    LaunchedEffect(Unit) {
        if (habits.isEmpty()) {
            habits.add(Habit(name = "Drink 8 glasses of water"))
            habits.add(Habit(name = "Read for 30 minutes", completedToday = true))
            habits.add(Habit(name = "Exercise for 20 minutes"))
        }
    }

    // State to control the visibility of the "Add New Habit" dialog
    var showAddHabitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Daily Habits") }
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
            Spacer(modifier = Modifier.height(16.dp))

            if (habits.isEmpty()) {
                Text(
                    text = "No habits yet! Click the '+' button to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(habit = habit) {
                            // Find the habit by id and toggle its completion status
                            val index = habits.indexOfFirst { it.id == habit.id }
                            if (index != -1) {
                                habits[index] = habits[index].copy(completedToday = !habits[index].completedToday)
                            }
                        }
                    }
                }
            }
        }

        // Display the Add Habit dialog if showAddHabitDialog is true
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
}

@Composable
fun HabitItem(habit: Habit, onToggleComplete: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onToggleComplete(habit) } // Toggle completion on card click
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
                fontWeight = FontWeight.Normal,
                // Visually distinguish completed habits
                color = if (habit.completedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Checkbox(
                checked = habit.completedToday,
                onCheckedChange = { onToggleComplete(habit) } // Toggle completion on checkbox click
            )
        }
    }
}

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAddHabit: (String) -> Unit) {
    var newHabitName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Habit") },
        text = {
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                label = { Text("Habit Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newHabitName.isNotBlank()) { // Only add if habit name is not empty
                        onAddHabit(newHabitName.trim())
                    }
                },
                enabled = newHabitName.isNotBlank() // Enable button only if text field is not blank
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