package com.drivelog

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            val colors = if (isDarkTheme) {
                darkColorScheme(
                    primary = Color(0xFF818CF8),
                    onPrimary = Color(0xFF0F172A),
                    secondary = Color(0xFF34D399),
                    onSecondary = Color(0xFF0F172A),
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B),
                    surfaceVariant = Color(0xFF334155),
                    onSurface = Color(0xFFF8FAFC),
                    onSurfaceVariant = Color(0xFFCBD5E1),
                    error = Color(0xFFF87171)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF4F46E5),
                    onPrimary = Color.White,
                    secondary = Color(0xFF10B981),
                    onSecondary = Color.White,
                    background = Color(0xFFF8FAFC),
                    surface = Color.White,
                    surfaceVariant = Color(0xFFF1F5F9),
                    onSurface = Color(0xFF0F172A),
                    onSurfaceVariant = Color(0xFF64748B),
                    error = Color(0xFFEF4444)
                )
            }

            MaterialTheme(colorScheme = colors) {
                HabitTrackerScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

data class Habit(
    val id: Int,
    val name: String,
    val emoji: String,
    val streak: Int,
    val isCompletedToday: Boolean,
    val category: String = "General"
) {
    fun serialize(): String {
        return "$id|||$name|||$emoji|||$streak|||$isCompletedToday|||$category"
    }

    companion object {
        fun deserialize(str: String): Habit? {
            return try {
                val parts = str.split("|||")
                Habit(
                    id = parts[0].toInt(),
                    name = parts[1],
                    emoji = parts[2],
                    streak = parts[3].toInt(),
                    isCompletedToday = parts[4].toBoolean(),
                    category = parts.getOrNull(5) ?: "General"
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("habitflow_prefs", android.content.Context.MODE_PRIVATE)

    var habits by mutableStateOf<List<Habit>>(emptyList())
        private set

    var filterType by mutableStateOf("All")
    var searchQuery by mutableStateOf("")

    // Dialog state management
    var showAddDialog by mutableStateOf(false)
    var newHabitName by mutableStateOf("")
    var selectedEmoji by mutableStateOf("🎯")
    var selectedCategory by mutableStateOf("Health")
    var validationError by mutableStateOf<String?>(null)

    // Habit deletion target state
    var pendingDeleteHabit by mutableStateOf<Habit?>(null)

    init {
        loadHabits()
    }

    private fun loadHabits() {
        val saved = prefs.getStringSet("habits_v2", null)
        if (saved != null) {
            habits = saved.mapNotNull { Habit.deserialize(it) }.sortedBy { it.id }
        } else {
            habits = listOf(
                Habit(1, "Drink 3L Water", "💧", 5, true, "Health"),
                Habit(2, "Read 15 Pages", "📚", 12, false, "Mind"),
                Habit(3, "Daily Workout", "🏃", 4, false, "Fitness"),
                Habit(4, "Meditate 10 mins", "🧘", 9, true, "Mind")
            )
            saveHabits()
        }
    }

    private fun saveHabits() {
        val set = habits.map { it.serialize() }.toSet()
        prefs.edit().putStringSet("habits_v2", set).apply()
    }

    fun addHabit() {
        val trimmed = newHabitName.trim()
        if (trimmed.isBlank()) {
            validationError = "Habit name cannot be empty!"
            return
        }
        if (trimmed.length > 28) {
            validationError = "Keep it under 28 characters."
            return
        }

        val nextId = (habits.maxOfOrNull { it.id } ?: 0) + 1
        val newHabit = Habit(
            id = nextId,
            name = trimmed,
            emoji = selectedEmoji,
            streak = 0,
            isCompletedToday = false,
            category = selectedCategory
        )
        habits = habits + newHabit
        saveHabits()
        closeAndResetDialog()
    }

    fun toggleHabit(id: Int) {
        habits = habits.map {
            if (it.id == id) {
                val nextCompleted = !it.isCompletedToday
                val nextStreak = if (nextCompleted) it.streak + 1 else maxOf(0, it.streak - 1)
                it.copy(isCompletedToday = nextCompleted, streak = nextStreak)
            } else it
        }
        saveHabits()
    }

    fun deleteHabit(id: Int) {
        habits = habits.filter { it.id != id }
        saveHabits()
        pendingDeleteHabit = null
    }

    fun closeAndResetDialog() {
        showAddDialog = false
        newHabitName = ""
        selectedEmoji = "🎯"
        selectedCategory = "Health"
        validationError = null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitTrackerScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: HabitViewModel = viewModel()
) {
    val habits = viewModel.habits
    val filterType = viewModel.filterType
    val searchQuery = viewModel.searchQuery

    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val completionProgress by animateFloatAsState(
        targetValue = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
        label = "progressAnimation"
    )

    val filteredHabits = habits.filter { habit ->
        val matchesSearch = habit.name.contains(searchQuery, ignoreCase = true) ||
                habit.category.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterType) {
            "Pending" -> !habit.isCompletedToday
            "Completed" -> habit.isCompletedToday
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val motivationalText = when {
        completionProgress == 1f && totalCount > 0 -> "Incredible! 100% of goals completed today! 🎉"
        completionProgress >= 0.7f -> "Almost there! Keep the momentum high! 🔥"
        completionProgress >= 0.4f -> "Great progress. One step at a time! 🌱"
        completedCount > 0 -> "You've started! Small steps yield huge results! ✨"
        else -> "Begin your journey today! Tap the circles to complete."
    }

    val emojis = listOf("🎯", "💧", "📚", "🏃", "🧘", "✍️", "🥗", "⏰", "💻", "💪", "🍎", "🎨")
    val categories = listOf("Health", "Mind", "Fitness", "Work", "Other")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Beautiful Top App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HabitFlow",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your daily consistency system",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Theme Toggle Switch / Button Custom Style
                IconButton(
                    onClick = onThemeToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Text(
                        text = if (isDarkTheme) "☀️" else "🌙",
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Scorecard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedCount of $totalCount completed",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${(completionProgress * 100).toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val barColor by animateColorAsState(
                        targetValue = when {
                            completionProgress >= 1f -> MaterialTheme.colorScheme.secondary
                            completionProgress >= 0.5f -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        },
                        label = "progressBarColor"
                    )

                    LinearProgressIndicator(
                        progress = completionProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = motivationalText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                placeholder = { Text("Search habit or category...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending", "Completed").forEach { type ->
                    val isSelected = filterType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.filterType = type }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habits List Title
            Text(
                text = "$filterType Habits",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable dynamic list of Habits
            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results matching '$searchQuery'" else "Clean slate! Add your first habit.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
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
                        val cardBgColor by animateColorAsState(
                            targetValue = if (habit.isCompletedToday) {
                                if (isDarkTheme) Color(0xFF132D24) else Color(0xFFECFDF5)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            label = "cardBgColor"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement(),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (habit.isCompletedToday) {
                                    if (isDarkTheme) Color(0xFF1E5842) else Color(0xFFA7F3D0)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Emoji container
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (habit.isCompletedToday) {
                                                if (isDarkTheme) Color(0xFF1A4D3A) else Color(0xFFD1FAE5)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = habit.emoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Habit details column
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = habit.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (habit.isCompletedToday) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Streak tag
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = "Streak",
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${habit.streak}d streak",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Category tag
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = habit.category,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                // Right Controls: Complete & Delete Action Buttons
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Complete checkbox button
                                    IconButton(
                                        onClick = { viewModel.toggleHabit(habit.id) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    if (habit.isCompletedToday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (habit.isCompletedToday) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Completed Today",
                                                    tint = MaterialTheme.colorScheme.onSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Direct trigger trigger dialog
                                    IconButton(
                                        onClick = { viewModel.pendingDeleteHabit = habit }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Habit",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
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

    // Modern Confirmation Dialog for Deleting Habit
    viewModel.pendingDeleteHabit?.let { habitToDelete ->
        AlertDialog(
            onDismissRequest = { viewModel.pendingDeleteHabit = null },
            title = {
                Text(
                    text = "Delete Habit?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${habitToDelete.name}\"? This action cannot be undone.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteHabit(habitToDelete.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.pendingDeleteHabit = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Modal dialog for Creating New Habits (Refactored and Validated)
    if (viewModel.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAndResetDialog() },
            title = {
                Text(
                    text = "Create New Habit",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Habit Title Input
                    Column {
                        OutlinedTextField(
                            value = viewModel.newHabitName,
                            onValueChange = {
                                viewModel.newHabitName = it
                                if (viewModel.validationError != null) viewModel.validationError = null
                            },
                            label = { Text("What is your daily goal?") },
                            singleLine = true,
                            isError = viewModel.validationError != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        // Validation error notice
                        AnimatedVisibility(visible = viewModel.validationError != null) {
                            Text(
                                text = viewModel.validationError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    // Category Selection Row
                    Column {
                        Text(
                            text = "Category",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                val isSelected = viewModel.selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { viewModel.selectedCategory = category }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = category,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Emoji Grid Choice Simulator
                    Column {
                        Text(
                            text = "Select Icon Representative",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            emojis.take(6).forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (viewModel.selectedEmoji == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { viewModel.selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            emojis.takeLast(6).forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (viewModel.selectedEmoji == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { viewModel.selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addHabit() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create Habit", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAndResetDialog() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}