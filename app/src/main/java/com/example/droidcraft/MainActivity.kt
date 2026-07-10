package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerAppTheme { // Using a simple theme wrapper for Material3 defaults
                HabitTrackerScreen()
            }
        }
    }
}

@Composable
fun HabitTrackerAppTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

data class Habit(
    val id: Int,
    val name: String,
    var isCompletedToday: Boolean
)

@Composable
fun HabitTrackerScreen() {
    val habits = remember {
        mutableStateListOf(
            Habit(1, "Drink 8 glasses of water", false),
            Habit(2, "Read for 30 minutes", true),
            Habit(3, "Exercise for 1 hour", false),
            Habit(4, "Meditate for 15 minutes", false),
            Habit(5, "Plan tomorrow's tasks", false)
        )
    }
    var newHabitName by remember { mutableStateOf("") }
    var nextHabitId by remember { mutableStateOf(habits.size + 1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), // Preserve template padding style
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Preserve template arrangement
    ) {
        // App Title Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "My Daily Habits",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stay consistent, achieve more!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Habits List Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp) // Preserve template height style
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (habits.isEmpty()) {
                Text(
                    text = "No habits added yet! Use the section below.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(habits) { habit ->
                        HabitItem(habit = habit) { checked ->
                            // Find the habit and update its state
                            val index = habits.indexOfFirst { it.id == habit.id }
                            if (index != -1) {
                                // Create a new instance to trigger recomposition
                                habits[index] = habits[index].copy(isCompletedToday = checked)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }

        // Add Habit and Actions Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Manage Habits",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newHabitName,
                    onValueChange = { newHabitName = it },
                    label = { Text("Add a new habit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            habits.add(Habit(nextHabitId++, newHabitName.trim(), false))
                            newHabitName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newHabitName.isNotBlank()
                ) {
                    Text("Add Habit")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        // Reset all habits for the day
                        habits.forEachIndexed { index, habit ->
                            habits[index] = habit.copy(isCompletedToday = false)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors() // Use outlined style for secondary action
                ) {
                    Text("Reset Daily Progress")
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = habit.isCompletedToday,
            onCheckedChange = onCheckedChange
        )
    }
}