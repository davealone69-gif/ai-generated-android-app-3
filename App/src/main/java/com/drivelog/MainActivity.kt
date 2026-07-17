package com.drivelog

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[HabitViewModel::class.java]
        setContent {
            HabitTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerApp(viewModel)
                }
            }
        }
    }
}

enum class HabitCategory(val displayName: String, val emoji: String, val colorLight: Color, val colorDark: Color) {
    HEALTH("Health", "🏃", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
    MIND("Mind", "🧘", Color(0xFFE3F2FD), Color(0xFF1565C0)),
    PRODUCTIVITY("Work", "📝", Color(0xFFFFF8E1), Color(0xFFF57F17)),
    FINANCE("Finance", "💰", Color(0xFFF3E5F5), Color(0xFF6A1B9A)),
    CREATIVITY("Art", "🎨", Color(0xFFFBE9E7), Color(0xFFD84315))
}

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: HabitCategory = HabitCategory.HEALTH,
    val streak: Int = 0,
    val completedToday: Boolean = false,
    val lastCompletedTimestamp: Long = 0L
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("habit_tracker_prefs", Context.MODE_PRIVATE)
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        val jsonString = prefs.getString("habits_list", null)
        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<Habit>()
                for (i in 0 until jsonArray.length()) {
                    list.add(jsonToHabit(jsonArray.getJSONObject(i)))
                }
                _habits.value = list
            } catch (e: Exception) {
                _habits.value = getMockHabits()
            }
        } else {
            _habits.value = getMockHabits()
            saveHabits(_habits.value)
        }
    }

    private fun saveHabits(list: List<Habit>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(habitToJson(it)) }
        prefs.edit().putString("habits_list", jsonArray.toString()).apply()
    }

    private fun habitToJson(habit: Habit): JSONObject {
        val json = JSONObject()
        json.put("id", habit.id)
        json.put("name", habit.name)
        json.put("category", habit.category.name)
        json.put("streak", habit.streak)
        json.put("completedToday", habit.completedToday)
        json.put("lastCompletedTimestamp", habit.lastCompletedTimestamp)
        return json
    }

    private fun jsonToHabit(json: JSONObject): Habit {
        return Habit(
            id = json.getString("id"),
            name = json.getString("name"),
            category = HabitCategory.valueOf(json.optString("category", HabitCategory.HEALTH.name)),
            streak = json.getInt("streak"),
            completedToday = json.getBoolean("completedToday"),
            lastCompletedTimestamp = json.optLong("lastCompletedTimestamp", 0L)
        )
    }

    fun addHabit(name: String, category: HabitCategory) {
        if (name.isBlank()) return
        val newHabit = Habit(
            name = name.trim(),
            category = category,
            streak = 0,
            completedToday = false,
            lastCompletedTimestamp = 0L
        )
        val updated = _habits.value + newHabit
        _habits.value = updated
        saveHabits(updated)
    }

    fun toggleHabit(id: String) {
        val updated = _habits.value.map { habit ->
            if (habit.id == id) {
                val nextCompleted = !habit.completedToday
                val nextStreak = if (nextCompleted) habit.streak + 1 else maxOf(0, habit.streak - 1)
                habit.copy(
                    completedToday = nextCompleted,
                    streak = nextStreak,
                    lastCompletedTimestamp = if (nextCompleted) System.currentTimeMillis() else 0L
                )
            } else habit
        }
        _habits.value = updated
        saveHabits(updated)
    }

    fun deleteHabit(id: String) {
        val updated = _habits.value.filter { it.id != id }
        _habits.value = updated
        saveHabits(updated)
    }

    private fun getMockHabits(): List<Habit> = listOf(
        Habit(name = "Drink 8 glasses of water", category = HabitCategory.HEALTH, streak = 5, completedToday = true),
        Habit(name = "Read 10 pages of a book", category = HabitCategory.MIND, streak = 3, completedToday = false),
        Habit(name = "30 minutes gym session", category = HabitCategory.HEALTH, streak = 12, completedToday = true),
        Habit(name = "Practice mindful breathing", category = HabitCategory.MIND, streak = 0, completedToday = false)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HabitFlow",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Habit") },
                text = { Text("New Habit") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val totalCount = habits.size
            val completedCount = habits.count { it.completedToday }
            val completionRate = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0

            HeroProgressDashboard(
                completedCount = completedCount,
                totalCount = totalCount,
                completionRate = completionRate
            )

            FilterChipRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            val filteredHabits = remember(habits, selectedFilter) {
                when (selectedFilter) {
                    "Done" -> habits.filter { it.completedToday }
                    "Pending" -> habits.filter { !it.completedToday }
                    else -> habits
                }
            }

            AnimatedContent(
                targetState = filteredHabits.isEmpty(),
                label = "list_state"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyStatePlaceholder(selectedFilter)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredHabits, key = { it.id }) { habit ->
                            HabitItemRow(
                                habit = habit,
                                onToggle = { viewModel.toggleHabit(habit.id) },
                                onDelete = { viewModel.deleteHabit(habit.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddHabitDialog(
            onDismiss = { showDialog = false },
            onSave = { name, category ->
                viewModel.addHabit(name, category)
                showDialog = false
            }
        )
    }
}

@Composable
fun HeroProgressDashboard(
    completedCount: Int,
    totalCount: Int,
    completionRate: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        )
    ) { 
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { 
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Track your Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedCount of $totalCount completed today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                val animatedProgress by animateFloatAsState(
                    targetValue = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
                    animationSpec = tween(500),
                    label = "progress"
                )
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completionRate%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "Pending", "Done")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitItemRow(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val categoryBg = if (isDark) habit.category.colorDark else habit.category.colorLight
    val categoryText = if (isDark) habit.category.colorLight else habit.category.colorDark

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.category.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryBg.copy(alpha = 0.4f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = habit.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryText
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Streak",
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${habit.streak} Days",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconToggleButton(
                    checked = habit.completedToday,
                    onCheckedChange = { onToggle() },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Habit"
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(selectedFilter: String) { 
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-40).dp)
        ) {
            Text(
                text = "✨",
                fontSize = 54.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (selectedFilter) {
                    "Done" -> "No completed habits yet!"
                    "Pending" -> "Hooray! All caught up!"
                    else -> "Start your journey!"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Small choices define your destiny. Tap the button to design a new positive habit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onSave: (String, HabitCategory) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.HEALTH) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Build a Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Enter habit name...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitCategory.values().forEach { category ->
                        val isSelected = category == selectedCategory
                        val lightColor = if (isSystemInDarkTheme()) category.colorDark else category.colorLight
                        val textAndBorder = if (isSystemInDarkTheme()) category.colorLight else category.colorDark

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) lightColor else Color.Transparent)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(category.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) textAndBorder else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(text, selectedCategory) },
                        enabled = text.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        background = Color(0xFFFEF7FF),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1D1B20),
        onSurfaceVariant = Color(0xFF49454F),
        error = Color(0xFFB3261E)
    )

    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        background = Color(0xFF141218),
        surface = Color(0xFF1D1B20),
        onSurface = Color(0xFFE6E1E5),
        onSurfaceVariant = Color(0xFFCAC4D0),
        error = Color(0xFFF2B8B5)
    )

    val colors = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
