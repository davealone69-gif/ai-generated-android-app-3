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
import androidx.compose.ui.unit.dp

data class Habit(val id: Int, val name: String, var isCompleted: Boolean)

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

@Composable
fun HabitTrackerScreen() {
    var habitName by remember { mutableStateOf("") }
    val habits = remember { mutableStateListOf<Habit>() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (habitName.isNotBlank()) {
                    habits.add(Habit(habits.size, habitName, false))
                    habitName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "My Habits",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Enter a new habit") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(habits) { habit ->
                    HabitItem(habit = habit, onToggle = {
                        val index = habits.indexOf(habit)
                        habits[index] = habit.copy(isCompleted = !habit.isCompleted)
                    })
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = habit.name, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Toggle completion",
                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}