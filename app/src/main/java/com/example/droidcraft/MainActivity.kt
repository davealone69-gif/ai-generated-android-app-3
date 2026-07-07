package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isCompleted: Boolean = false
)

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert
    suspend fun insert(habit: Habit)

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)
}

@Database(entities = [Habit::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}

class HabitViewModel(private val dao: HabitDao) : ViewModel() {
    val habits = dao.getAllHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { dao.insert(Habit(name = name.trim())) }
    }

    fun toggleHabit(habit: Habit) {
        viewModelScope.launch { dao.update(habit.copy(isCompleted = !habit.isCompleted)) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { dao.delete(habit) }
    }
}

class HabitViewModelFactory(private val dao: HabitDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HabitViewModel(dao) as T
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "habits.db").build()
        val factory = HabitViewModelFactory(db.habitDao())
        
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HabitTrackerScreen(viewModel(factory = factory))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsState()
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily Rituals", fontWeight = FontWeight.Black) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter a new habit") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.addHabit(text)
                    text = ""
                    keyboardController?.hide()
                }),
                trailingIcon = {
                    IconButton(onClick = { viewModel.addHabit(text); text = ""; keyboardController?.hide() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(habits, key = { it.id }) { habit ->
                    HabitItem(habit, { viewModel.toggleHabit(habit) }, { viewModel.deleteHabit(habit) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
    })

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent, label = "")
            Box(Modifier.fillMaxSize().background(color, RoundedCornerShape(12.dp)).padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            }
        },
        content = {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onToggle) {
                        Icon(
                            if (habit.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                            "Toggle",
                            tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    )
}