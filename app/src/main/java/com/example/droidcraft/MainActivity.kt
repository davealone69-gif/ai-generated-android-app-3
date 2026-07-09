package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.droidcraft.ui.theme.DroidCraftTheme // Assuming default theme is here

// Data class to represent a single habit
data class Habit(val id: Int, var name: String, var isCompletedToday: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DroidCraftTheme { // Wrap the app screen with the theme
                HabitTrackerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits
    val habits: SnapshotStateList<Habit> = remember {
        mutableStateListOf(
            Habit(1, "Drink 8 Glasses of Water", false),
            Habit(2, "Read for 30 Minutes", true),
            Habit(3, "Exercise for 1 Hour", false)
        )
    }

    // State for the new habit input field
    var newHabitName by remember { mutableStateOf("") }
    var nextHabitId by remember { mutableStateOf(habits.maxOfOrNull { it.id }?.plus(1) ?: 1) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Use spacedBy for consistent spacing
    ) {
        // App Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "My Habit Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your daily progress!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Habits List Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Make the list take available space
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Today's Habits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (habits.isEmpty()) {
                    Text(
                        text = "No habits added yet! Add some below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            HabitItem(habit = habit) { clickedHabit ->
                                // Find the habit in the list and toggle its completion status
                                val index = habits.indexOfFirst { it.id == clickedHabit.id }
                                if (index != -1) {
                                    habits[index] = habits[index].copy(isCompletedToday = !habits[index].isCompletedToday)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add New Habit Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add New Habit",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newHabitName,
                    onValueChange = { newHabitName = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            habits.add(Habit(nextHabitId++, newHabitName.trim()))
                            newHabitName = "" // Clear the input field
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newHabitName.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Habit")
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onHabitClick: (Habit) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHabitClick(habit) },
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Checkbox(
                checked = habit.isCompletedToday,
                onCheckedChange = { onHabitClick(habit) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// Preview function for the HabitTrackerScreen
@Preview(showBackground = true)
@Composable
fun PreviewHabitTrackerScreen() {
    DroidCraftTheme {
        HabitTrackerScreen()
    }
}