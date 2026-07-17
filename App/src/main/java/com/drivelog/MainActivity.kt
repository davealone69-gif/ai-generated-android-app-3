package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val emoji: String,
    val streak: Int = 0,
    val isCompletedToday: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 8 glasses of water", category = "Health", emoji = "💧", streak = 5, isCompletedToday = true),
                Habit(name = "Read for 15 minutes", category = "Mind", emoji = "📚", streak = 12, isCompletedToday = false),
                Habit(name = "10 Minute Meditation", category = "Mind", emoji = "🧘", streak = 3, isCompletedToday = true),
                Habit(name = "Daily Planning", category = "Productivity", emoji = "📅", streak = 0, isCompletedToday = false),
                Habit(name = "Save $5 today", category = "Finance", emoji = "💰", streak = 18, isCompletedToday = true)
            )
        )
    }

    var showAddHabitForm by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val categories = listOf(
        CategoryItem("Health", "💧", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        CategoryItem("Mind", "📚", Color(0xFFE3F2FD), Color(0xFF1565C0)),
        CategoryItem("Productivity", "📅", Color(0xFFFFF3E0), Color(0xFFE65100)),
        CategoryItem("Finance", "💰", Color(0xFFFBE9E7), Color(0xFFD84315)),
        CategoryItem("Fitness", "🏃", Color(0xFFF3E5F5), Color(0xFF6A1B9A))
    )

    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
            .padding(16.dp)
    ) {
        // App Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HabitSpark",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "Build a better version of you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6F777D)
                )
            }
            Text(
                text = "✨",
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Progress",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalCount > 0) "${(progress * 100).toInt()}% Done" else "Add habits to begin!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "$completedCount/$totalCount",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = Color(0xFF4ADE80),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (progress == 1f && totalCount > 0) "🎉 Outstanding! You cleared today!" else "Keep going, consistency is key!",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle New Habit Creator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddHabitForm = !showAddHabitForm }
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Icon",
                            tint = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Custom Habit",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Icon(
                        imageVector = if (showAddHabitForm) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand/Collapse",
                        tint = Color(0xFF2563EB)
                    )
                }

                AnimatedVisibility(visible = showAddHabitForm) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = newHabitName,
                            onValueChange = { newHabitName = it },
                            placeholder = { Text("E.g., Read 10 pages") },
                            label = { Text("Habit Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select Category",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            categories.forEachIndexed { index, category ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .background(
                                            color = if (selectedCategoryIndex == index) category.selectedBgColor else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedCategoryIndex = index }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = category.emoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = category.name,
                                            fontSize = 10.sp,
                                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedCategoryIndex == index) category.textColor else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (newHabitName.isNotBlank()) {
                                    val newHabit = Habit(
                                        name = newHabitName.trim(),
                                        category = categories[selectedCategoryIndex].name,
                                        emoji = categories[selectedCategoryIndex].emoji,
                                        streak = 0,
                                        isCompletedToday = false
                                    )
                                    habits = habits + newHabit
                                    newHabitName = ""
                                    showAddHabitForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Create Habit", color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Habit list section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Routine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            TextButton(
                onClick = {
                    habits = habits.map { it.copy(isCompletedToday = false) }
                }
            ) {
                Text(
                    text = "Reset Progress",
                    color = Color(0xFFDC2626),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Habits List
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "☕",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No habits tracked yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Add one above to kickstart your day!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitRow(
                        habit = habit,
                        onToggleComplete = {
                            habits = habits.map { currentHabit ->
                                if (currentHabit.id == habit.id) {
                                    val nextCompletedState = !currentHabit.isCompletedToday
                                    val calculatedStreak = if (nextCompletedState) {
                                        currentHabit.streak + 1
                                    } else {
                                        if (currentHabit.streak > 0) currentHabit.streak - 1 else 0
                                    }
                                    currentHabit.copy(
                                        isCompletedToday = nextCompletedState,
                                        streak = calculatedStreak
                                    )
                                } else {
                                    currentHabit
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

data class CategoryItem(
    val name: String,
    val emoji: String,
    val selectedBgColor: Color,
    val textColor: Color
)

@Composable
fun HabitRow(
    habit: Habit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) Color(0xFFECFDF5) else Color.White
        ),
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (habit.isCompletedToday) Color(0xFFA7F3D0) else Color(0xFFF1F5F9),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = habit.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (habit.isCompletedToday) Color(0xFF065F46) else Color(0xFF1E293B),
                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Label
                        Text(
                            text = habit.category,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        // Streak Indicator
                        if (habit.streak > 0) {
                            Text(
                                text = "🔥 ${habit.streak} day${if (habit.streak > 1) "s" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        } else {
                            Text(
                                text = "✨ New!",
                                fontSize = 11.sp,
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Right side Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = Color(0xFFFDA4AF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Checkbox Toggle Button
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (habit.isCompletedToday) Color(0xFF10B981) else Color(0xFFE2E8F0),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark Habit Complete",
                        tint = if (habit.isCompletedToday) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}