package com.drivelog

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLDecoder
import java.net.URLEncoder

enum class HabitCategory(val displayName: String, val icon: String, val color: Color) {
    HEALTH("Health", "❤️", Color(0xFFE57373)),
    FITNESS("Fitness", "💪", Color(0xFF81C784)),
    MIND("Mind", "🧘", Color(0xFF64B5F6)),
    WORK("Work", "💼", Color(0xFFFFB74D)),
    ROUTINE("Routine", "✨", Color(0xFFBA68C8))
}

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val streak: Int = 0,
    val category: HabitCategory = HabitCategory.ROUTINE
) {
    fun serialize(): String {
        val encodedName = URLEncoder.encode(name, "UTF-8")
        val encodedDesc = URLEncoder.encode(description, "UTF-8")
        return "$id|$encodedName|$encodedDesc|$isCompleted|$streak|${category.name}"
    }

    companion object {
        fun deserialize(serialized: String): Habit? {
            return try {
                val parts = serialized.split("|")
                if (parts.size < 6) return null
                val id = parts[0]
                val name = URLDecoder.decode(parts[1], "UTF-8")
                val desc = URLDecoder.decode(parts[2], "UTF-8")
                val isCompleted = parts[3].toBoolean()
                val streak = parts[4].toIntOrNull() ?: 0
                val category = HabitCategory.valueOf(parts[5])
                Habit(id, name, desc, isCompleted, streak, category)
            } catch (e: Exception) {
                null
            }
        }
    }
}

enum class HabitFilter {
    ALL, ACTIVE, COMPLETED
}

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("habit_tracker_prefs", Context.MODE_PRIVATE)
    private val _habits = mutableStateListOf<Habit>()
    val habits: List<Habit> get() = _habits

    init {
        loadHabits()
    }

    private fun loadHabits() {
        val set = prefs.getStringSet("habits_list", emptySet()) ?: emptySet()
        _habits.clear()
        val loaded = set.mapNotNull { Habit.deserialize(it) }.sortedBy { it.name }
        _habits.addAll(loaded)
    }

    private fun saveHabits() {
        val set = _habits.map { it.serialize() }.toSet()
        prefs.edit().putStringSet("habits_list", set).apply()
    }

    fun addHabit(name: String, description: String, category: HabitCategory) {
        val newHabit = Habit(
            name = name.trim(),
            description = description.trim(),
            category = category
        )
        _habits.add(newHabit)
        saveHabits()
    }

    fun toggleHabitCompletion(id: String) {
        val index = _habits.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = _habits[index]
            val newCompleted = !current.isCompleted
            val newStreak = if (newCompleted) current.streak + 1 else maxOf(0, current.streak - 1)
            _habits[index] = current.copy(isCompleted = newCompleted, streak = newStreak)
            saveHabits()
        }
    }

    fun deleteHabit(id: String) {
        _habits.removeAll { it.id == id }
        saveHabits()
    }

    fun resetAllHabits() {
        for (i in _habits.indices) {
            _habits[i] = _habits[i].copy(isCompleted = false)
        }
        saveHabits()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomAppTheme {
                HabitTrackerAppScreen()
            }
        }
    }
}

@Composable
fun CustomAppTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFF9E86FF),
        onPrimary = Color(0xFF1C0D5E),
        primaryContainer = Color(0xFF35258E),
        onPrimaryContainer = Color(0xFFE2D6FF),
        secondary = Color(0xFF03DAC6),
        onSecondary = Color(0xFF003732),
        background = Color(0xFF0F0E17),
        surface = Color(0xFF161426),
        surfaceVariant = Color(0xFF23213A),
        onBackground = Color(0xFFE8E6E3),
        onSurface = Color(0xFFE8E6E3),
        onSurfaceVariant = Color(0xFFB0ACC0)
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerAppScreen() {
    val viewModel: HabitViewModel = viewModel()
    val habits = viewModel.habits

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(HabitFilter.ALL) }

    val filteredHabits = remember(habits, selectedFilter) {
        when (selectedFilter) {
            HabitFilter.ALL -> habits
            HabitFilter.ACTIVE -> habits.filter { !it.isCompleted }
            HabitFilter.COMPLETED -> habits.filter { it.isCompleted }
        }
    }

    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompleted }
    val progressPercentage = if (totalHabits > 0) (completedHabits.toFloat() / totalHabits.toFloat()) else 0f

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Habit Flow",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (habits.isNotEmpty()) {
                        IconButton(onClick = { viewModel.resetAllHabits() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset All",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
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
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            // Summary Progress Card
            Spacer(modifier = Modifier.height(8.dp))
            ProgressCard(
                completedCount = completedHabits,
                totalCount = totalHabits,
                progress = progressPercentage
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Filter Row
            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Habits Dynamic list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (filteredHabits.isEmpty()) {
                    EmptyStatePlaceholder(selectedFilter)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(
                            items = filteredHabits,
                            key = { it.id }
                        ) { habit ->
                            HabitItemRow(
                                habit = habit,
                                onToggleComplete = { viewModel.toggleHabitCompletion(habit.id) },
                                onDelete = { viewModel.deleteHabit(habit.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, desc, cat ->
                viewModel.addHabit(name, desc, cat)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProgressCard(completedCount: Int, totalCount: Int, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$completedCount of $totalCount completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun FilterChipsRow(selectedFilter: HabitFilter, onFilterSelected: (HabitFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HabitFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HabitItemRow(
    habit: Habit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val scale by animateFloatAsState(if (habit.isCompleted) 0.98f else 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (habit.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onToggleComplete() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Visual Tag
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(habit.category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.category.icon, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (habit.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Streak badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔥 ${habit.streak} days",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (habit.streak > 0) Color(0xFFFFB74D) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Actions Area
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))

                // Custom Animating Checkbox
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (habit.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .border(
                            2.dp,
                            if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            CircleShape
                        )
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (habit.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, HabitCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.HEALTH) }
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New Habit Flow",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What is your habit call?") },
                    placeholder = { Text("e.g. Read Book, Drink Water") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short description (Optional)") },
                    placeholder = { Text("e.g. 15 pages a day") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(HabitCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) category.color else category.color.copy(alpha = 0.15f)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = category.icon)
                                Text(
                                    text = category.displayName,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, description, selectedCategory)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(filter: HabitFilter) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✨",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (filter) {
                HabitFilter.ALL -> "No habits loaded yet"
                HabitFilter.ACTIVE -> "All caught up with active habits!"
                HabitFilter.COMPLETED -> "No completed habits yet"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (filter) {
                HabitFilter.ALL -> "Press '+' below to start mapping consistency."
                HabitFilter.ACTIVE -> "You are perfectly sorted!"
                HabitFilter.COMPLETED -> "Finish one of your daily targets to see it here."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}