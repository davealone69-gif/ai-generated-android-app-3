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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerScreen()
        }
    }
}

data class Habit(val id: Int, val name: String, var isCompleted: Boolean = false)

@Composable
fun HabitTrackerScreen() {
    val habits = remember { mutableStateListOf<Habit>() }
    var newHabitName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
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
                text = "Stay consistent and track your progress!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Habit List Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Occupy available space
                .background(Color(0xFFE0F7FA), RoundedCornerShape(16.dp)) // Light blue background
                .padding(16.dp),
            contentAlignment = if (habits.isEmpty()) Alignment.Center else Alignment.TopStart
        ) {
            if (habits.isEmpty()) {
                Text(
                    text = "No habits yet! Add some below.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF006064) // Darker blue text
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(habit = habit) { habitId, isChecked ->
                            val index = habits.indexOfFirst { it.id == habitId }
                            if (index != -1) {
                                habits[index] = habits[index].copy(isCompleted = isChecked)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Spacer between list and input card

        // Add New Habit Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = newHabitName,
                    onValueChange = { newHabitName = it },
                    label = { Text("New Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            val newId = (habits.maxOfOrNull { it.id } ?: 0) + 1
                            habits.add(Habit(id = newId, name = newHabitName.trim()))
                            newHabitName = "" // Clear input field
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newHabitName.isNotBlank() // Enable button only when text is present
                ) {
                    Text("Add Habit")
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggleComplete: (Int, Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.titleMedium,
            color = if (habit.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            fontWeight = if (habit.isCompleted) FontWeight.Normal else FontWeight.Medium
        )
        Checkbox(
            checked = habit.isCompleted,
            onCheckedChange = { isChecked ->
                onToggleComplete(habit.id, isChecked)
            }
        )
    }
}