package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    val id: String = UUID.randomUUID().toString(), // Unique ID for each habit
    val name: String,
    var isCompleted: Boolean = false
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

@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits. mutableStateListOf ensures recomposition when items are added/removed/updated.
    val habits = remember { mutableStateListOf<Habit>() }

    // State to control the visibility of the "Add New Habit" dialog
    var showAddHabitDialog by remember { mutableStateOf(false) }

    // State to hold the text input for a new habit's name within the dialog
    var newHabitName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simple Habit Tracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add new habit")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .fillMaxSize()
        ) {
            if (habits.isEmpty()) {
                // Display a message when there are no habits
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits yet! Click '+' to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Display the list of habits using LazyColumn for efficient scrolling
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Space between habit items
                ) {
                    items(items = habits, key = { it.id }) { habit ->
                        // Each habit item is a Composable that can be checked/unchecked
                        HabitItem(habit = habit) { updatedHabit ->
                            // When a habit's completion status changes, update it in the list
                            val index = habits.indexOfFirst { it.id == updatedHabit.id }
                            if (index != -1) {
                                habits[index] = updatedHabit // Replace the old habit with the updated one
                            }
                        }
                    }
                }
            }
        }

        // Dialog for adding a new habit
        if (showAddHabitDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddHabitDialog = false
                    newHabitName = "" // Clear input if dialog is dismissed
                },
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
                            if (newHabitName.isNotBlank()) {
                                habits.add(Habit(name = newHabitName.trim())) // Add new habit to the list
                                newHabitName = "" // Clear input after adding
                                showAddHabitDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        newHabitName = "" // Clear input on dismiss
                        showAddHabitDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onHabitCompletionChange: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Use MaterialTheme's default medium shape
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
                style = MaterialTheme.typography.titleMedium.copy(
                    // Visually indicate completed habits with a lighter font weight
                    fontWeight = if (habit.isCompleted) FontWeight.Light else FontWeight.Normal
                ),
                color = if (habit.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f) // Allows text to take available space
            )
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = { isChecked ->
                    // Propagate the change back to the parent Composable
                    onHabitCompletionChange(habit.copy(isCompleted = isChecked))
                }
            )
        }
    }
}