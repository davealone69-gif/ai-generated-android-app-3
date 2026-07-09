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

data class Habit(val id: Int, val name: String, var isCompleted: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp() {
    var habitList by remember { mutableStateOf(listOf<Habit>()) }
    var habitInput by remember { mutableStateOf("") }
    var nextId by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Daily Habits") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (habitInput.isNotBlank()) {
                    habitList = habitList + Habit(nextId++, habitInput, false)
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
            OutlinedTextField(
                value = habitInput,
                onValueChange = { habitInput = it },
                label = { Text("New Habit") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(habitList) { habit ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
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
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                habitList = habitList.map {
                                    if (it.id == habit.id) it.copy(isCompleted = !it.isCompleted) else it
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Toggle completion",
                                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}