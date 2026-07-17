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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6366F1),
                    secondary = Color(0xFFEC4899),
                    tertiary = Color(0xFF10B981),
                    background = Color(0xFFF8FAFC),
                    surface = Color.White
                )
            ) {
                HabitTrackerScreen()
            }
        }
    }
}

data class Habit(
    val id: Int,
    val name: String,
    val category: String,
    val streak: Int,
    val isCompletedToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // Initial Mock Habits
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(1, "Drink 8 glasses of water", "🍎 Health", 3, false),
                Habit(2, "Read 15 minutes", "🧠 Mind", 5, true),
                Habit(3, "Morning walk", "🍎 Health", 12, true),
                Habit(4, "Write journal", "🧠 Mind", 0, false),
                Habit(5, "Plan next workday", "💼 Work", 7, true)
            )
        )
    }

    var nextId by remember { mutableStateOf(6) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    
    // Form fields for adding new habit
    var newHabitName by remember { mutableStateOf("") }
    var newHabitCategory by remember { mutableStateOf("🍎 Health") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("🍎 Health", "🧠 Mind", "💼 Work", "✨ Other")
    val filterCategories = listOf("All") + categories

    // Quick Stats Calculations
    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val completionPercentage = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val currentStreakRecord = habits.maxOfOrNull { it.streak } ?: 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HabitQuest",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Habit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Habit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Stats Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFEEF2FF), Color(0xFFF0FDF4))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Progress",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 18.sp
                            )
                            Text(
                                text = "$completedCount of $totalCount habits completed",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                        // Circular Progress Indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(54.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = completionPercentage,
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "${(completionPercentage * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Streak",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Best Active Streak:",
                                fontSize = 14.sp,
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = "$currentStreakRecord days",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterCategories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
                            )
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Habit List Header
            Text(
                text = "My Habits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Habit Items
            val filteredHabits = habits.filter {
                selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
            }

            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "✨",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No habits found here yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            "Create a new habit to start your journey!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (habit.isCompletedToday) Color(0xFFF0FDF4) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular check indicator button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (habit.isCompletedToday) MaterialTheme.colorScheme.tertiary else Color(0xFFE2E8F0)
                                        )
                                        .clickable {
                                            habits = habits.map {
                                                if (it.id == habit.id) {
                                                    val nextCompletedState = !it.isCompletedToday
                                                    it.copy(
                                                        isCompletedToday = nextCompletedState,
                                                        streak = if (nextCompletedState) it.streak + 1 else maxOf(0, it.streak - 1)
                                                    )
                                                } else {
                                                    it
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (habit.isCompletedToday) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Habit details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = habit.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (habit.isCompletedToday) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFF0F172A),
                                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else null
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = habit.category,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "🔥 ${habit.streak} days streak",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (habit.streak > 0) Color(0xFFEF4444) else Color(0xFF64748B)
                                        )
                                    }
                                }

                                // Delete Button
                                IconButton(
                                    onClick = {
                                        habits = habits.filter { it.id != habit.id }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete Habit",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Habit Dialog Modal
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "New Habit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("What habit do you want to track?") },
                        placeholder = { Text("E.g., Read, Drink water, Gym") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = newHabitCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9)
                                    )
                                    .clickable { newHabitCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else Color(0xFF1E293B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            habits = habits + Habit(
                                id = nextId,
                                name = newHabitName.trim(),
                                category = newHabitCategory,
                                streak = 0,
                                isCompletedToday = false
                            )
                            nextId++
                            newHabitName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}