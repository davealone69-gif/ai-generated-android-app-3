package com.example.droidcraft

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import kotlinx.coroutines.flow.Flow
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
}

@Database(entities = [Habit::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "habits-db").build()
    val habits = db.habitDao().getAllHabits()

    fun addHabit(name: String) {
        viewModelScope.launch { db.habitDao().insert(Habit(name = name)) }
    }

    fun toggleHabit(habit: Habit) {
        viewModelScope.launch { db.habitDao().update(habit.copy(isCompleted = !habit.isCompleted)) }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = dynamicLightColorScheme(this)) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HabitTrackerScreen()
                }
            }
        }
    }
}

@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel = viewModel()) {
    val habits by viewModel.habits.collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Daily Habits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        HabitInput(onAdd = { viewModel.addHabit(it) })
        Spacer(modifier = Modifier.height(16.dp))
        
        if (habits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No habits yet. Start by adding one above!", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(habits, key = { it.id }) { habit ->
                    HabitItem(habit = habit, onToggle = { viewModel.toggleHabit(habit) })
                }
            }
        }
    }
}

@Composable
fun HabitInput(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Enter a new habit") },
        trailingIcon = {
            IconButton(onClick = {
                if (text.isNotBlank()) { onAdd(text); text = ""; focusManager.clearFocus() }
            }) { Icon(Icons.Default.Add, "Add") }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) { onAdd(text); text = ""; focusManager.clearFocus() } }),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(habit.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onToggle) {
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null,
                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }
        }
    }
}