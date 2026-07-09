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
import androidx.compose.material.icons.filled.Delete
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
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HabitTrackerApp()
                }
            }
        }
    }
}

data class Habit(val id: Int, val name: String, var isCompleted: Boolean = false)

@Composable
fun HabitTrackerApp() {
    var habits by remember { mutableStateOf(listOf(
        Habit(1, "Morning Meditation"),
        Habit(2, "Drink 2L Water"),
        Habit(3, "Read 20 pages")
    )) }
    var newHabitName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "My Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                modifier = Modifier.weight(1f),
                label = { Text("New Habit") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (newHabitName.isNotBlank()) {
                    habits = habits + Habit(habits.size + 1, newHabitName)
                    newHabitName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(habits) { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (habit.isCompleted) MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = habit.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            habits = habits.map { if (it.id == habit.id) it.copy(isCompleted = !it.isCompleted) else it }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Toggle")
                        }
                        IconButton(onClick = {
                            habits = habits.filter { it.id != habit.id }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}