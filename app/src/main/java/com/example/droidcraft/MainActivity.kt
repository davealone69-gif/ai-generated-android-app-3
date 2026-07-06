package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Enum to represent different types of Pomodoro sessions
enum class SessionType {
    WORK, SHORT_BREAK, LONG_BREAK
}

// Constants for session durations and logic
const val WORK_DURATION_SECONDS = 25 * 60 // 25 minutes
const val SHORT_BREAK_DURATION_SECONDS = 5 * 60 // 5 minutes
const val LONG_BREAK_DURATION_SECONDS = 15 * 60 // 15 minutes
const val POMODOROS_UNTIL_LONG_BREAK = 4 // Number of work sessions before a long break

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // MaterialTheme provides default styling for Material3 components.
            MaterialTheme {
                PomodoroAppScreen()
            }
        }
    }
}

@Composable
fun PomodoroAppScreen() {
    // State for the timer
    var currentSessionType by remember { mutableStateOf(SessionType.WORK) }
    var timeInSeconds by remember { mutableStateOf(WORK_DURATION_SECONDS) }
    var isRunning by remember { mutableStateOf(false) }
    // Stores the initial duration of the *current* session for progress calculation
    var initialSessionDuration by remember { mutableStateOf(WORK_DURATION_SECONDS) }

    // State for statistics
    var completedPomodoros by remember { mutableStateOf(0) }
    var completedShortBreaks by remember { mutableStateOf(0) }
    var completedLongBreaks by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    // Helper function to get the duration for a given session type
    fun getDurationForSession(type: SessionType): Int {
        return when (type) {
            SessionType.WORK -> WORK_DURATION_SECONDS
            SessionType.SHORT_BREAK -> SHORT_BREAK_DURATION_SECONDS
            SessionType.LONG_BREAK -> LONG_BREAK_DURATION_SECONDS
        }
    }

    // Function to transition to the next session based on current type and statistics
    fun nextSession() {
        isRunning = false // Always pause when transitioning to allow user to initiate next session
        when (currentSessionType) {
            SessionType.WORK -> {
                completedPomodoros++
                if (completedPomodoros % POMODOROS_UNTIL_LONG_BREAK == 0) {
                    currentSessionType = SessionType.LONG_BREAK
                } else {
                    currentSessionType = SessionType.SHORT_BREAK
                }
            }
            SessionType.SHORT_BREAK -> {
                completedShortBreaks++
                currentSessionType = SessionType.WORK
            }
            SessionType.LONG_BREAK -> {
                completedLongBreaks++
                currentSessionType = SessionType.WORK
            }
        }
        // Update initial duration and current time for the new session
        initialSessionDuration = getDurationForSession(currentSessionType)
        timeInSeconds = initialSessionDuration
    }

    // Timer logic using LaunchedEffect for side effects in Compose
    LaunchedEffect(isRunning, timeInSeconds) {
        if (isRunning && timeInSeconds > 0) {
            delay(1000L) // Wait for 1 second
            timeInSeconds--
        } else if (isRunning && timeInSeconds == 0) {
            // Session completed, transition to the next one
            nextSession()
            // The timer will be paused after nextSession(), user needs to click Start again.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround // Distribute elements vertically
    ) {
        // Current Session Title
        Text(
            text = when (currentSessionType) {
                SessionType.WORK -> "Work Session"
                SessionType.SHORT_BREAK -> "Short Break"
                SessionType.LONG_BREAK -> "Long Break"
            },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Timer Progress Ring and Display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp) // Larger size for the ring
        ) {
            // Calculate progress for the CircularProgressIndicator
            val progress = animateFloatAsState(
                targetValue = if (initialSessionDuration > 0) {
                    timeInSeconds.toFloat() / initialSessionDuration.toFloat()
                } else 0f,
                animationSpec = tween(durationMillis = 900, easing = LinearEasing),
                label = "ProgressAnimation"
            ).value

            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 16.dp, // Thicker stroke for the ring
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = formatTime(timeInSeconds),
                style = MaterialTheme.typography.displayLarge, // Larger text for time
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                // Enable Start/Pause if time is not zero or if it's currently paused
                enabled = timeInSeconds > 0 || !isRunning
            ) {
                Text(if (isRunning) "Pause" else "Start")
            }

            Button(
                onClick = {
                    coroutineScope.launch { // Launch to manage state changes
                        isRunning = false
                        initialSessionDuration = getDurationForSession(currentSessionType)
                        timeInSeconds = initialSessionDuration
                    }
                },
                // Enable Reset if not running, or if time is not at its initial value
                enabled = !isRunning || timeInSeconds != initialSessionDuration
            ) {
                Text("Reset")
            }

            Button(
                onClick = { coroutineScope.launch { nextSession() } },
                // Skip is always enabled to allow quick transitions
            ) {
                Text("Skip")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Statistics Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Statistics",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Divider(modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp))
            StatisticRow("Pomodoros Completed:", completedPomodoros)
            StatisticRow("Short Breaks Taken:", completedShortBreaks)
            StatisticRow("Long Breaks Taken:", completedLongBreaks)
            Divider(modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp))
        }
    }
}

/**
 * Composable for displaying a single statistic row.
 */
@Composable
fun StatisticRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.5f)
        )
    }
}

/**
 * Helper function to format total seconds into a "MM:SS" string.
 */
fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}