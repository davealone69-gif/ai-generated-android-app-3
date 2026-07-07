package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val errorMessage: String? = null
)

class HabitViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState = _uiState.asStateFlow()

    fun addHabit(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Habit name cannot be empty") }
            return
        }
        val newHabit = Habit(name = trimmed)
        _uiState.update { it.copy(habits = it.habits + newHabit, errorMessage = null) }
    }

    fun toggleHabit(id: String) {
        _uiState.update { state ->
            state.copy(habits = state.habits.map {
                if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
            })
        }
    }

    fun deleteHabit(id: String) {
        _uiState.update { state ->
            state.copy(habits = state.habits.filterNot { it.id == id })
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6200EE),
                    secondary = Color(0xFF03DAC6),
                    background = Color(0xFFF8F9FA),
                    surface = Color.White
                )
            ) {
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
                title = { Text("Daily Rituals", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = habitText,
                onValueChange = { habitText = it },
                label = { Text("New Habit") },
                isError = uiState.errorMessage != null,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.addHabit(habitText)
                        habitText = ""
                    }) { Icon(Icons.Default.Add, "Add") }
                },
                shape = RoundedCornerShape(12.dp)
            )
            
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = uiState.habits, key = { it.id }) { habit ->
                    HabitItem(habit, viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItem(habit: Habit, viewModel: HabitViewModel) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                viewModel.deleteHabit(habit.id)
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = Color.Red.copy(alpha = 0.8f)
            Box(Modifier.fillMaxSize().background(color, RoundedCornerShape(12.dp)).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
            }
        },
        content = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { viewModel.toggleHabit(habit.id) }) {
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