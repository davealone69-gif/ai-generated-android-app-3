package com.drivelog

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6200EE),
                    secondary = Color(0xFF03DAC6),
                    tertiary = Color(0xFF3700B3),
                    background = Color(0xFFF6F8FA),
                    surface = Color(0xFFFFFFFF)
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

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val streak: Int,
    val isCompletedToday: Boolean,
    val category: String,
    val emoji: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HabitTrackerApp() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 8 Cups of Water", streak = 5, isCompletedToday = true, category = "Health", emoji = "💧"),
                Habit(name = "Read 20 Pages", streak = 12, isCompletedToday = false, category = "Mind", emoji = "📚"),
                Habit(name = "Morning Workout", streak = 0, isCompletedToday = false, category = "Fitness", emoji = "🏋️"),
                Habit(name = "8 Hours of Sleep", streak = 3, isCompletedToday = true, category = "Health", emoji = "😴"),
                Habit(name = "Meditation", streak = 1, isCompletedToday = false, category = "Mind", emoji = "🧘")
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Statistics calculations
    val totalHabits = habits.size
    val completedToday = habits.count { it.isCompletedToday }
    val completionProgress = if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f
    val highestStreak = if (habits.isNotEmpty()) habits.maxOf { it.streak } else 0

    // Filtered list
    val filteredHabits = when (selectedFilter) {
        "Completed" -> habits.filter { it.isCompletedToday }
        "Active" -> habits.filter { !it.isCompletedToday }
        else -> habits
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Leave space for Floating Action Bar / Bottom Section
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp)
            ) {
                Text(
                    text = "HabitFlow",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Track your daily consistency",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Dashboard Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "$completedToday / $totalHabits Done",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Indicator
                        LinearProgressIndicator(
                            progress = completionProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "🔥 Best Streak",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$highestStreak Days",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            // Simulated Next Day button (great for demoing / testing state changes!)
                            Button(
                                onClick = {
                                    // Process "Next Day Simulation"
                                    // If habit is completed, increment streak and reset completion
                                    // If not completed, reset streak and reset completion
                                    habits = habits.map {
                                        val newStreak = if (it.isCompletedToday) it.streak + 1 else 0
                                        it.copy(streak = newStreak, isCompletedToday = false)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Simulate Next Day ⏩",
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Filtering Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "Completed").forEach { status ->
                    val isSelected = selectedFilter == status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(
                                    alpha = 0.3f
                                )
                            )
                            .clickable { selectedFilter = status }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = status,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else Color.DarkGray
                        )
                    }
                }
            }

            // Habit List Header
            Text(
                text = "$selectedFilter Habits",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = Color.DarkGray
            )

            // Habit Cards List
            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "✨",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No habits found in this category",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        HabitItemCard(
                            habit = habit,
                            onCompleteToggle = { clickedHabit ->
                                habits = habits.map {
                                    if (it.id == clickedHabit.id) {
                                        it.copy(isCompletedToday = !it.isCompletedToday)
                                    } else {
                                        it
                                    }
                                }
                            },
                            onDelete = { deletedHabit ->
                                habits = habits.filter { it.id != deletedHabit.id }
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text(
                text = "+ Add",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    // New Habit Dialog Window
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAddHabit = { name, category, emoji ->
                val newHabit = Habit(
                    name = name,
                    category = category,
                    emoji = emoji,
                    streak = 0,
                    isCompletedToday = false
                )
                habits = habits + newHabit
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitItemCard(
    habit: Habit,
    onCompleteToggle: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    val categoryColor = when (habit.category) {
        "Fitness" -> Color(0xFFFFEBEE)
        "Mind" -> Color(0xFFF3E5F5)
        "Health" -> Color(0xFFE3F2FD)
        else -> Color(0xFFFFF8E1)
    }

    val categoryTextColor = when (habit.category) {
        "Fitness" -> Color(0xFFC62828)
        "Mind" -> Color(0xFF6A1B9A)
        "Health" -> Color(0xFF1565C0)
        else -> Color(0xFFF57F17)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                // Emoji Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = habit.emoji,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Metadata Column
                Column {
                    Text(
                        text = habit.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (habit.isCompletedToday) Color.Gray else Color.Black,
                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(categoryColor.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = habit.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryTextColor
                            )
                        }

                        // Streak Indicator
                        Text(
                            text = "🔥 ${habit.streak} day streak",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Action Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Delete Trigger
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f))
                        .clickable { onDelete(habit) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🗑️", fontSize = 14.sp)
                }

                // Custom Checkbox Trigger
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (habit.isCompletedToday) Color(0xFF4CAF50) else Color.LightGray.copy(
                                alpha = 0.5f
                            )
                        )
                        .clickable { onCompleteToggle(habit) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (habit.isCompletedToday) "✓" else "○",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (habit.isCompletedToday) Color.White else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (name: String, category: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }
    var selectedEmoji by remember { mutableStateOf("💧") }

    val categories = listOf("Health", "Mind", "Fitness", "Other")
    val emojis = listOf("💧", "📚", "🏋️", "🧘", "🥗", "🏃", "😴", "💡", "💰", "✏️")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "New Habit 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Habit Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What is your goal?") },
                    placeholder = { Text("e.g. Read physical books") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Select Emoji Label
                Text(
                    text = "Pick an Emoji Icon:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Emoji Carousel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.take(5).forEach { emo ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedEmoji == emo) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedEmoji = emo }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emo, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.drop(5).take(5).forEach { emo ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedEmoji == emo) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedEmoji = emo }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emo, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category selection Label
                Text(
                    text = "Select Category:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAddHabit(name, selectedCategory, selectedEmoji)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}