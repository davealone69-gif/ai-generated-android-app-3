package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isCompleted: Boolean = false
)

class HabitViewModel : ViewModel() {
    private val _habits = mutableStateListOf(
        Habit(name = "Morning Meditation"),
        Habit(name = "Drink 2L Water"),
        Habit(name = "Read 30 minutes")
    )
    val habits: List<Habit> = _habits

    fun addHabit(name: String) {
        if (name.isNotBlank()) {
            _habits.add(Habit(name = name))
        }
    }

    fun toggleHabit(id: String) {
        val index = _habits.indexOfFirst { it.id == id }
        if (index != -1) {
            _habits[index] = _habits[index].copy(isCompleted = !_habits[index].isCompleted)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerScreen()
                }
            }
        }
    }
}

@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel = viewModel()) {
    var newHabitName by remember { mutableStateOf("") }

    val onAddHabit = {
        viewModel.addHabit(newHabitName)
        newHabitName = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "My Habits",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                label = { Text("New Habit") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddHabit() })
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilledIconButton(
                onClick = onAddHabit,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(viewModel.habits, key = { it.id }) { habit ->
                HabitItem(habit = habit, onToggle = { viewModel.toggleHabit(habit.id) })
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surfaceVariant
        )
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
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Toggle completion",
                    tint = if (habit.isCompleted) Color(0xFF66BB6A) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}