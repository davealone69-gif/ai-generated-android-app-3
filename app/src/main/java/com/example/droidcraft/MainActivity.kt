package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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

data class Habit(val id: String = UUID.randomUUID().toString(), val name: String, val isCompleted: Boolean = false)

class HabitViewModel : ViewModel() {
    val habits = mutableStateListOf<Habit>()

    fun addHabit(name: String) {
        if (name.isNotBlank()) {
            habits.add(Habit(name = name))
        }
    }

    fun toggleHabit(id: String) {
        val index = habits.indexOfFirst { it.id == id }
        if (index != -1) {
            val habit = habits[index]
            habits[index] = habit.copy(isCompleted = !habit.isCompleted)
        }
    }

    fun removeHabit(id: String) {
        habits.removeAll { it.id == id }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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
    var habitText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "My Daily Habits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = habitText,
            onValueChange = { habitText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("What do you want to accomplish?") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { 
                    viewModel.addHabit(habitText)
                    habitText = "" 
                }) {
                    Icon(Icons.Default.Add, "Add")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                viewModel.addHabit(habitText)
                habitText = ""
            })
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = viewModel.habits, key = { it.id }) { habit ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.removeHabit(habit.id)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
                        Box(Modifier.fillMaxSize().background(color).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                        }
                    },
                    content = {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(habit.name, style = MaterialTheme.typography.bodyLarge)
                                val tint by animateColorAsState(if (habit.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, label = "color")
                                IconButton(onClick = { viewModel.toggleHabit(habit.id) }) {
                                    Icon(Icons.Default.Check, "Toggle", tint = tint)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}