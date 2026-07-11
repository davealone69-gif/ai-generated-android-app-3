package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

// Data model representing a Habit
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val isCompletedToday: Boolean = false,
    val streak: Int = 0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerApp()
        }
    }
}

@Composable
fun HabitTrackerApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6366F1), // Modern Indigo
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEEF2FF),
            onPrimaryContainer = Color(0xFF312E81),
            secondary = Color(0xFF10B981), // Fresh Emerald
            onSecondary = Color.White,
            background = Color(0xFFF9FAFB),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // Standard initial sample habits
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 3L of Water", category = "Health", isCompletedToday = false, streak = 4),
                Habit(name = "30-min Morning Run", category = "Fitness", isCompletedToday = true, streak = 12),
                Habit(name = "Read 10 Pages of a Book", category = "Learning", isCompletedToday = false, streak = 7),
                Habit(name = "Mindfulness Meditation", category = "Mind", isCompletedToday = true, streak = 2)
            )
        )
    }

    var isAddingHabit by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HabitSphere",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingHabit = !isAddingHabit },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = if (isAddingHabit) Icons.Default.KeyboardArrowDown else Icons.Default.Add,
                    contentDescription = "Add Habit"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Overall Daily Progress Section
            val completedCount = habits.count { it.isCompletedToday }
            val totalCount = habits.size
            val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (totalCount > 0) "$completedCount of $totalCount completed" else "Add your first habit below!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
            }

            // Expandable Habit Creator Form
            AnimatedVisibility(
                visible = isAddingHabit,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                AddHabitForm(
                    onHabitCreated = { name, category ->
                        habits = habits + Habit(name = name, category = category)
                        isAddingHabit = false
                    },
                    onDismiss = { isAddingHabit = false }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Habit List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Habits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = "Tap to Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "✨ No habits defined yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Press the + button to build a healthy routine!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(habits) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = {
                                habits = habits.map {
                                    if (it.id == habit.id) {
                                        val nextCompleted = !it.isCompletedToday
                                        val nextStreak = if (nextCompleted) it.streak + 1 else maxOf(0, it.streak - 1)
                                        it.copy(isCompletedToday = nextCompleted, streak = nextStreak)
                                    } else it
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
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryStyle = getCategoryStyle(habit.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(categoryStyle.bgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = categoryStyle.emoji,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (habit.isCompletedToday) Color.Gray else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
                            text = "🔥 ${habit.streak} day streak",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (habit.streak > 0) Color(0xFFF59E0B) else Color.Gray
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Done toggle button
                IconButton(
                    onClick = onToggleComplete,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.secondary else Color(0xFFF3F4F6),
                        contentColor = if (habit.isCompletedToday) Color.White else Color.Gray
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Habit",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddHabitForm(
    onHabitCreated: (name: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var habitName by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Health") }
    val categories = listOf("Health", "Fitness", "Learning", "Mind", "Work")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "✨ Form a New Habit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("What is your goal?") },
                placeholder = { Text("e.g. Read for 20 mins") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        val style = getCategoryStyle(category)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6))
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = style.emoji, fontSize = 16.sp)
                                Text(
                                    text = category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.DarkGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            onHabitCreated(habitName.trim(), selectedCategory)
                            habitName = ""
                        }
                    },
                    enabled = habitName.isNotBlank()
                ) {
                    Text("Add Habit")
                }
            }
        }
    }
}

// Helpers for Categorization UI Details
data class CategoryStyle(val emoji: String, val bgColor: Color)

fun getCategoryStyle(category: String): CategoryStyle {
    return when (category) {
        "Health" -> CategoryStyle("🍏", Color(0xFFE8F5E9))
        "Fitness" -> CategoryStyle("💪", Color(0xFFFFF3E0))
        "Learning" -> CategoryStyle("📚", Color(0xFFE3F2FD))
        "Mind" -> CategoryStyle("🧘", Color(0xFFF3E5F5))
        "Work" -> CategoryStyle("💼", Color(0xFFECEFF1))
        else -> CategoryStyle("⭐", Color(0xFFFFFFFF))
    }
}