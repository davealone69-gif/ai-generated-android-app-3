package com.example.droidcraft

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

data class Habit(val id: String = UUID.randomUUID().toString(), val name: String, val isCompleted: Boolean = false)

class HabitViewModel : ViewModel() {
    private val _habits = mutableStateListOf<Habit>()
    val habits: List<Habit> get() = _habits

    fun addHabit(name: String) {
        if (name.isNotBlank()) {
            _habits.add(Habit(name = name.trim()))
        }
    }

    fun toggleHabit(habitId: String) {
        val index = _habits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val habit = _habits[index]
            _habits[index] = habit.copy(isCompleted = !habit.isCompleted)
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

@Composable
fun HabitTrackerScreen(viewModel: HabitViewModel = viewModel()) {
    var habitText by remember { mutableStateOf("") }

    fun handleAdd() {
        if (habitText.isNotBlank()) {
            viewModel.addHabit(habitText)
            habitText = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "Daily Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = habitText,
                onValueChange = { habitText = it },
                label = { Text("Add new habit...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { handleAdd() })
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilledIconButton(
                onClick = { handleAdd() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(viewModel.habits, key = { it.id }) { habit ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = habit.name, 
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = habit.isCompleted,
                            onCheckedChange = { viewModel.toggleHabit(habit.id) }
                        )
                    }
                }
            }
        }
    }
}