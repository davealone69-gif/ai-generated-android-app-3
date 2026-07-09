package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
                HabitTrackerScreen()
            }
        }
    }
}

data class Habit(val id: Int, val name: String, var completed: Boolean = false)

@Composable
fun HabitTrackerScreen() {
    var habits by remember { mutableStateOf(listOf<Habit>()) }
    var habitInput by remember { mutableStateOf("") }
    var nextId by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (habitInput.isNotBlank()) {
                    habits = habits + Habit(nextId++, habitInput)
                    habitInput = ""
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = habitInput,
                onValueChange = { habitInput = it },
                label = { Text("New Habit") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(habits) { habit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = habit.completed,
                                    onCheckedChange = { isChecked ->
                                        habits = habits.map {
                                            if (it.id == habit.id) it.copy(completed = isChecked) else it
                                        }
                                    }
                                )
                                Text(text = habit.name)
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
}