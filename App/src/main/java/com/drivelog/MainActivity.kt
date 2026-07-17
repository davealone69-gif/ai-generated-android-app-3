package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val streak: Int,
    val isCompletedToday: Boolean,
    val iconEmoji: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF2E7D32),
                    background = Color(0xFFF4F6F4),
                    surface = Color.White
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // Initial habits list simulation
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 8 glasses of water", category = "Health", streak = 5, isCompletedToday = false, iconEmoji = "💧"),
                Habit(name = "Read a book", category = "Mind", streak = 12, isCompletedToday = true, iconEmoji = "📚"),
                Habit(name = "Morning workout", category = "Health", streak = 0, isCompletedToday = false, iconEmoji = "🏋️"),
                Habit(name = "Daily meditation", category = "Mind", streak = 8, isCompletedToday = true, iconEmoji = "🧘"),
                Habit(name = "Write 100 lines of code", category = "Productivity", streak = 15, isCompletedToday = false, iconEmoji = "💻")
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var newHabitName by remember { mutableStateOf("") }
    var newHabitCategory by remember { mutableStateOf("Health") }
    var newHabitEmoji by remember { mutableStateOf("💧") }

    val categories = listOf("All", "Health", "Mind", "Productivity", "Finance", "Other")
    val formCategories = listOf("Health", "Mind", "Productivity", "Finance", "Other")
    val availableEmojis = listOf("💧", "📚", "🏋️", "🧘", "💻", "🍎", "🏃", "💰", "☕", "🧹")

    // Stats calculations
    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompletedToday }
    val progressFraction = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f
    val activeStreaks = habits.sumOf { it.streak }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HabitFlow 🌟",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Track your daily micro-habits",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    // Next day simulation button
                    TextButton(
                        onClick = {
                            // Increment completed streaks, break incomplete streaks, and reset for the new day
                            habits = habits.map {
                                it.copy(
                                    streak = if (it.isCompletedToday) it.streak + 1 else 0,
                                    isCompletedToday = false
                                )
                            }
                        }
                    ) {
                        Text("Simulate Next Day 📆", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newHabitName = ""
                    newHabitCategory = "Health"
                    newHabitEmoji = "💧"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Stats summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedHabits of $totalHabits completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🔥 $activeStreaks Total Streaks",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFFE0E0E0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (progressFraction == 1f && totalHabits > 0)
                            "🎉 Incredible! All habits completed today!"
                        else if (progressFraction > 0.5f)
                            "⚡ You are doing great! Keep it up!"
                        else
                            "💪 Small steps lead to big change. Complete a habit!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Categories horizontal filter list
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Habits list
            val filteredHabits = if (selectedFilter == "All") {
                habits
            } else {
                habits.filter { it.category.equals(selectedFilter, ignoreCase = true) }
            }

            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🕊️",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No habits found in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        val catColor = getCategoryColor(habit.category)
                        val catTextColor = getCategoryTextColor(habit.category)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                    // Emoji display
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(catColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = habit.iconEmoji, fontSize = 24.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = habit.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else null,
                                            color = if (habit.isCompletedToday) Color.Gray else Color.Unspecified
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = habit.category,
                                                fontSize = 11.sp,
                                                color = catTextColor,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(catColor, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                            Text(
                                                text = "🔥 ${habit.streak} day streak",
                                                fontSize = 12.sp,
                                                color = if (habit.streak > 0) Color(0xFFE65100) else Color.Gray,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Complete checkbox button
                                    IconButton(
                                        onClick = {
                                            habits = habits.map {
                                                if (it.id == habit.id) {
                                                    it.copy(isCompletedToday = !it.isCompletedToday)
                                                } else {
                                                    it
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Complete",
                                            tint = Color.White
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = {
                                            habits = habits.filter { it.id != habit.id }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Gray
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

    // Add dialog implementation
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "New Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("What habit are we tracking?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        formCategories.forEach { category ->
                            val isCatSelected = newHabitCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCatSelected) MaterialTheme.colorScheme.primary else Color(0xFFEEEEEE))
                                    .clickable { newHabitCategory = category }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCatSelected) Color.White else Color.Black
                                )
                            }
                        }
                    }

                    Text(text = "Select Emoji", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableEmojis) { emoji ->
                            val isEmojiSelected = newHabitEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isEmojiSelected) Color(0xFFC8E6C9) else Color.Transparent)
                                    .clickable { newHabitEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newHabitName.isNotBlank()) {
                                    val newHabit = Habit(
                                        name = newHabitName,
                                        category = newHabitCategory,
                                        streak = 0,
                                        isCompletedToday = false,
                                        iconEmoji = newHabitEmoji
                                    )
                                    habits = habits + newHabit
                                    showAddDialog = false
                                }
                            },
                            enabled = newHabitName.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Health" -> Color(0xFFE8F5E9)
        "Mind" -> Color(0xFFE3F2FD)
        "Productivity" -> Color(0xFFFFF3E0)
        "Finance" -> Color(0xFFF3E5F5)
        else -> Color(0xFFF5F5F5)
    }
}

fun getCategoryTextColor(category: String): Color {
    return when (category) {
        "Health" -> Color(0xFF2E7D32)
        "Mind" -> Color(0xFF1565C0)
        "Productivity" -> Color(0xFFE65100)
        "Finance" -> Color(0xFF6A1B9A)
        else -> Color(0xFF424242)
    }
}