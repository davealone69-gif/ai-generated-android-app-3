package com.example.droidcraft

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- Data Model and Persistence Layer (Room Database) ---
// These classes would typically reside in separate files (e.g., data/model/, data/dao/, data/repository/)
// but are included here for self-containment as per the prompt's output requirements.

/**
 * [HabitEntity] represents a habit entry in the Room database.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-generated primary key
    val name: String,
    val isCompleted: Boolean = false
)

/**
 * [Habit] is the UI-friendly data class used across the Composables.
 * It's mapped from/to [HabitEntity].
 */
data class Habit(
    val id: Int,
    val name: String,
    val isCompleted: Boolean
) {
    // Convenience constructor for creating new habits before Room assigns an ID.
    // The ID '0' signals Room to auto-generate a new primary key.
    constructor(name: String, isCompleted: Boolean = false) : this(0, name, isCompleted)
}

/**
 * Extension function to convert a [HabitEntity] to a [Habit] for UI display.
 */
fun HabitEntity.toHabit(): Habit {
    return Habit(
        id = this.id,
        name = this.name,
        isCompleted = this.isCompleted
    )
}

/**
 * Extension function to convert a [Habit] (from UI) to a [HabitEntity] for database operations.
 */
fun Habit.toHabitEntity(): HabitEntity {
    return HabitEntity(
        id = this.id,
        name = this.name,
        isCompleted = this.isCompleted
    )
}

/**
 * [HabitDao] provides methods for database access to [HabitEntity].
 */
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)
}

/**
 * [HabitDatabase] is the Room database abstract class.
 */
@Database(entities = [HabitEntity::class], version = 1, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database"
                )
                    .fallbackToDestructiveMigration() // Simple migration strategy for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * [HabitRepository] abstracts the data sources, providing a clean API for the ViewModel.
 */
class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    suspend fun insert(habit: HabitEntity) {
        habitDao.insert(habit)
    }

    suspend fun update(habit: HabitEntity) {
        habitDao.update(habit)
    }

    suspend fun delete(habit: HabitEntity) {
        habitDao.delete(habit)
    }
}

// --- ViewModel Layer ---

/**
 * [HabitViewModel] manages the UI-related data and business logic.
 * It interacts with the [HabitRepository] and exposes data as [StateFlow] for Compose.
 */
class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    val allHabits: StateFlow<List<Habit>>

    init {
        val habitDao = HabitDatabase.getDatabase(application).habitDao()
        repository = HabitRepository(habitDao)

        // Collect habits from the repository and map them to UI-friendly Habit objects.
        // stateIn converts the Flow to a StateFlow, making it suitable for Compose observation.
        allHabits = repository.allHabits
            .map { entities -> entities.map { it.toHabit() } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000), // Keep subscription active for 5 seconds after last collector
                emptyList() // Initial value
            )
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(habit.toHabitEntity())
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(habit.toHabitEntity())
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(habit.toHabitEntity())
        }
    }
}

/**
 * Custom [ViewModelProvider.Factory] for [HabitViewModel] to inject the application context.
 */
class HabitViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- UI Layer (Compose) ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { // Apply MaterialTheme for consistent Material Design styling
                // Obtain the ViewModel instance using the custom factory
                val viewModel: HabitViewModel = viewModel(
                    factory = HabitViewModelFactory(application)
                )
                HabitTrackerScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel) {
    // Collect the list of habits from the ViewModel as a Compose state
    val habits by viewModel.allHabits.collectAsState()

    // State for controlling dialog visibility and the habit being acted upon
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showEditHabitDialog by remember { mutableStateOf(false) }
    var showDeleteHabitDialog by remember { mutableStateOf(false) }
    var selectedHabitForAction by remember { mutableStateOf<Habit?>(null) } // Stores the habit for edit/delete

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DroidCraft Habit Tracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabitDialog = true },
                // Enhance FAB with custom colors from the theme for visual appeal
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add new habit")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply Scaffold's system padding
                .fillMaxSize()
        ) {
            if (habits.isEmpty()) {
                // Display an empty state message when there are no habits
                EmptyState(
                    message = "No habits yet! Click '+' to add one.",
                    modifier = Modifier.weight(1f) // Ensures empty state fills available space
                )
            } else {
                // Display the list of habits using LazyColumn for efficient scrolling
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Space between habit items
                ) {
                    items(items = habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleCompletion = { updatedHabit ->
                                viewModel.updateHabit(updatedHabit)
                            },
                            onEditClick = { habitToEdit ->
                                selectedHabitForAction = habitToEdit
                                showEditHabitDialog = true
                            },
                            onDeleteClick = { habitToDelete ->
                                selectedHabitForAction = habitToDelete
                                showDeleteHabitDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Dialogs for various actions
        if (showAddHabitDialog) {
            AddHabitDialog(
                onDismiss = { showAddHabitDialog = false },
                onConfirm = { name ->
                    viewModel.addHabit(Habit(name = name))
                    showAddHabitDialog = false
                }
            )
        }

        // Use a safe call operator to only show dialogs if a habit is selected for action
        selectedHabitForAction?.let { habit ->
            if (showEditHabitDialog) {
                EditHabitDialog(
                    habit = habit,
                    onDismiss = {
                        showEditHabitDialog = false
                        selectedHabitForAction = null
                    },
                    onConfirm = { updatedHabit ->
                        viewModel.updateHabit(updatedHabit)
                        showEditHabitDialog = false
                        selectedHabitForAction = null
                    }
                )
            }

            if (showDeleteHabitDialog) {
                DeleteHabitDialog(
                    habit = habit,
                    onDismiss = {
                        showDeleteHabitDialog = false
                        selectedHabitForAction = null
                    },
                    onConfirm = { habitToDelete ->
                        viewModel.deleteHabit(habitToDelete)
                        showDeleteHabitDialog = false
                        selectedHabitForAction = null
                    }
                )
            }
        }
    }
}

/**
 * Composable for displaying an empty state message.
 */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add, // Using 'Add' icon as a gentle hint
            contentDescription = null, // Content description can be null if decorative
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/**
 * Composable for displaying a single habit item in the list.
 */
@Composable
fun HabitItem(
    habit: Habit,
    onToggleCompletion: (Habit) -> Unit,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Visually distinguish completed habits with a slightly different background color
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    // Apply strikethrough for completed habits and lighter font weight
                    textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null,
                    fontWeight = if (habit.isCompleted) FontWeight.Light else FontWeight.Normal
                ),
                color = if (habit.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f) // Allows text to take available space
                    .padding(end = 8.dp) // Add some space before the next element
            )

            // Dropdown menu for more actions (Edit, Delete)
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More actions for ${habit.name}")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEditClick(habit)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    Divider() // Visual separator
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteClick(habit)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }

            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = { isChecked ->
                    onToggleCompletion(habit.copy(isCompleted = isChecked))
                }
            )
        }
    }
}

/**
 * Composable for the "Add New Habit" dialog.
 */
@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newHabitName by remember { mutableStateOf("") }
    val isInputValid = newHabitName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Habit") },
        text = {
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                label = { Text("Habit Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = !isInputValid && newHabitName.isNotEmpty(), // Show error if invalid after user input
                supportingText = {
                    if (!isInputValid && newHabitName.isNotEmpty()) {
                        Text("Habit name cannot be empty.", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isInputValid) {
                        onConfirm(newHabitName.trim()) // Trim whitespace
                    }
                },
                enabled = isInputValid // Disable button if input is not valid
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Composable for the "Edit Habit" dialog.
 */
@Composable
fun EditHabitDialog(habit: Habit, onDismiss: () -> Unit, onConfirm: (Habit) -> Unit) {
    var editedHabitName by remember { mutableStateOf(habit.name) }
    val isInputValid = editedHabitName.isNotBlank()
    // Check if the name has genuinely changed and is valid
    val isNameUnchanged = editedHabitName.trim() == habit.name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Habit") },
        text = {
            OutlinedTextField(
                value = editedHabitName,
                onValueChange = { editedHabitName = it },
                label = { Text("Habit Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = !isInputValid && editedHabitName.isNotEmpty(),
                supportingText = {
                    if (!isInputValid && editedHabitName.isNotEmpty()) {
                        Text("Habit name cannot be empty.", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isInputValid && !isNameUnchanged) {
                        onConfirm(habit.copy(name = editedHabitName.trim()))
                    }
                },
                enabled = isInputValid && !isNameUnchanged // Enable only if valid and name has changed
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Composable for the "Delete Habit" confirmation dialog.
 */
@Composable
fun DeleteHabitDialog(habit: Habit, onDismiss: () -> Unit, onConfirm: (Habit) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Habit") },
        text = { Text("Are you sure you want to delete '${habit.name}'? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = { onConfirm(habit) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Use error color for destructive action
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}