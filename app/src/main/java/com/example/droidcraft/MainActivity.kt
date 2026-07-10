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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data class for a habit
data class Habit(val id: Int, val name: String, var completedToday: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    val habits: SnapshotStateList<Habit> = remember { mutableStateListOf() }
    var newHabitName by remember { mutableStateOf("") }
    var nextHabitId by remember { mutableStateOf(0) } // Simple ID generator

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Simple Habit Tracker",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Section for adding new habits
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Add New Habit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = newHabitName,
                                onValueChange = { newHabitName = it },
                                label = { Text("Habit Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newHabitName.isNotBlank()) {
                                        habits.add(Habit(nextHabitId++, newHabitName.trim(), false))
                                        newHabitName = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Habit")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of habits
                if (habits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No habits yet! Add your first habit above.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Checkbox(
                                        checked = habit.completedToday,
                                        onCheckedChange = { isChecked ->
                                            val index = habits.indexOfFirst { it.id == habit.id }
                                            if (index != -1) {
                                                habits[index] = habits[index].copy(completedToday = isChecked)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset Daily Habits Button
                Button(
                    onClick = {
                        habits.forEachIndexed { index, habit ->
                            habits[index] = habit.copy(completedToday = false)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Reset Daily Habits", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    )
}