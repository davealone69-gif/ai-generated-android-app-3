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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

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
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val isCompletedToday: Boolean = false,
    val streak: Int = 0
)

fun getCategoryColors(category: String): Pair<Color, Color> {
    return when (category) { 
        "Health" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Mind" -> Color(0xFFE1F5FE) to Color(0xFF0277BD)
        "Fitness" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "Work" -> Color(0xFFEDE7F6) to Color(0xFF651FFF)
        else -> Color(0xFFECEFF1) to Color(0xFF37474F)
    }
}

@Composable
fun HabitTrackerScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 8 cups of water", category = "Health", isCompletedToday = true, streak = 5),
                Habit(name = "15 mins meditation", category = "Mind", isCompletedToday = false, streak = 3),
                Habit(name = "Morning stretching", category = "Fitness", isCompletedToday = true, streak = 12),
                Habit(name = "Read non-fiction book", category = "Mind", isCompletedToday = false, streak = 0),
                Habit(name = "Write daily code updates", category = "Work", isCompletedToday = false, streak = 8)
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Health", "Mind", "Fitness", "Work", "Other")
    val filteredHabits = if (selectedFilter == "All") habits else habits.filter { it.category == selectedFilter }

    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val progressPercentage = (progressFraction * 100).toInt()

    val motivationMessage = when {
        totalCount == 0 -> "Add some positive habits below to begin!"
        progressPercentage == 0 -> "Let's kickstart today's positive changes! 💪"
        progressPercentage < 50 -> "Great start! You're making steady progress. 🌱"
        progressPercentage < 100 -> "Almost there! Keep pushing your limits! 🚀"
        else -> "Absolute Champion! Perfect day completed! 🎉"
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(8.dp)
            ) { 
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) { 
            // Header Custom App Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "HabitFlow",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Track your daily self-growth",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Motivation Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Progress",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$completedCount of $totalCount",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = progressFraction,
                        label = "progress"
                    )

                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = motivationMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Categories Filter Section
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    CustomFilterChip(
                        text = category,
                        isSelected = selectedFilter == category,
                        onClick = { selectedFilter = category }
                    )
                } 
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Habits Section Header
            Text(
                text = if (selectedFilter == "All") "My Habits" else "$selectedFilter Habits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Habits List
            if (filteredHabits.isEmpty()) {
                EmptyHabitState(selectedFilter)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggle = {
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

        // Show Create Dialog
        if (showAddDialog) {
            AddHabitDialog(
                onDismiss = { showAddDialog = false },
                onAddHabit = { name, category ->
                    val newHabit = Habit(
                        name = name,
                        category = category,
                        isCompletedToday = false,
                        streak = 0
                    )
                    habits = habits + newHabit
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColors = getCategoryColors(habit.category)
    val checkColor by animateColorAsState(
        targetValue = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "checkColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Complete Circle Checkbox
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (habit.isCompletedToday) checkColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    .clickable { onToggle() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (habit.isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Habit Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category Chip Badge
                Box(
                    modifier = Modifier
                        .background(categoryColors.first, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = habit.category,
                        color = categoryColors.second,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (habit.streak > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🔥 ${habit.streak} day streak",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }

            // Delete Action
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

@Composable
fun CustomFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CustomCategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColors = getCategoryColors(text)
    val backgroundColor = if (isSelected) categoryColors.second else categoryColors.first
    val textColor = if (isSelected) Color.White else categoryColors.second

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyHabitState(category: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✨",
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (category == "All") "No habits tracked yet" else "No habits under '$category'",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tap the '+' button down below to construct positive habits and daily goals!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (name: String, category: String) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }
    val categories = listOf("Health", "Mind", "Fitness", "Work", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Create New Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Read for 15 mins") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        CustomCategoryChip(
                            text = category,
                            isSelected = category == selectedCategory,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

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
                                onAddHabit(habitName.trim(), selectedCategory)
                            }
                        },
                        enabled = habitName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Habit")
                    }
                }
            }
        }
    }
}