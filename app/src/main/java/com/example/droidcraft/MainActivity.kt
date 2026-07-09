package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Define a data class for a Habit
data class Habit(val id: Int, var name: String, var completedToday: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerAppTheme { // Apply a basic theme for Material3 colors
                HabitTrackerScreen()
            }
        }
    }
}

@Composable
fun HabitTrackerAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            onPrimary = Color.White,
            secondary = Color(0xFF03DAC5),
            onSecondary = Color.Black,
            background = Color.White,
            surface = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFE0E0E0) // Lighter grey for cards/containers
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits
    val habits = remember {
        mutableStateListOf(
            Habit(1, "Drink 8 Glasses of Water", false),
            Habit(2, "Read for 30 Minutes", true),
            Habit(3, "Exercise for 1 Hour", false),
            Habit(4, "Meditate for 15 Minutes", false)
        )
    }
    // State for the new habit input field
    var newHabitText by remember { mutableStateOf("") }
    // State for generating unique IDs for new habits
    var nextHabitId by remember { mutableStateOf(habits.size + 1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: App Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "My Daily Habits",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Stay consistent, track your progress!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Middle section: Habit List
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes up available vertical space
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits yet! Add some below.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(habits) { habit ->
                        HabitItem(habit = habit) { clickedHabit ->
                            val index = habits.indexOfFirst { it.id == clickedHabit.id }
                            if (index != -1) {
                                habits[index] = habits[index].copy(completedToday = !habits[index].completedToday)
                            }
                        }
                    }
                }
            }
        }

        // Bottom section: Add New Habit
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add a New Habit",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newHabitText,
                    onValueChange = { newHabitText = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newHabitText.isNotBlank()) {
                            habits.add(Habit(nextHabitId, newHabitText.trim(), false))
                            newHabitText = ""
                            nextHabitId++ // Increment for the next habit
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newHabitText.isNotBlank(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Habit")
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onHabitClick: (Habit) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHabitClick(habit) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.completedToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(
                checked = habit.completedToday,
                onCheckedChange = { onHabitClick(habit) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}