package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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

data class Habit(val id: Int, val name: String, var completed: Boolean = false)

@Composable
fun HabitTrackerScreen() {
    var habits by remember { mutableStateOf(listOf(
        Habit(1, "Morning Meditation"),
        Habit(2, "Drink 2L Water"),
        Habit(3, "Read 30 Minutes")
    )) }
    var newHabitName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Daily Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                label = { Text("New Habit") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newHabitName.isNotBlank()) {
                        habits = habits + Habit(habits.size + 1, newHabitName)
                        newHabitName = ""
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(habits) { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (habit.completed) MaterialTheme.colorScheme.primaryContainer 
                                         else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = habit.name, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = {
                            habits = habits.map {
                                if (it.id == habit.id) it.copy(completed = !it.completed) else it
                            }
                        }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Toggle",
                                tint = if (habit.completed) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}