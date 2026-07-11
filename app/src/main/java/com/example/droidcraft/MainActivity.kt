package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Applying MaterialTheme for the app's overall look and feel.
            // In a real app, you'd likely have a custom theme like DroidcraftTheme.
            MaterialTheme {
                HabitTrackerScreen()
            }
        }
    }
}

/**
 * Data class to represent a single habit.
 * [completedDates] uses [SnapshotStateSet] to ensure changes to the set itself are observed by Compose.
 * This allows the UI to react when dates are added or removed.
 */
data class Habit(
    val id: Int, // Unique ID for the habit
    val name: String,
    val completedDates: SnapshotStateSet<Long> = mutableStateSetOf() // Stores LocalDate.toEpochDay() for completed dates
)

@Composable
fun HabitTrackerScreen() {
    // State for the list of habits. SnapshotStateList allows mutations to be observed.
    val habits: SnapshotStateList<Habit> = remember { mutableStateListOf() }

    // State for the new habit input field.
    var newHabitName by remember { mutableStateOf("") }

    // Counter for unique habit IDs.
    var nextHabitId by remember { mutableStateOf(0) }

    // Get today's date as epoch day (number of days since 1970-01-01).
    val todayEpochDay = LocalDate.now().toEpochDay()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Daily Habits") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold to avoid system bar overlaps
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Section for adding new habits
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = newHabitName,
                    onValueChange = { newHabitName = it },
                    label = { Text("New Habit Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            // Add a new habit with a unique ID and clear the input field
                            habits.add(Habit(nextHabitId++, newHabitName.trim()))
                            newHabitName = ""
                        }
                    },
                    enabled = newHabitName.isNotBlank() // Button is enabled only when input is not blank
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            // Display existing habits
            if (habits.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "No habits added yet. Start tracking!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(habit = habit, todayEpochDay = todayEpochDay) { isChecked ->
                            // Update the habit's completedDates based on checkbox state
                            if (isChecked) {
                                habit.completedDates.add(todayEpochDay)
                            } else {
                                habit.completedDates.remove(todayEpochDay)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable to display a single habit item.
 * Features a habit name and a checkbox to mark completion for the current day.
 */
@Composable
fun HabitItem(
    habit: Habit,
    todayEpochDay: Long,
    onCheckedChange: (Boolean) -> Unit
) {
    // Use remember to observe changes in the habit's completedDates for today
    val isCompletedToday = remember(habit, todayEpochDay) {
        habit.completedDates.contains(todayEpochDay)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // Change card color based on completion status
            containerColor = if (isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
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
                color = if (isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = isCompletedToday,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}