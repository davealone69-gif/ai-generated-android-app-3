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
import androidx.compose.material.icons.filled.*
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
    val streak: Int = 0,
    val isCompletedToday: Boolean = false,
    val iconEmoji: String = "⭐"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 3L Water", category = "Health", streak = 5, isCompletedToday = true, iconEmoji = "💧"),
                Habit(name = "Read 15 Pages", category = "Mind", streak = 3, isCompletedToday = false, iconEmoji = "📚"),
                Habit(name = "Morning Meditation", category = "Mind", streak = 12, isCompletedToday = true, iconEmoji = "🧘"),
                Habit(name = "30 Min Workout", category = "Fitness", streak = 0, isCompletedToday = false, iconEmoji = "🏋️‍♂️"),
                Habit(name = "Code 1 Hour", category = "Work", streak = 8, isCompletedToday = false, iconEmoji = "💻")
            )
        )
    }

    var selectedTab by remember { mutableStateOf("All") }
    var showAddForm by remember { mutableStateOf(false) }

    // New habit state
    var newHabitName by remember { mutableStateOf("") }
    var newHabitCategory by remember { mutableStateOf("Health") }
    var newHabitEmoji by remember { mutableStateOf("💧") }

    val categories = listOf("Health", "Mind", "Fitness", "Work", "Other")
    val emojis = listOf("💧", "📚", "🧘", "🏋️‍♂️", "💻", "🍎", "🚶", "✍️", "🧹", "⭐")

    // Stats
    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompletedToday }
    val progressPercent = if (totalHabits > 0) (completedHabits.toFloat() / totalHabits.toFloat()) else 0f

    val filteredHabits = when (selectedTab) {
        "Pending" -> habits.filter { !it.isCompletedToday }
        "Completed" -> habits.filter { it.isCompletedToday }
        else -> habits
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFC))
    ) {
        // App Bar / Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HabitFlow",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Keep up the momentum!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // New Day Reset / Quick Action
                IconButton(
                    onClick = {
                        habits = habits.map {
                            it.copy(isCompletedToday = false)
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Progress for Demo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Main content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$completedHabits of $totalHabits",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = when {
                                progressPercent == 1.0f -> "🎉 Outstanding! All habits completed today!"
                                progressPercent >= 0.5f -> "🚀 Great job! You are past the halfway mark!"
                                progressPercent > 0.0f -> "🌱 Good start! Take it one step at a time."
                                else -> "💤 Let's get started on your daily routine today!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tabs / Filters and "Add Habit" action button row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom chips for tabs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Pending", "Completed").forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Add Habit Toggle Button
                    Button(
                        onClick = { showAddForm = !showAddForm },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showAddForm) Color.Red.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Toggle Add",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddForm) "Close" else "New",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Expanding Add Form
            item {
                AnimatedVisibility(visible = showAddForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Create Custom Habit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newHabitName,
                                onValueChange = { newHabitName = it },
                                label = { Text("Habit Name") },
                                placeholder = { Text("e.g., Read books, Drink water") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { category ->
                                    val isSelected = newHabitCategory == category
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                                else Color(0xFFF1F3F5)
                                            )
                                            .clickable { newHabitCategory = category }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.DarkGray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Emoji / Icon", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                emojis.forEach { emoji ->
                                    val isSelected = newHabitEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Transparent
                                            )
                                            .clickable { newHabitEmoji = emoji }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 18.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (newHabitName.isNotBlank()) {
                                        val newHabit = Habit(
                                            name = newHabitName,
                                            category = newHabitCategory,
                                            iconEmoji = newHabitEmoji
                                        )
                                        habits = habits + newHabit
                                        newHabitName = ""
                                        showAddForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Add Habit to Tracker", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Habit Items List
            if (filteredHabits.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📭 No habits in this section.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add a new habit to start tracking!",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(filteredHabits) { habit ->
                    HabitCardItem(
                        habit = habit,
                        onToggle = {
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

@Composable
fun HabitCardItem(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val (catBgColor, catTextColor) = getCategoryColorPalette(habit.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon/Emoji Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(catBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.iconEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Habit Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (habit.isCompletedToday) Color.Gray else Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(catBgColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = habit.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = catTextColor
                        )
                    }

                    // Streak Display
                    if (habit.streak > 0) {
                        Text(
                            text = "🔥 ${habit.streak} Day Streak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF6D00)
                        )
                    } else {
                        Text(
                            text = "🌱 Get started",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Quick Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Habit",
                    tint = Color.LightGray.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Complete/Incomplete Toggle Circle
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (habit.isCompletedToday) MaterialTheme.colorScheme.primary
                        else Color(0xFFECEFF1)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Toggle Complete",
                    tint = if (habit.isCompletedToday) Color.White else Color(0xFF78909C),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getCategoryColorPalette(category: String): Pair<Color, Color> {
    return when (category) {
        "Health" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        "Mind" -> Pair(Color(0xFFEDE7F6), Color(0xFF6A1B9A))
        "Fitness" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "Work" -> Pair(Color(0xFFFFF3E0), Color(0xFFEF6C00))
        else -> Pair(Color(0xFFECEFF1), Color(0xFF37474F))
    }
}