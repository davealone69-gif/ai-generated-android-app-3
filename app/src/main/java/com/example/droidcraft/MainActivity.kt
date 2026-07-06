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

data class Habit(val id: Int, val name: String, val isCompleted: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp() {
    var habitText by remember { mutableStateOf("") }
    val habits = remember { mutableStateListOf<Habit>() }
    var nextId by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Daily Habit Tracker") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (habitText.isNotBlank()) {
                    habits.add(Habit(nextId++, habitText, false))
                    habitText = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = habitText,
                onValueChange = { habitText = it },
                label = { Text("Enter a new habit") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(habits) { habit ->
                    HabitItem(
                        habit = habit,
                        onToggle = {
                            val index = habits.indexOf(habit)
                            habits[index] = habit.copy(isCompleted = !habit.isCompleted)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (habit.isCompleted) FontWeight.Bold else FontWeight.Normal
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