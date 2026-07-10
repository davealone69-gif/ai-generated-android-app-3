package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerAppScreen()
        }
    }
}

data class Habit(val id: Int, var name: String, var completed: Boolean = false)

@Composable
fun HabitTrackerAppScreen() {
    val habits: SnapshotStateList<Habit> = remember { mutableStateListOf() }
    var newHabitName by remember { mutableStateOf("") }
    var nextHabitId by remember { mutableStateOf(0) }

    // Optional: Add some initial dummy habits for demonstration
    LaunchedEffect(Unit) {
        if (habits.isEmpty()) {
            habits.add(Habit(nextHabitId++, "Drink 8 glasses of water"))
            habits.add(Habit(nextHabitId++, "Meditate for 10 minutes"))
            habits.add(Habit(nextHabitId++, "Read 20 pages"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (newHabitName.isNotBlank()) {
                    habits.add(Habit(nextHabitId++, newHabitName.trim()))
                    newHabitName = ""
                }
            }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Habit Input Section
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                label = { Text("New Habit Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Habits List Section
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits added yet!\nTap '+' to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(habit = habit) {
                            val index = habits.indexOf(habit)
                            if (index != -1) {
                                habits[index] = habit.copy(completed = !habit.completed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItem(habit: Habit, onToggleCompletion: (Habit) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleCompletion(habit) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (habit.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = habit.completed,
                onCheckedChange = { onToggleCompletion(habit) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}