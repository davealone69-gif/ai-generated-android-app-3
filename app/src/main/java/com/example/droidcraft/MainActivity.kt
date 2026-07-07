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
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Habit(val id: Int, val name: String, val isDone: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Using the M3 MaterialTheme which reads from the Android theme resources automatically
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerScreen()
                }
            }
        }
    }
}

@Composable
fun HabitTrackerScreen() {
    var habits by remember { mutableStateOf(listOf(
        Habit(1, "Morning Meditation"),
        Habit(2, "Drink 2L Water"),
        Habit(3, "Read 10 pages")
    )) }
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "My Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                label = { Text("New Habit") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        habits = habits + Habit(habits.size + 1, text)
                        text = ""
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(habit.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            habits = habits.map { 
                                if (it.id == habit.id) it.copy(isDone = !it.isDone) else it 
                            }
                        }) {
                            Icon(
                                if (habit.isDone) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = "Toggle Habit",
                                tint = if (habit.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}