package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Habit(
    val id: String,
    val name: String,
    val streak: Int,
    val isCompletedToday: Boolean
)

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
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit("1", "Drink 8 glasses of water", 5, true),
                Habit("2", "Read 15 pages of a book", 12, false),
                Habit("3", "30-minute workout", 3, false),
                Habit("4", "Meditate 10 mins", 0, false)
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }

    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Habit Tracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { 
                Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) { 
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedCount of $totalCount habits completed (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = "Your Habits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits tracked yet! Tap + to create one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitRow(
                            habit = habit,
                            onToggleComplete = {
                                habits = habits.map {
                                    if (it.id == habit.id) {
                                        val nextCompleted = !it.isCompletedToday
                                        val nextStreak = if (nextCompleted) it.streak + 1 else maxOf(0, it.streak - 1)
                                        it.copy(isCompletedToday = nextCompleted, streak = nextStreak)
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

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    newHabitName = ""
                },
                title = { Text("New Habit") },
                text = {
                    Column {
                        Text("What habit do you want to track daily?")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newHabitName,
                            onValueChange = { newHabitName = it },
                            placeholder = { Text("e.g., Read 15 pages") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newHabitName.isNotBlank()) {
                                habits = habits + Habit(
                                    id = System.currentTimeMillis().toString(),
                                    name = newHabitName.trim(),
                                    streak = 0,
                                    isCompletedToday = false
                                )
                                showAddDialog = false
                                newHabitName = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            newHabitName = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HabitRow(
    habit: Habit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        )
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
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (habit.isCompletedToday) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onToggleComplete() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (habit.isCompletedToday) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (habit.isCompletedToday) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = "Streak: ${habit.streak} days 🔥",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Habit",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}