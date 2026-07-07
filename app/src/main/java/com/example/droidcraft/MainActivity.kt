package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val isInputError: Boolean = false
)

class HabitViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState = _uiState.asStateFlow()

    fun addHabit(name: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(isInputError = true) }
            return
        }
        val newHabit = Habit(name = name.trim())
        _uiState.update { it.copy(habits = it.habits + newHabit, isInputError = false) }
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

    fun clearError() {
        _uiState.update { it.copy(isInputError = false) }
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
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Rituals", fontWeight = FontWeight.ExtraBold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = habitText,
                onValueChange = { 
                    habitText = it
                    if (uiState.isInputError) viewModel.clearError()
                },
                label = { Text("What habit are you tracking?") },
                isError = uiState.isInputError,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (habitText.isNotBlank()) {
                            viewModel.addHabit(habitText)
                            habitText = ""
                            focusManager.clearFocus()
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.addHabit(habitText)
                    if (!uiState.isInputError) {
                        habitText = ""
                        focusManager.clearFocus()
                    }
                })
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.habits.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Your dashboard is clear. Build a new habit!", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items = uiState.habits, key = { it.id }) { habit ->
                        HabitCard(habit, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun HabitCard(habit: Habit, viewModel: HabitViewModel) {
    val alpha by animateFloatAsState(if (habit.isCompleted) 0.7f else 1f, label = "alpha")
    val contentColor by animateColorAsState(if (habit.isCompleted) MaterialTheme.colorScheme.primary else Color.Unspecified, label = "color")

    Card(
        modifier = Modifier.fillMaxWidth().alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.toggleHabit(habit.id) }) {
                Icon(
                    imageVector = if (habit.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle",
                    tint = if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = { viewModel.deleteHabit(habit.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}