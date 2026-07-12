package com.davealone69.everything4droid

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class Habit(
    val id: Long,
    val name: String,
    val description: String,
    val streak: Int,
    val isCompleted: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF81C784),
                    background = Color(0xFFF1F8E9),
                    surface = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(1, "Drink Water", "8 glasses a day", 5, true),
                Habit(2, "Morning Run", "30-minute outdoor jog", 12, false),
                Habit(3, "Read Book", "At least 15 pages", 3, false),
                Habit(4, "Meditate", "10-minute breathing exercise", 0, false)
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitDesc by remember { mutableStateOf("") }

    val completedCount = habits.count { it.isCompleted }
    val totalCount = habits.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HabitFlow",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedCount of $totalCount habits completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    )
                }
            }

            // Habit List
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits added yet. Tap + to begin!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = {
                                habits = habits.map {
                                    if (it.id == habit.id) {
                                        val newCompleted = !it.isCompleted
                                        val newStreak = if (newCompleted) it.streak + 1 else maxOf(0, it.streak - 1)
                                        it.copy(isCompleted = newCompleted, streak = newStreak)
                                    } else {
                                        it
                                    }
                                }
                            },
                            onDelete = {
                                habits = habits.filter { it.id != habit.id }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Create New Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Habit Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newHabitDesc,
                        onValueChange = { newHabitDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            showAddDialog = false
                            newHabitName = ""
                            newHabitDesc = ""
                        }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newHabitName.isNotBlank()) {
                                    val newHabit = Habit(
                                        id = System.currentTimeMillis(),
                                        name = newHabitName,
                                        description = newHabitDesc,
                                        streak = 0,
                                        isCompleted = false
                                    )
                                    habits = habits + newHabit
                                    showAddDialog = false
                                    newHabitName = ""
                                    newHabitDesc = ""
                                }
                            },
                            enabled = newHabitName.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (habit.isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Habit",
                        tint = if (habit.isCompleted) Color.White else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (habit.description.isNotEmpty()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔥 Streak: ${habit.streak} days",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Habit",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}