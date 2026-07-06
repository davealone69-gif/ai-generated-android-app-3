package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isCompleted: Boolean = false
)

sealed class HabitUiState {
    object Loading : HabitUiState()
    data class Success(val habits: List<Habit>) : HabitUiState()
    object Empty : HabitUiState()
}

class HabitViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HabitUiState>(HabitUiState.Empty)
    val uiState = _uiState.asStateFlow()

    fun addHabit(name: String) {
        if (name.isBlank()) return
        val newHabit = Habit(name = name)
        _uiState.update { currentState ->
            val currentList = if (currentState is HabitUiState.Success) currentState.habits else emptyList()
            HabitUiState.Success(currentList + newHabit)
        }
    }

    fun toggleHabit(id: String) {
        _uiState.update { currentState ->
            if (currentState is HabitUiState.Success) {
                val updated = currentState.habits.map {
                    if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
                }
                HabitUiState.Success(updated)
            } else currentState
        }
    }

    fun deleteHabit(id: String) {
        _uiState.update { currentState ->
            if (currentState is HabitUiState.Success) {
                val updated = currentState.habits.filterNot { it.id == id }
                if (updated.isEmpty()) HabitUiState.Empty else HabitUiState.Success(updated)
            } else currentState
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HabitTrackerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var habitText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Habits", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = habitText,
                onValueChange = { habitText = it },
                label = { Text("Add new habit") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { 
                        viewModel.addHabit(habitText)
                        habitText = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Habit")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 
                    viewModel.addHabit(habitText)
                    habitText = ""
                }),
                shape = MaterialTheme.shapes.extraLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is HabitUiState.Empty -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No habits yet. Start by adding one above!", color = MaterialTheme.colorScheme.outline)
                }
                is HabitUiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggle = { viewModel.toggleHabit(habit.id) },
                            onDelete = { viewModel.deleteHabit(habit.id) }
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit, onDelete: () -> Unit) {
    val alpha by animateFloatAsState(if (habit.isCompleted) 0.6f else 1f, label = "alpha")
    
    Card(
        modifier = Modifier.fillMaxWidth().alpha(alpha),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).semantics { contentDescription = "Habit name: ${habit.name}" }
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (habit.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle completion",
                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete habit", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}