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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// Data class to represent a single habit
data class Habit(
    val id: UUID = UUID.randomUUID(), // Unique ID for each habit
    var name: String,
    var description: String,
    var completedCount: Int = 0,
    var lastCompletedDate: LocalDate? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply a simple Material Theme for consistency
            MaterialTheme {
                HabitTrackerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits using a mutableStateListOf for observable list changes
    val habits = remember { mutableStateListOf<Habit>() }

    // State to control the visibility of the "Add Habit" dialog
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
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp), // Additional horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (habits.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "No habits yet! Click '+' to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onMarkComplete = { habitToComplete ->
                                val today = LocalDate.now()
                                // Only mark complete if not already completed today
                                if (habitToComplete.lastCompletedDate != today) {
                                    val index = habits.indexOfFirst { it.id == habitToComplete.id }
                                    if (index != -1) {
                                        // Update the habit in the list using copy for recomposition
                                        habits[index] = habits[index].copy(
                                            completedCount = habits[index].completedCount + 1,
                                            lastCompletedDate = today
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showAddHabitDialog) {
            AddHabitDialog(
                onDismiss = { showAddHabitDialog = false },
                onAddHabit = { name, description ->
                    habits.add(Habit(name = name, description = description))
                    showAddHabitDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitCard(habit: Habit, onMarkComplete: (Habit) -> Unit) {
    // Date formatter for display
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    // Check if the habit was completed today
    val isCompletedToday = habit.lastCompletedDate == LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        // Change card color based on completion status
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = habit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Completed: ${habit.completedCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    habit.lastCompletedDate?.let { date ->
                        Text(
                            text = "Last: ${date.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = { onMarkComplete(habit) },
                    enabled = !isCompletedToday // Disable if already completed today
                ) {
                    Text(if (isCompletedToday) "Completed Today" else "Mark Complete")
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (String, String) -> Unit
) {
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
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) { // Only add if habit name is not blank
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