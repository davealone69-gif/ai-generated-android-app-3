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

@Composable
fun HabitTrackerScreen() {
    var habits by remember { mutableStateOf(listOf(
        Habit(1, "Drink Water", false),
        Habit(2, "Morning Exercise", false),
        Habit(3, "Read 10 pages", false)
    )) }
    var newHabitText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text(
            text = "Daily Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedTextField(
                value = newHabitText,
                onValueChange = { newHabitText = it },
                label = { Text("New Habit") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newHabitText.isNotBlank()) {
                        habits = habits + Habit(habits.size + 1, newHabitText, false)
                        newHabitText = ""
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(habits) { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = habit.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = {
                            habits = habits.map {
                                if (it.id == habit.id) it.copy(isCompleted = !it.isCompleted) else it
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = if (habit.isCompleted) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}