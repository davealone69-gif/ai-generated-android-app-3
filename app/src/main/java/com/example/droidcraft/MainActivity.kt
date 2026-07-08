package com.example.droidcraft

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- Data Model ---
data class Habit(val id: Int, val name: String, val completed: Boolean = false)

// --- ViewModel ---
class HabitViewModel : ViewModel() {
    private val _habits = MutableStateFlow(listOf(
        Habit(1, "Drink 2L Water"),
        Habit(2, "Morning Exercise"),
        Habit(3, "Read 30 mins")
    ))
    val habits = _habits.asStateFlow()

    fun addHabit(name: String) {
        if (name.isBlank()) return
        val newHabit = Habit(id = (_habits.value.maxOfOrNull { it.id } ?: 0) + 1, name = name)
        _habits.update { it + newHabit }
    }

    fun toggleHabit(id: Int) {
        _habits.update { list ->
            list.map { if (it.id == id) it.copy(completed = !it.completed) else it }
        }
    }
}

// --- Activity ---
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

// --- Composable UI ---
@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel = viewModel()) {
    val habits by viewModel.habits.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val onAddHabit = {
        if (textInput.isNotBlank()) {
            viewModel.addHabit(textInput)
            textInput = ""
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Daily Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("New Habit") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddHabit() })
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilledIconButton(
                onClick = onAddHabit,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(habits, key = { it.id }) { habit ->
                HabitItem(habit = habit, onToggle = { viewModel.toggleHabit(habit.id) })
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Toggle completion",
                    tint = if (habit.completed) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }
        }
    }
}