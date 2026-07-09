package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var nextId by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Daily Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("New Habit") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (habitName.isNotBlank()) {
                        habits.add(Habit(nextId++, habitName, false))
                        habitName = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.surfaceVariant
        )
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
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = if (habit.isCompleted) Color(0xFF2E7D32) else Color.Gray
                )
            }
        }
    }
}