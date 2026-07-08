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

data class Habit(val id: Int, val name: String, var isCompleted: Boolean = false)

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
    var habitName by remember { mutableStateOf("") }
    var habits by remember { mutableStateOf(listOf<Habit>()) }
    var nextId by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Habit Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Enter a new habit") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (habitName.isNotBlank()) {
                        habits = habits + Habit(nextId++, habitName)
                        habitName = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}