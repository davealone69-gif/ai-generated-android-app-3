package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF8BC34A),
                    background = Color(0xFFF4F6F4),
                    surface = Color.White
                )
            ) {
                HabitTrackerScreen()
            }
        }
    }
}

data class Habit(
    val id: Long,
    val name: String,
    val category: String,
    val streak: Int,
    val isCompleted: Boolean,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(1, "Drink 8 glasses of water", "Health", 5, true, "💧"),
                Habit(2, "Read for 20 minutes", "Mind", 12, false, "📚"),
                Habit(3, "30 mins Daily Workout", "Fitness", 3, true, "💪"),
                Habit(4, "Morning Meditation", "Mind", 0, false, "🧘")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }
    
    val categories = listOf(
        "Health" to "💧", 
        "Mind" to "🧠", 
        "Fitness" to "💪", 
        "Productivity" to "⚡", 
        "Routine" to "⏰"
    )

    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompleted }
    val completionProgress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HabitFlow",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Stats / Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Today's Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedHabits of $totalHabits completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(70.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { completionProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 8.dp,
                            trackColor = Color(0xFFE8F5E9),
                        )
                        Text(
                            text = "${(completionProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Text(
                text = "My Habits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No habits created yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Text(
                            "Tap + to start built-in routines!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        val cardBgColor by animateColorAsState(
                            targetValue = if (habit.isCompleted) Color(0xFFE8F5E9) else Color.White,
                            label = "cardBg"
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                            .size(48.dp)
                                            .background(Color(0xFFF1F8E9), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(habit.emoji, fontSize = 24.sp)
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null
                                            ),
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (habit.isCompleted) Color.Gray else Color.Black
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = habit.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "🔥 ${habit.streak} days",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFFF9800),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Complete checkbox/button
                                    IconButton(
                                        onClick = {
                                            habits = habits.map {
                                                if (it.id == habit.id) {
                                                    val nextCompletedState = !it.isCompleted
                                                    val newStreak = if (nextCompletedState) it.streak + 1 else maxOf(0, it.streak - 1)
                                                    it.copy(isCompleted = nextCompletedState, streak = newStreak)
                                                } else it
                                            }
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = if (habit.isCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Complete",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = {
                                            habits = habits.filter { it.id != habit.id }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF5350)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    "Create New Habit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Habit Name") },
                        placeholder = { Text("e.g., Read 10 pages") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text(
                            "Select Category",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { (category, emoji) ->
                                val isSelected = selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFC8E6C9) else Color(0xFFEEEEEE))
                                        .clickable { selectedCategory = category }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(emoji, fontSize = 18.sp)
                                        Text(
                                            category, 
                                            fontSize = 10.sp, 
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            val matchingCategory = categories.firstOrNull { it.first == selectedCategory }
                            val emoji = matchingCategory?.second ?: "⭐"
                            val newHabit = Habit(
                                id = System.currentTimeMillis(),
                                name = newHabitName,
                                category = selectedCategory,
                                streak = 0,
                                isCompleted = false,
                                emoji = emoji
                            )
                            habits = habits + newHabit
                            newHabitName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Add Habit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}