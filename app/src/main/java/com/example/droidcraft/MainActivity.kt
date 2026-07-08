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
import androidx.compose.material.icons.filled.Delete
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
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HabitTrackerScreen()
                }
            }
        }
    }
}

data class Habit(val id: Int, val name: String, var isCompleted: Boolean = false)

@Composable
fun HabitTrackerScreen() {
    var habitText by remember { mutableStateOf("") }
    var habitList by remember { mutableStateOf(listOf<Habit>()) }
    var nextId by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Habits",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = habitText,
                onValueChange = { habitText = it },
                label = { Text("New Habit") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (habitText.isNotBlank()) {
                        habitList = habitList + Habit(nextId++, habitText)
                        habitText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(habitList) { habit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row {
                            IconButton(onClick = {
                                habitList = habitList.map {
                                    if (it.id == habit.id) it.copy(isCompleted = !it.isCompleted) else it
                                }
                            }) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Complete",
                                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                            IconButton(onClick = {
                                habitList = habitList.filter { it.id != habit.id }
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