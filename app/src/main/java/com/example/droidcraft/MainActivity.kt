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

data class Habit(val id: Int, val name: String, var isCompleted: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habits by remember { mutableStateOf(listOf(
        Habit(1, "Morning Meditation", false),
        Habit(2, "Drink 2L Water", false),
        Habit(3, "Read 20 pages", false)
    )) }
    
    var newHabitName by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Daily Habits") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (newHabitName.isNotBlank()) {
                    habits = habits + Habit(habits.size + 1, newHabitName, false)
                    newHabitName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                label = { Text("New Habit") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = habit.name, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Toggle",
                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}