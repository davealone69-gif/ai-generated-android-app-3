package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

// Constants for durations
private const val POMODORO_DURATION_MINUTES = 25L
private const val SHORT_BREAK_DURATION_MINUTES = 5L
private const val LONG_BREAK_DURATION_MINUTES = 15L
private const val POMODOROS_PER_LONG_BREAK = 4

// Enums for timer state and mode
enum class TimerState { IDLE, RUNNING, PAUSED }
enum class TimerMode { POMODORO, SHORT_BREAK, LONG_BREAK }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Using MaterialTheme provides default colors and typography
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PomodoroAppScreen()
                }
            }
        }
    }
}

@Composable
fun PomodoroAppScreen() {
    // State variables
    var currentTimerMode by remember { mutableStateOf(TimerMode.POMODORO) }
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var timeRemainingMillis by remember { mutableStateOf(0L) }
    var initialDurationMillis by remember { mutableStateOf(0L) } // Total duration for current mode
    var totalPomodorosCompleted by remember { mutableStateOf(0) }
    var pomodorosSinceLastLongBreak by remember { mutableStateOf(0) }

    // Initialize timer duration based on current mode or reset if needed
    LaunchedEffect(currentTimerMode) {
        val duration = when (currentTimerMode) {
            TimerMode.POMODORO -> POMODORO_DURATION_MINUTES
            TimerMode.SHORT_BREAK -> SHORT_BREAK_DURATION_MINUTES
            TimerMode.LONG_BREAK -> LONG_BREAK_DURATION_MINUTES
        }
        initialDurationMillis = TimeUnit.MINUTES.toMillis(duration)
        if (timerState == TimerState.IDLE) {
            timeRemainingMillis = initialDurationMillis
        }
    }

    // Timer countdown logic
    LaunchedEffect(timerState, timeRemainingMillis) {
        if (timerState == TimerState.RUNNING && timeRemainingMillis > 0) {
            while (timeRemainingMillis > 0 && timerState == TimerState.RUNNING) {
                delay(1000L)
                timeRemainingMillis -= 1000L
                if (timeRemainingMillis <= 0) {
                    timeRemainingMillis = 0
                    timerState = TimerState.IDLE // Timer finished
                    handleTimerCompletion(
                        currentTimerMode,
                        totalPomodorosCompleted,
                        pomodorosSinceLastLongBreak
                    ) { newMode, newTotalPomodoros, newPomodorosSinceLastLongBreak ->
                        currentTimerMode = newMode
                        totalPomodorosCompleted = newTotalPomodoros
                        pomodorosSinceLastLongBreak = newPomodorosSinceLastLongBreak
                    }
                }
            }
        }
    }

    // Progress animation for the ring
    val progress = if (initialDurationMillis > 0) {
        1f - (timeRemainingMillis.toFloat() / initialDurationMillis.toFloat())
    } else 0f

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
    }

    // Helper functions for timer control
    fun startTimer() { timerState = TimerState.RUNNING }
    fun pauseTimer() { timerState = TimerState.PAUSED }
    fun resetTimer() {
        timerState = TimerState.IDLE
        timeRemainingMillis = initialDurationMillis
    }
    fun skipTimer() {
        // Force timer completion logic
        timeRemainingMillis = 0 // Setting to 0 will trigger LaunchedEffect completion logic
        timerState = TimerState.IDLE // Ensures LaunchedEffect stops and handles completion
    }

    fun setModeAndReset(mode: TimerMode) {
        currentTimerMode = mode
        // Explicitly set timerState to IDLE so LaunchedEffect(currentTimerMode) resets timeRemainingMillis
        timerState = TimerState.IDLE
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Mode selection buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeButton(
                text = "Pomodoro",
                isSelected = currentTimerMode == TimerMode.POMODORO,
                onClick = { setModeAndReset(TimerMode.POMODORO) }
            )
            ModeButton(
                text = "Short Break",
                isSelected = currentTimerMode == TimerMode.SHORT_BREAK,
                onClick = { setModeAndReset(TimerMode.SHORT_BREAK) }
            )
            ModeButton(
                text = "Long Break",
                isSelected = currentTimerMode == TimerMode.LONG_BREAK,
                onClick = { setModeAndReset(TimerMode.LONG_BREAK) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer display with progress ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Background ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                    size = Size(size.width - 20.dp.toPx(), size.height - 20.dp.toPx()),
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            // Progress ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = MaterialTheme.colorScheme.primary,
                    startAngle = -90f, // Start from top
                    sweepAngle = animatedProgress.value * 360f,
                    useCenter = false,
                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                    size = Size(size.width - 20.dp.toPx(), size.height - 20.dp.toPx()),
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Text(
                text = formatTime(timeRemainingMillis),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { resetTimer() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
                Spacer(Modifier.width(8.dp))
                Text("Reset")
            }
            Button(onClick = {
                if (timerState == TimerState.RUNNING) pauseTimer() else startTimer()
            }) {
                if (timerState == TimerState.RUNNING) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                    Spacer(Modifier.width(8.dp))
                    Text("Pause")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    Spacer(Modifier.width(8.dp))
                    Text("Start")
                }
            }
            Button(onClick = { skipTimer() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Skip")
                Spacer(Modifier.width(8.dp))
                Text("Skip")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Statistics
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pomodoros completed: $totalPomodorosCompleted",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            val pomodorosUntilLongBreakDisplay = POMODOROS_PER_LONG_BREAK - pomodorosSinceLastLongBreak
            Text(
                text = "Pomodoros until long break: $pomodorosUntilLongBreakDisplay",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text)
    }
}

/**
 * Formats milliseconds into a MM:SS string.
 */
private fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Handles the logic for transitioning between timer modes and updating statistics
 * when a timer segment completes.
 */
private fun handleTimerCompletion(
    currentMode: TimerMode,
    totalPomodoros: Int,
    pomodorosSinceBreak: Int,
    onStateChange: (newMode: TimerMode, newTotalPomodoros: Int, newPomodorosSinceLastLongBreak: Int) -> Unit
) {
    var newMode = currentMode
    var newTotalPomodoros = totalPomodoros
    var newPomodorosSinceLastLongBreak = pomodorosSinceBreak

    when (currentMode) {
        TimerMode.POMODORO -> {
            newTotalPomodoros++
            newPomodorosSinceLastLongBreak++
            if (newPomodorosSinceLastLongBreak >= POMODOROS_PER_LONG_BREAK) {
                newMode = TimerMode.LONG_BREAK
                newPomodorosSinceLastLongBreak = 0 // Reset for the next cycle of pomodoros
            } else {
                newMode = TimerMode.SHORT_BREAK
            }
        }
        TimerMode.SHORT_BREAK -> {
            newMode = TimerMode.POMODORO
        }
        TimerMode.LONG_BREAK -> {
            newMode = TimerMode.POMODORO
            // pomodorosSinceLastLongBreak is already reset to 0 when long break is chosen
        }
    }
    onStateChange(newMode, newTotalPomodoros, newPomodorosSinceLastLongBreak)
}