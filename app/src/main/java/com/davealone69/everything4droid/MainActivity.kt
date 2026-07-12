package com.davealone69.everything4droid

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Habit(
    val id: Long,
    val name: String,
    val category: String,
    val streak: Int,
    val isCompletedToday: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4CAF50),
                    onPrimary = Color.White,
                    secondary = Color(0xFF009688),
                    background = Color(0xFFF5F5F5),
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

@Composable
fun HabitTrackerApp() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(1L, "Drink 8 glasses of water", "Health", 3, false),
                Habit(2L, "Meditate 10 minutes", "Mind", 5, true),
                Habit(3L, "Read 15 pages", "Productivity", 12, false),
                Habit(4L, "Morning stretch", "Health", 0, false)
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Health", "Mind", "Productivity")

    val filteredHabits = if (selectedFilter == "All") {
        habits
    } else {
        habits.filter { it.category == selectedFilter }
    }

    val totalHabits = habits.size
    val completedCount = habits.count { it.isCompletedToday }
    val progress = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Habit Loop",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Track your daily micro-wins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's Progress",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedCount of $totalHabits habits completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFC8E6C9),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Filters horizontal list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedFilter == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                            .clickable { selectedFilter = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habits List
            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits found. Start by creating one!",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredHabits) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = { toggledHabit ->
                                habits = habits.map {
                                    if (it.id == toggledHabit.id) {
                                        val nextState = !it.isCompletedToday
                                        val nextStreak = if (nextState) it.streak + 1 else maxOf(0, it.streak - 1)
                                        it.copy(isCompletedToday = nextState, streak = nextStreak)
                                    } else it
                                }
                            },
                            onDelete = { habitToDelete ->
                                habits = habits.filter { it.id != habitToDelete.id }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            categories = categories.filter { it != "All" },
            onDismiss = { showAddDialog = false },
            onAddHabit = { name, category ->
                val newId = if (habits.isEmpty()) 1L else habits.maxOf { it.id } + 1
                habits = habits + Habit(
                    id = newId,
                    name = name,
                    category = category,
                    streak = 0,
                    isCompletedToday = false
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggleComplete: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    val categoryColor = when (habit.category) {
        "Health" -> Color(0xFFE3F2FD)
        "Mind" -> Color(0xFFF3E5F5)
        "Productivity" -> Color(0xFFFFF3E0)
        else -> Color(0xFFECEFF1)
    }

    val categoryTextColor = when (habit.category) {
        "Health" -> Color(0xFF1E88E5)
        "Mind" -> Color(0xFF8E24AA)
        "Productivity" -> Color(0xFFF4511E)
        else -> Color(0xFF546E7A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                // Done Circle Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (habit.isCompletedToday) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                        .clickable { onToggleComplete(habit) },
                    contentAlignment = Alignment.Center
                ) {
                    if (habit.isCompletedToday) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed Today",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = habit.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (habit.isCompletedToday) Color.Gray else Color.Black,
                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(categoryColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = habit.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryTextColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "🔥 ${habit.streak} days",
                            fontSize = 12.sp,
                            color = Color(0xFFFF5722),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick = { onDelete(habit) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Habit",
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAddHabit: (String, String) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Health") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Habit",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("What habit do you want to start?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Column {
                    Text(
                        text = "Category",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                                    .clickable { selectedCategory = category }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        onAddHabit(habitName.trim(), selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}