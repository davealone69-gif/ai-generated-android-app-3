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
                    background = Color(0xFFF6F8FA),
                    surface = Color.White
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

enum class HabitCategory(val icon: String, val displayName: String, val baseColor: Color) {
    HEALTH("🍎", "Health", Color(0xFFE8F5E9)),
    FITNESS("💪", "Fitness", Color(0xFFFFEBEE)),
    MIND("🧘", "Mind", Color(0xFFF3E5F5)),
    WORK("💼", "Work", Color(0xFFE1F5FE)),
    CREATIVE("🎨", "Creative", Color(0xFFFFFDE7))
}

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: HabitCategory,
    val streak: Int = 0,
    val completedToday: Boolean = false,
    val totalCompletions: Int = 0,
    val color: Color = Color(0xFF6200EE)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(name = "Drink 8 glasses of water", category = HabitCategory.HEALTH, streak = 5, completedToday = true, totalCompletions = 12, color = Color(0xFF4CAF50)),
                Habit(name = "30 mins Morning Workout", category = HabitCategory.FITNESS, streak = 3, completedToday = false, totalCompletions = 8, color = Color(0xFFFF5722)),
                Habit(name = "Meditate for 10 minutes", category = HabitCategory.MIND, streak = 12, completedToday = true, totalCompletions = 24, color = Color(0xFF9C27B0)),
                Habit(name = "Read 10 pages of a book", category = HabitCategory.CREATIVE, streak = 0, completedToday = false, totalCompletions = 4, color = Color(0xFFFFC107))
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<HabitCategory?>(null) }

    val totalHabits = habits.size
    val completedHabits = habits.count { it.completedToday }
    val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Habits",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Track your daily self-improvement journey",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Summary Dashboard Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$completedHabits of $totalHabits habits done",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    )
                }
            }

            // Categories Filter Title
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(HabitCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategoryFilter == category,
                        onClick = { selectedCategoryFilter = category },
                        label = { Text("${category.icon} ${category.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Filtered habits list
            val filteredHabits = if (selectedCategoryFilter != null) {
                habits.filter { it.category == selectedCategoryFilter }
            } else {
                habits
            }

            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No habits here yet!",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap + to start building strong habits.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleComplete = {
                                habits = habits.map {
                                    if (it.id == habit.id) {
                                        val nextCompleted = !it.completedToday
                                        val newStreak = if (nextCompleted) it.streak + 1 else maxOf(0, it.streak - 1)
                                        val newTotal = if (nextCompleted) it.totalCompletions + 1 else maxOf(0, it.totalCompletions - 1)
                                        it.copy(completedToday = nextCompleted, streak = newStreak, totalCompletions = newTotal)
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
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAddHabit = { name, category, color ->
                val newHabit = Habit(
                    name = name,
                    category = category,
                    color = color
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
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Indicator Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(habit.category.baseColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.category.icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Habit Details info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (habit.completedToday) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (habit.completedToday) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 ${habit.streak} day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "🏆 Total: ${habit.totalCompletions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Quick Delete Button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Habit",
                    tint = Color.LightGray
                )
            }

            // Completion check circle button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (habit.completedToday) habit.color else Color.Transparent
                    )
                    .clickable { onToggleComplete() }
                    .background(
                        if (!habit.completedToday) habit.color.copy(alpha = 0.1f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (habit.completedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed Today",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.Transparent, CircleShape)
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
    onAddHabit: (String, HabitCategory, Color) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.HEALTH) }
    var selectedColor by remember { mutableStateOf(Color(0xFF4CAF50)) }

    val colors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFFFC107), // Amber
        Color(0xFF2196F3), // Blue
        Color(0xFFE91E63)  // Pink
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New Habit 🚀",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("What is your new habit?") },
                    placeholder = { Text("e.g. Read for 15 mins") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Grid/Row for selection of Category
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HabitCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else category.baseColor
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${category.icon} ${category.displayName}",
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Color Selection row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
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
                            if (habitName.isNotBlank()) {
                                onAddHabit(habitName, selectedCategory, selectedColor)
                            }
                        },
                        enabled = habitName.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}