package com.example.droidcraft

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

// --- Theme Definition ---
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A), // Green 400
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1B5E20), // Green 900
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF81C784), // Green 300
    onSecondary = Color.Black,
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    background = Color(0xFF121212), // Darker background
    onBackground = Color(0xFFE3E2E6),
    error = Color(0xFFCF6679), // Red
    onError = Color.Black,
    surfaceContainer = Color(0xFF2B2B2B), // Slightly lighter than background for cards
    onSurfaceVariant = Color(0xFFC4C6D0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF388E3C), // Green 700
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9), // Green 100
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF66BB6A), // Green 400
    onSecondary = Color.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    background = Color(0xFFF0F2F5), // Light background
    onBackground = Color(0xFF1C1B1F),
    error = Color(0xFFB00020), // Red
    onError = Color.White,
    surfaceContainer = Color(0xFFFFFFFF), // White for cards
    onSurfaceVariant = Color(0xFF44474E)
)

@Composable
fun DroidCraftTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Using Material3 default typography
        content = content
    )
}

// --- Data Layer ---
// Data class to represent a single habit, now serializable
@kotlinx.serialization.Serializable
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String = "",
    var currentStreak: Int = 0,
    var lastCompletedDate: String? = null // Store as String for serialization
) {
    // Helper function to check if the habit was completed on the current day
    fun isCompletedToday(): Boolean {
        return lastCompletedDate == LocalDate.now().toString()
    }
}

// DataStore Preferences Key
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_prefs")
val HABITS_KEY = stringPreferencesKey("habits_list")

class HabitDataStoreManager(private val dataStore: DataStore<Preferences>) {

    private val json = Json { ignoreUnknownKeys = true } // Configure JSON serializer

    // Flow to emit the current list of habits from DataStore
    val habitsFlow: StateFlow<List<Habit>> = MutableStateFlow(emptyList())

    init {
        // Initialize habitsFlow by loading from DataStore
        viewModelScope.launch {
            loadHabits()
        }
    }

    private suspend fun loadHabits() {
        dataStore.data.collect { preferences ->
            val habitsJson = preferences[HABITS_KEY]
            val habitsList = if (habitsJson != null) {
                try {
                    json.decodeFromString<List<Habit>>(habitsJson)
                } catch (e: Exception) {
                    // Handle deserialization errors (e.g., if data structure changes)
                    e.printStackTrace()
                    emptyList()
                }
            } else {
                emptyList()
            }
            (habitsFlow as MutableStateFlow).value = habitsList
        }
    }

    suspend fun saveHabits(habits: List<Habit>) {
        dataStore.edit { preferences ->
            preferences[HABITS_KEY] = json.encodeToString(habits)
        }
    }
}

// --- ViewModel Layer ---
class HabitViewModel(application: Application, private val habitDataStoreManager: HabitDataStoreManager) : AndroidViewModel(application) {

    private val _habits: MutableStateFlow<List<Habit>> = MutableStateFlow(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    init {
        viewModelScope.launch {
            habitDataStoreManager.habitsFlow.collect {
                _habits.value = it
            }
        }
    }

    fun addHabit(name: String, description: String) {
        if (name.isBlank()) return // Simple validation
        viewModelScope.launch {
            val currentHabits = _habits.value.toMutableList()
            currentHabits.add(Habit(name = name.trim(), description = description.trim()))
            habitDataStoreManager.saveHabits(currentHabits)
        }
    }

    fun markHabitComplete(habitId: String) {
        viewModelScope.launch {
            val currentHabits = _habits.value.toMutableList()
            val index = currentHabits.indexOfFirst { it.id == habitId }
            if (index != -1) {
                val habit = currentHabits[index]
                val today = LocalDate.now()

                // If already completed today, do nothing.
                if (habit.lastCompletedDate == today.toString()) {
                    return@launch
                }

                val updatedHabit = habit.copy()

                // Streak logic:
                val lastDate = updatedHabit.lastCompletedDate?.let { LocalDate.parse(it) }
                if (lastDate == null || lastDate == today.minusDays(1)) {
                    updatedHabit.currentStreak++
                } else {
                    updatedHabit.currentStreak = 1 // Streak broken, start new streak
                }
                updatedHabit.lastCompletedDate = today.toString() // Update the last completion date

                currentHabits[index] = updatedHabit
                habitDataStoreManager.saveHabits(currentHabits)
            }
        }
    }

    fun editHabit(habitId: String, newName: String, newDescription: String) {
        if (newName.isBlank()) return // Simple validation
        viewModelScope.launch {
            val currentHabits = _habits.value.toMutableList()
            val index = currentHabits.indexOfFirst { it.id == habitId }
            if (index != -1) {
                val updatedHabit = currentHabits[index].copy(
                    name = newName.trim(),
                    description = newDescription.trim()
                )
                currentHabits[index] = updatedHabit
                habitDataStoreManager.saveHabits(currentHabits)
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            val currentHabits = _habits.value.toMutableList()
            val removed = currentHabits.removeIf { it.id == habitId }
            if (removed) {
                habitDataStoreManager.saveHabits(currentHabits)
            }
        }
    }

    // Factory for creating ViewModel with dependencies
    class Factory(private val application: Application, private val dataStore: DataStore<Preferences>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
                val dataStoreManager = HabitDataStoreManager(dataStore)
                @Suppress("UNCHECKED_CAST")
                return HabitViewModel(application, dataStoreManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// --- UI Layer ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DroidCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Provide ViewModel to the screen
                    val application = application as Application
                    val dataStore = LocalContext.current.dataStore
                    val viewModel: HabitViewModel = viewModel(
                        factory = HabitViewModel.Factory(application, dataStore)
                    )
                    HabitTrackerScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showEditHabitDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HabitForge", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabitDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (habits.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    val dismissState = rememberDismissState(
                        confirmValueChange = { value ->
                            if (value == DismissValue.DismissedToEnd || value == DismissValue.DismissedToStart) {
                                viewModel.deleteHabit(habit.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Habit '${habit.name}' deleted.")
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val color = when (dismissState.dismissDirection) {
                                DismissDirection.StartToEnd -> Color.Red.copy(alpha = 0.8f)
                                DismissDirection.EndToStart -> Color.Red.copy(alpha = 0.8f)
                                null -> Color.Transparent
                            }
                            val icon = Icons.Filled.Delete
                            val alignment = when (dismissState.dismissDirection) {
                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                DismissDirection.EndToStart -> Alignment.CenterEnd
                                null -> Alignment.Center
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = "Delete Habit",
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        },
                        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                        dismissContent = {
                            HabitItem(
                                habit = habit,
                                onMarkComplete = {
                                    viewModel.markHabitComplete(it)
                                    if (!habit.isCompletedToday()) { // Only show if it was marked
                                        scope.launch { snackbarHostState.showSnackbar("Habit '${habit.name}' completed!") }
                                    }
                                },
                                onEdit = {
                                    habitToEdit = it
                                    showEditHabitDialog = true
                                }
                            )
                        }
                    )
                }
            }
        }

        // Add New Habit Dialog
        if (showAddHabitDialog) {
            AddEditHabitDialog(
                title = "Add New Habit",
                confirmButtonText = "Add Habit",
                onDismiss = { showAddHabitDialog = false },
                onConfirm = { name, description ->
                    viewModel.addHabit(name, description)
                    scope.launch { snackbarHostState.showSnackbar("Habit '$name' added!") }
                    showAddHabitDialog = false
                }
            )
        }

        // Edit Habit Dialog
        if (showEditHabitDialog && habitToEdit != null) {
            val currentHabit = habitToEdit!!
            AddEditHabitDialog(
                title = "Edit Habit",
                confirmButtonText = "Save Changes",
                initialName = currentHabit.name,
                initialDescription = currentHabit.description,
                onDismiss = {
                    showEditHabitDialog = false
                    habitToEdit = null
                },
                onConfirm = { name, description ->
                    viewModel.editHabit(currentHabit.id, name, description)
                    scope.launch { snackbarHostState.showSnackbar("Habit '${currentHabit.name}' updated!") }
                    showEditHabitDialog = false
                    habitToEdit = null
                }
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.List,
            contentDescription = "No habits icon",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No habits yet!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Tap the '+' button below to start tracking your first habit.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HabitItem(habit: Habit, onMarkComplete: (String) -> Unit, onEdit: (Habit) -> Unit) {
    val isCompletedToday = habit.isCompletedToday()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(habit) }, // Make card clickable to edit
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star, // Changed icon for streak
                        contentDescription = "Streak indicator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Streak: ${habit.currentStreak} day${if (habit.currentStreak != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { onMarkComplete(habit.id) },
                enabled = !isCompletedToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isCompletedToday) "Completed!" else "Mark Complete")
            }
        }
    }
}

@Composable
fun AddEditHabitDialog(
    title: String,
    confirmButtonText: String,
    initialName: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    val isNameValid = remember { derivedStateOf { name.isNotBlank() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    singleLine = true,
                    isError = !isNameValid.value,
                    supportingText = { if (!isNameValid.value) Text("Name cannot be empty") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNameValid.value) {
                        onConfirm(name, description)
                    }
                },
                enabled = isNameValid.value
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


// --- Previews ---
@Preview(showBackground = true)
@Composable
fun PreviewHabitTrackerScreen() {
    DroidCraftTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // For preview, we can create a dummy ViewModel
            val previewViewModel = object : HabitViewModel(Application(), HabitDataStoreManager(LocalContext.current.dataStore)) {
                private val _previewHabits = MutableStateFlow(
                    listOf(
                        Habit(id = "1", name = "Drink Water", description = "8 glasses a day", currentStreak = 5, lastCompletedDate = LocalDate.now().toString()),
                        Habit(id = "2", name = "Exercise", description = "30 min workout", currentStreak = 2, lastCompletedDate = LocalDate.now().minusDays(1).toString()),
                        Habit(id = "3", name = "Read Book", description = "10 pages before bed", currentStreak = 0)
                    )
                )
                override val habits: StateFlow<List<Habit>> = _previewHabits.asStateFlow()

                override fun markHabitComplete(habitId: String) {
                    val updatedList = _previewHabits.value.toMutableList()
                    val index = updatedList.indexOfFirst { it.id == habitId }
                    if (index != -1) {
                        val habit = updatedList[index]
                        if (!habit.isCompletedToday()) {
                            updatedList[index] = habit.copy(
                                currentStreak = habit.currentStreak + 1,
                                lastCompletedDate = LocalDate.now().toString()
                            )
                            _previewHabits.value = updatedList
                        }
                    }
                }
                override fun addHabit(name: String, description: String) { /* No-op for preview */ }
                override fun editHabit(habitId: String, newName: String, newDescription: String) { /* No-op for preview */ }
                override fun deleteHabit(habitId: String) {
                    _previewHabits.value = _previewHabits.value.filter { it.id != habitId }
                }
            }
            HabitTrackerScreen(viewModel = previewViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyState() {
    DroidCraftTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            EmptyState()
        }
    }
}