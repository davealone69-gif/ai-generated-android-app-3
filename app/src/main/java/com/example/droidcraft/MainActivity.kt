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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data class to represent a single habit
data class Habit(
    val id: Int,
    val name: String,
    var completedToday: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the default Material3 theme
            MaterialTheme {
                HabitTrackerAppScreen()
            }
        }
    }
}

@Composable
fun HabitTrackerAppScreen() {
    // State for the list of habits using mutableStateListOf for observable list changes
    val habits = remember { mutableStateListOf<Habit>() }

    // State for the new habit input field
    var newHabitText by remember { mutableStateOf("") }
    // Counter to generate unique IDs for new habits
    var nextHabitId by remember { mutableStateOf(0) }

    // Add some initial habits when the composable is first launched
    LaunchedEffect(Unit) {
        if (habits.isEmpty()) {
            habits.add(Habit(nextHabitId++, "Drink 8 glasses of water"))
            habits.add(Habit(nextHabitId++, "Read for 30 minutes"))
            habits.add(Habit(nextHabitId++, "Exercise for 15 minutes"))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use background from the theme
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "My Habits",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Section for adding a new habit
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = newHabitText,
                    onValueChange = { newHabitText = it },
                    label = { Text("New Habit Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newHabitText.isNotBlank()) {
                            // Add new habit and increment ID counter
                            habits.add(Habit(nextHabitId++, newHabitText.trim()))
                            newHabitText = "" // Clear the input field
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newHabitText.isNotBlank(), // Enable button only when text is not blank
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Habit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Habit")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title for the habit list
        Text(
            text = "Today's Habits",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Display a message if there are no habits
        if (habits.isEmpty()) {
            Text(
                text = "No habits added yet! Start by adding one above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Scrollable list of habits
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitItem(habit = habit) { completed ->
                        // Find the habit by ID and update its completion status
                        val index = habits.indexOfFirst { it.id == habit.id }
                        if (index != -1) {
                            // Create a copy to trigger recomposition for mutableStateListOf
                            habits[index] = habits[index].copy(completedToday = completed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggleCompletion: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (habit.completedToday) FontWeight.Medium else FontWeight.Normal,
                color = if (habit.completedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f) // Allows the text to take available space
            )
            // Checkbox to mark habit completion
            Checkbox(
                checked = habit.completedToday,
                onCheckedChange = onToggleCompletion,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}