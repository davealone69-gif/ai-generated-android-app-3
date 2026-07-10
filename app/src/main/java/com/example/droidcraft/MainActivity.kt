package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data class to represent a single habit
data class Habit(val id: Int, var name: String, var isCompleted: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // MaterialTheme provides the default Material 3 design system styling
            MaterialTheme {
                HabitTrackerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    // State to hold the list of habits
    // Using mutableStateListOf for observable list changes
    val habits = remember {
        mutableStateListOf(
            Habit(0, "Drink 8 glasses of water", false),
            Habit(1, "Read 30 minutes", true),
            Habit(2, "Exercise for 20 minutes", false)
        )
    }

    // State for the new habit input field
    var newHabitText by remember { mutableStateOf("") }
    // Counter for unique habit IDs
    var nextHabitId by remember { mutableStateOf(habits.size) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simple Habit Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold to avoid overlap
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // List of Habits
                if (habits.isEmpty()) {
                    Text(
                        text = "No habits added yet! Use the field below to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .wrapContentHeight(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f) // Takes up available space
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            HabitItem(habit = habit) { updatedHabit ->
                                // Find the habit in the list by its ID and update its completion status
                                val index = habits.indexOfFirst { it.id == updatedHabit.id }
                                if (index != -1) {
                                    // Update the item in the list, triggering recomposition
                                    habits[index] = updatedHabit
                                }
                            }
                        }
                    }
                }

                // Add New Habit Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add New Habit",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newHabitText,
                            onValueChange = { newHabitText = it },
                            label = { Text("Habit Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (newHabitText.isNotBlank()) {
                                    habits.add(Habit(nextHabitId++, newHabitText.trim()))
                                    newHabitText = "" // Clear input field
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = newHabitText.isNotBlank() // Enable button only if text is not blank
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Habit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Habit")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun HabitItem(habit: Habit, onHabitCompletionChange: (Habit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
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
                fontWeight = FontWeight.Medium,
                color = if (habit.isCompleted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = { isChecked ->
                    // Create a copy of the habit with the updated completion status
                    onHabitCompletionChange(habit.copy(isCompleted = isChecked))
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}