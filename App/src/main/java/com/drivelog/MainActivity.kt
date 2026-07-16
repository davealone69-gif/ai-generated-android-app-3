package com.drivelog

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
import androidx.compose.ui.graphics.Color
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
            HabitTrackerScreen()
        }
    }
}

@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits
    val habits = remember { mutableStateListOf<Habit>() }
    // State to hold the text for a new habit
    var newHabitText by remember { mutableStateOf("") }
    // State to generate unique IDs for habits
    var nextHabitId by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Distribute space
    ) {
        // App Title and Subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Simple Habit Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Build good habits, track your progress!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Habit List Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Occupy available vertical space
                .background(Color(0xFFE0F7FA), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter // Align content to the top
        ) {
            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No habits yet! Add some below.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF006064)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habits) { habit ->
                        HabitItem(habit = habit) { isChecked ->
                            // Update the completedToday status for the specific habit
                            val index = habits.indexOfFirst { it.id == habit.id }
                            if (index != -1) {
                                habits[index] = habits[index].copy(completedToday = isChecked)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add New Habit Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = newHabitText,
                    onValueChange = { newHabitText = it },
                    label = { Text("New Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newHabitText.isNotBlank()) {
                            habits.add(Habit(id = nextHabitId++, name = newHabitText.trim()))
                            newHabitText = "" // Clear the input field
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newHabitText.isNotBlank() // Enable button only when text is not blank
                ) {
                    Text("Add Habit")
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (habit.completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f) // Take up most of the space
            )
            Checkbox(
                checked = habit.completedToday,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}