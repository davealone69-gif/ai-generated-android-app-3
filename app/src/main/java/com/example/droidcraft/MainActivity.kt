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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HabitTrackerScreen()
            }
        }
    }
}

data class Habit(val id: Int, val name: String, var isCompleted: Boolean)

@Composable
fun HabitTrackerScreen() {
    var habitName by remember { mutableStateOf("") }
    var habits by remember { mutableStateOf(listOf<Habit>()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (habitName.isNotBlank()) {
                    habits = habits + Habit(habits.size, habitName, false)
                    habitName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "My Habits",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("New Habit") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(habits) { habit ->
                    HabitItem(
                        habit = habit,
                        onToggle = {
                            habits = habits.map {
                                if (it.id == habit.id) it.copy(isCompleted = !it.isCompleted) else it
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (habit.isCompleted) FontWeight.Light else FontWeight.Medium
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Toggle completion",
                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}