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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4F46E5),
                    onPrimary = Color.White,
                    secondary = Color(0xFF0EA5E9),
                    background = Color(0xFFF8FAFC),
                    surface = Color.White,
                    error = Color(0xFFEF4444)
                )
            ) {
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

enum class HabitCategory(val displayName: String, val icon: String, val color: Color) {
    HEALTH("Health", "💧", Color(0xFF10B981)),
    MIND("Mind", "📚", Color(0xFF8B5CF6)),
    WORK("Work", "💻", Color(0xFF3B82F6)),
    FITNESS("Fitness", "🏋️‍♂️", Color(0xFFF59E0B)),
    ROUTINE("Routine", "✨", Color(0xFFEC4899))
}

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: HabitCategory,
    val isCompleted: Boolean = false,
    val streak: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 8 glasses of water", category = HabitCategory.HEALTH, isCompleted = true, streak = 5),
                Habit(name = "Read 15 pages of a book", category = HabitCategory.MIND, isCompleted = false, streak = 2),
                Habit(name = "Code 1 hour daily", category = HabitCategory.WORK, isCompleted = true, streak = 14),
                Habit(name = "30 mins morning workout", category = HabitCategory.FITNESS, isCompleted = false, streak = 0),
                Habit(name = "Write active gratitude journal", category = HabitCategory.ROUTINE, isCompleted = false, streak = 1)
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddForm by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.HEALTH) }
    
    val focusManager = LocalFocusManager.current

    // Statistics calculations
    val totalCount = habits.size
    val completedCount = habits.count { it.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val progressPercentage = (progressFraction * 100).toInt()

    // Filter logic
    val filteredHabits = if (selectedFilter == "All") {
        habits
    } else {
        habits.filter { it.category.displayName == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HabitBuilder",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Track your routines, achieve your goals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Progress",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$completedCount/$totalCount Completed",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        progressPercentage == 100 -> "Perfect score! Outstanding work today! 🎉"
                        progressPercentage >= 50 -> "More than halfway there! Keep pushing! 🔥"
                        progressPercentage > 0 -> "Good start! Keep ticking those boxes! 💪"
                        else -> "Start your day by checking off your first habit! 🚀"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Expandable Add Habit Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (showAddForm) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddForm = !showAddForm },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Create Custom Habit",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = if (showAddForm) "Collapse ▲" else "Expand ▼",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = showAddForm) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = newHabitName,
                            onValueChange = { newHabitName = it },
                            label = { Text("What is your daily goal?") },
                            placeholder = { Text("e.g. Read for 20 minutes") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select Category:",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category choice list
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HabitCategory.values().forEach { category ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedCategory == category) category.color
                                            else category.color.copy(alpha = 0.15f)
                                        )
                                        .clickable { selectedCategory = category }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = category.icon, fontSize = 18.sp)
                                        Text(
                                            text = category.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedCategory == category) Color.white else Color.Black
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
                                        category = selectedCategory,
                                        isCompleted = false,
                                        streak = 0
                                    )
                                    habits = habits + newHabit
                                    newHabitName = ""
                                    showAddForm = false
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = newHabitName.isNotBlank()
                        ) {
                            Text("Save Habit Strategy")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Categories Quick Filters
        Text(
            text = "Your Checklist Today",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf("All") + HabitCategory.values().map { it.displayName }
            filterOptions.take(4).forEach { option ->
                val isSelected = selectedFilter == option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                        .clickable { selectedFilter = option }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.white else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Habit list
        if (filteredHabits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No habits match this category yet.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredHabits, key = { it.id }) { habit ->
                    HabitItem(
                        habit = habit,
                        onToggleComplete = {
                            habits = habits.map { currentHabit ->
                                if (currentHabit.id == habit.id) {
                                    val isComp = !currentHabit.isCompleted
                                    val streakModifier = if (isComp) 1 else -1
                                    val nextStreak = (currentHabit.streak + streakModifier).coerceAtLeast(0)
                                    currentHabit.copy(isCompleted = isComp, streak = nextStreak)
                                } else {
                                    currentHabit
                                }
                            }
                        },
                        onDelete = {
                            habits = habits.filterNot { it.id == habit.id }
                        }
                    )
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) Color(0xFFF1F5F9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Block
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(habit.category.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.category.icon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (habit.isCompleted) Color.Gray else Color.Black,
                        textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = habit.category.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = habit.category.color
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "🔥 ${habit.streak} Day Streak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF97316)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Toggle Checkbox Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (habit.isCompleted) Color(0xFF10B981)
                            else Color.LightGray.copy(alpha = 0.3f)
                        )
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Checklist",
                        tint = if (habit.isCompleted) Color.White else Color.Transparent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete Action
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Strategy",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}