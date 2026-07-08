package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

data class Habit(val id: Int, val name: String, var isCompleted: Boolean = false)

@Composable
fun HabitTrackerScreen() {
    var habits by remember { mutableStateOf(listOf(Habit(1, "Morning Meditation"), Habit(2, "Drink Water"), Habit(3, "Exercise"))) }
    var newHabitText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "My Habits",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedTextField(
                value = newHabitText,
                onValueChange = { newHabitText = it },
                label = { Text("New Habit") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newHabitText.isNotBlank()) {
                        habits = habits + Habit(habits.size + 1, newHabitText)
                        newHabitText = ""
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        LazyColumn {
            items(habits) { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (habit.isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = habit.name, style = MaterialTheme.typography.bodyLarge)
                        Checkbox(
                            checked = habit.isCompleted,
                            onCheckedChange = { checked ->
                                habits = habits.map {
                                    if (it.id == habit.id) it.copy(isCompleted = checked) else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}