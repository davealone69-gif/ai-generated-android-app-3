package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Pomodoro Constants and Enums ---
enum class PomodoroSegment(val durationMinutes: Int, val displayName: String) {
    WORK(25, "Work"),
    SHORT_BREAK(5, "Short Break"),
    LONG_BREAK(15, "Long Break"); // Typically after 4 work sessions

    val durationSeconds: Int get() = durationMinutes * 60
}

enum class TimerState {
    RUNNING, PAUSED, INITIAL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Using MaterialTheme provides default styling for components
            MaterialTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    // --- State Variables ---
    var currentSegment by remember { mutableStateOf(PomodoroSegment.WORK) }
    var timerState by remember { mutableStateOf(TimerState.INITIAL) }
    var timeElapsedSeconds by remember { mutableStateOf(0) } // Seconds elapsed in current segment
    var pomodorosCompleted by remember { mutableStateOf(0) }
    var workSessionCount by remember { mutableStateOf(0) } // Tracks consecutive work sessions for long break logic

    val totalSegmentSeconds = currentSegment.durationSeconds
    val remainingSeconds = totalSegmentSeconds - timeElapsedSeconds

    val scope = rememberCoroutineScope()

    // --- Timer Logic (LaunchedEffect) ---
    LaunchedEffect(timerState, currentSegment) {
        if (timerState == TimerState.RUNNING) {
            while (timeElapsedSeconds < totalSegmentSeconds && timerState == TimerState.RUNNING) {
                delay(1000) // Delay for 1 second
                if (timerState == TimerState.RUNNING) { // Re-check state in case it changed during delay
                    timeElapsedSeconds++
                }
            }

            // Segment completed (only if the timer was running and not paused mid-segment)
            if (timerState == TimerState.RUNNING && timeElapsedSeconds >= totalSegmentSeconds) {
                when (currentSegment) {
                    PomodoroSegment.WORK -> {
                        pomodorosCompleted++
                        workSessionCount++
                        currentSegment = if (workSessionCount % 4 == 0) {
                            PomodoroSegment.LONG_BREAK
                        } else {
                            PomodoroSegment.SHORT_BREAK
                        }
                    }
                    PomodoroSegment.SHORT_BREAK -> {
                        currentSegment = PomodoroSegment.WORK
                    }
                    PomodoroSegment.LONG_BREAK -> {
                        currentSegment = PomodoroSegment.WORK
                        workSessionCount = 0 // Reset work session count after long break
                    }
                }
                timeElapsedSeconds = 0 // Reset for the new segment
                // Timer remains running for the next segment automatically
                timerState = TimerState.RUNNING
            }
        }
    }

    // --- UI Layout ---
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pomodoro Timer") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Status Display
            Text(
                text = currentSegment.displayName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Progress Ring and Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(250.dp)
            ) {
                // Background circle for the ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 20.dp.toPx())
                    )
                }

                // Progress Arc
                val progress = if (totalSegmentSeconds > 0) {
                    timeElapsedSeconds.toFloat() / totalSegmentSeconds
                } else {
                    0f
                }
                val sweepAngle = progress * 360f
                val progressColor = when (currentSegment) {
                    PomodoroSegment.WORK -> MaterialTheme.colorScheme.primary
                    PomodoroSegment.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
                    PomodoroSegment.LONG_BREAK -> MaterialTheme.colorScheme.secondary
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = progressColor,
                        startAngle = -90f, // Start from the top
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Time Text
                Text(
                    text = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 60.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 3. Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                Button(
                    onClick = {
                        scope.launch {
                            timerState = TimerState.INITIAL
                            timeElapsedSeconds = 0
                            currentSegment = PomodoroSegment.WORK
                            pomodorosCompleted = 0 // Reset stats too
                            workSessionCount = 0
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }

                // Start/Pause Button
                Button(
                    onClick = {
                        timerState = if (timerState == TimerState.RUNNING) TimerState.PAUSED else TimerState.RUNNING
                    },
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timerState == TimerState.RUNNING) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (timerState == TimerState.RUNNING) "Pause" else "Start")
                }

                // Skip Button (to next segment)
                Button(
                    onClick = {
                        scope.launch {
                            // Immediately transition to the next segment as if current one finished
                            when (currentSegment) {
                                PomodoroSegment.WORK -> {
                                    pomodorosCompleted++
                                    workSessionCount++
                                    currentSegment = if (workSessionCount % 4 == 0) {
                                        PomodoroSegment.LONG_BREAK
                                    } else {
                                        PomodoroSegment.SHORT_BREAK
                                    }
                                }
                                PomodoroSegment.SHORT_BREAK -> {
                                    currentSegment = PomodoroSegment.WORK
                                }
                                PomodoroSegment.LONG_BREAK -> {
                                    currentSegment = PomodoroSegment.WORK
                                    workSessionCount = 0 // Reset work session count after long break
                                }
                            }
                            timeElapsedSeconds = 0 // Reset for the new segment
                            // If it was running, it continues running for the new segment.
                            // If it was paused or initial, set it to paused (displaying new segment, but not running)
                            if (timerState != TimerState.RUNNING) {
                                timerState = TimerState.PAUSED
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Skip")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // 4. Statistics
            Text(
                text = "Pomodoros Completed: $pomodorosCompleted",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}