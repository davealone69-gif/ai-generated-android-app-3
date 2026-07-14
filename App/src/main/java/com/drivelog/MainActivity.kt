package com.drivelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4F46E5),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFEEF2FF),
                    onPrimaryContainer = Color(0xFF312E81),
                    secondary = Color(0xFF0EA5E9),
                    onSecondary = Color.White,
                    background = Color(0xFFF8FAFC),
                    surface = Color.White,
                    onBackground = Color(0xFF0F172A),
                    onSurface = Color(0xFF1E293B)
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
    val id: String,
    val name: String,
    val category: String,
    val emoji: String,
    val streak: Int,
    val isCompleted: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit("1", "Drink 8 glasses of water", "Health", "💧", 5, true),
                Habit("2", "Read 15 pages", "Mind", "📚", 12, false),
                Habit("3", "10-minute meditation", "Mind", "🧘", 3, false),
                Habit("4", "Morning workout", "Health", "🏋️", 0, false),
                Habit("5", "Save money log", "Finance", "💰", 8, true)
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Health", "Mind", "Finance", "Creative", "Other")

    val filteredHabits = if (selectedCategory == "All") {
        habits
    } else {
        habits.filter { it.category == selectedCategory }
    }

    val totalHabits = habits.size
    val completedCount = habits.count { it.isCompleted }
    val progress = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Your Progress Today",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$completedCount of $totalHabits Completed",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.LightGray.copy(alpha = 0.3f),
                    )
                }
            }

            // Category Selector Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(text = category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Habits List
            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                            text = "No habits found. Start fresh!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleCompletion = {
                                habits = habits.map { h ->
                                    if (h.id == habit.id) {
                                        val nextCompleted = !h.isCompleted
                                        h.copy(
                                            isCompleted = nextCompleted,
                                            streak = if (nextCompleted) h.streak + 1 else maxOf(0, h.streak - 1)
                                        )
                                    } else h
                                }
                            },
                            onDelete = {
                                habits = habits.filter { h -> h.id != habit.id }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAddHabit = { name, category, emoji ->
                val newHabit = Habit(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    category = category,
                    emoji = emoji,
                    streak = 0,
                    isCompleted = false
                )
                habits = habits + newHabit
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = if (habit.isCompleted) Color(0xFFF1F5F9) else Color.White,
        label = "cardColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.emoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name and Status
                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (habit.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = habit.category,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = "🔥 ${habit.streak} day streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (habit.streak > 0) Color(0xFFF97316) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Buttons (Done Toggle & Delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = Color.LightGray
                    )
                }

                val buttonColor = if (habit.isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0)
                val checkColor = if (habit.isCompleted) Color.White else Color(0xFF94A3B8)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(buttonColor)
                        .clickable { onToggleCompletion() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Habit",
                        tint = checkColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Health") }
    var selectedEmoji by remember { mutableStateOf("💧") }

    val categories = listOf("Health", "Mind", "Finance", "Creative", "Other")
    val emojis = listOf("💧", "📚", "🧘", "🏋️", "🍎", "🏃", "💻", "🎨", "✍️", "💰", "🛌")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Daily Reading") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Selection
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSel = cat == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9))
                                    .clickable { category = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color.White else Color(0xFF475569),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Emoji Selection
                Column {
                    Text(
                        text = "Select Icon",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(emojis) { emoji ->
                            val isSel = emoji == selectedEmoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            if (name.isNotBlank()) {
                                onAddHabit(name, category, selectedEmoji)
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}