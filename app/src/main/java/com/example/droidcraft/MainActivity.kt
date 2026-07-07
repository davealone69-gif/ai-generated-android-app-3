package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
            // Apply the theme defined in themes.xml via the MaterialTheme wrapper
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerScreen()
                }
            }
        }
    }
}

data class Habit(val id: Int, val name: String, val isCompleted: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habitName by remember { mutableStateOf("") }
    val habits = remember { mutableStateListOf<Habit>() }
    var nextId by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Habits") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
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
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(habits) { habit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
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
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = {
                                val index = habits.indexOf(habit)
                                if (index != -1) {
                                    habits[index] = habit.copy(isCompleted = !habit.isCompleted)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Toggle completion",
                                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}