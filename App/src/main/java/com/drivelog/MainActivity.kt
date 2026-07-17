package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// Habit Data Class
data class Habit(
    val id: Long,
    val name: String,
    val category: String,
    val streak: Int,
    val isCompleted: Boolean,
    val icon: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF10B981), // Beautiful emerald green
                    onPrimary = Color.White,
                    secondary = Color(0xFF3B82F6), // Blue
                    background = Color(0xFFF3F4F6), // Light grey
                    surface = Color.White,
                    onSurface = Color(0xFF1F2937)
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
    // Default mock data
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(1, "Drink 8 glasses of water", "Health", 5, true, "💧"),
                Habit(2, "Read 15 pages of a book", "Mind", 12, false, "📚"),
                Habit(3, "30-minute workout", "Fitness", 3, true, "🏋️"),
                Habit(4, "Meditate for 10 mins", "Mind", 0, false, "🧘"),
                Habit(5, "Write daily gratitude log", "Spirit", 8, false, "📝")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    // Statistics calculations
    val totalHabits = habits.size
    val completedCount = habits.count { it.isCompleted }
    val progress = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f
    val overallStreak = if (habits.isNotEmpty()) habits.maxOf { it.streak } else 0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HabitFlow",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Track your daily micro-habits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                
                // Top Day Reset Button (Simulation helper)
                IconButton(
                    onClick = {
                        habits = habits.map { it.copy(isCompleted = false) }
                    },
                    modifier = Modifier
                        .background(Color(0xFFE5E7EB), CircleShape)
                        .size(40.dp)
                ) {
                    Text("🔄", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Summary Dashboard Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedCount of $totalHabits completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        // Top Streak Badge
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "🔥 $overallStreak days",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF08A) // Gold warning-like color
                            )
                            Text(
                                text = "Best Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress indicator
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic encouragement message
                    Text(
                        text = when {
                            progress == 1f -> "Outstanding! You nailed everything today! 🎉"
                            progress >= 0.5f -> "Over halfway there! Keep the momentum! 💪"
                            progress > 0f -> "Great start! One step at a time. ✨"
                            else -> "Start your day with a simple win below! 👇"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title list segment
            Text(
                text = "Today's Checklist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Habit List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(habits) { habit ->
                    HabitCard(
                        habit = habit,
                        onToggle = {
                            habits = habits.map {
                                if (it.id == habit.id) {
                                    val newStatus = !it.isCompleted
                                    it.copy(
                                        isCompleted = newStatus,
                                        streak = if (newStatus) it.streak + 1 else maxOf(0, it.streak - 1)
                                    )
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

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF111827),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Habit", fontWeight = FontWeight.Bold)
            }
        }

        // Add Habit Dialog
        if (showAddDialog) {
            AddHabitDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, category, icon ->
                    val newHabit = Habit(
                        id = System.currentTimeMillis(),
                        name = name,
                        category = category,
                        streak = 0,
                        isCompleted = false,
                        icon = icon
                    )
                    habits = habits + newHabit
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    // Distinct category color mapping
    val categoryBg = when (habit.category.lowercase()) {
        "health" -> Color(0xFFD1FAE5)
        "mind" -> Color(0xFFE0F2FE)
        "fitness" -> Color(0xFFFEE2E2)
        "spirit" -> Color(0xFFF3E8FF)
        else -> Color(0xFFF3F4F6)
    }

    val categoryText = when (habit.category.lowercase()) {
        "health" -> Color(0xFF065F46)
        "mind" -> Color(0xFF075985)
        "fitness" -> Color(0xFF991B1B)
        "spirit" -> Color(0xFF6B21A8)
        else -> Color(0xFF374151)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) Color(0xFFF9FAFB) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (habit.isCompleted) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left block (Icon, title, category, streak)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(categoryBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.icon, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (habit.isCompleted) Color(0xFF9CA3AF) else Color(0xFF1F2937),
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Pill
                        Box(
                            modifier = Modifier
                                .background(categoryBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = habit.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = categoryText
                            )
                        }

                        // Streak Indicator
                        if (habit.streak > 0) {
                            Text(
                                text = "🔥 ${habit.streak}d streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Right block (Delete and Checkmark action buttons)
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
                        tint = Color(0xFFEF4444).copy(alpha = 0.7f)
                    )
                }

                // Check Button
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (habit.isCompleted) Color(0xFF10B981) else Color(0xFFE5E7EB)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Habit",
                        tint = if (habit.isCompleted) Color.White else Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, category: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Health") }
    var selectedIcon by remember { mutableStateOf("💧") }

    val categories = listOf("Health", "Mind", "Fitness", "Spirit", "Work")
    val icons = listOf("💧", "📚", "🏋️", "🧘", "📝", "🍎", "🏃", "💡", "💰", "🛌")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create New Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What habit do you want to start?") },
                    placeholder = { Text("e.g., Read for 15 mins") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Selection Label
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4B5563)
                )

                // Category Row/Flow Simulation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFF10B981) else Color(0xFFF3F4F6)
                                )
                                .clickable { category = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFF4B5563),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Emoji Icon Label
                Text(
                    text = "Select Icon/Emoji",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4B5563)
                )

                // Icons Row Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.take(5).forEach { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Transparent
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 24.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.drop(5).take(5).forEach { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Transparent
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF6B7280))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name, category, selectedIcon)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create", color = Color.White)
                    }
                }
            }
        }
    }
}