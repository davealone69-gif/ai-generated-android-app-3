package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.TimeUnit

// Constants for Pomodoro durations
private val POMODORO_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(25)
private val SHORT_BREAK_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(5)
private val LONG_BREAK_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(15)
private const val LONG_BREAK_INTERVAL = 4 // Take a long break after every N Pomodoros

/**
 * Represents the current state of the timer.
 */
enum class TimerState {
    IDLE, RUNNING, PAUSED
}

/**
 * Represents the type of the current session.
 */
enum class SessionType {
    POMODORO, SHORT_BREAK, LONG_BREAK
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Providing MaterialTheme for the entire app for proper styling
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen()
                }
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    // State variables for the Pomodoro timer logic
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var sessionType by remember { mutableStateOf(SessionType.POMODORO) }
    var timeRemainingMillis by remember { mutableStateOf(POMODORO_DURATION_MILLIS) }
    var totalPomodoroCompleted by remember { mutableStateOf(0) }
    var pomodorosInCycle by remember { mutableStateOf(0) } // Tracks Pomodoros for long break logic

    // Calculate the total duration for the current session type, used for progress calculation
    val initialTotalDurationMillis = remember(sessionType) {
        when (sessionType) {
            SessionType.POMODORO -> POMODORO_DURATION_MILLIS
            SessionType.SHORT_BREAK -> SHORT_BREAK_DURATION_MILLIS
            SessionType.LONG_BREAK -> LONG_BREAK_DURATION_MILLIS
        }
    }

    // LaunchedEffect to handle the timer countdown logic
    LaunchedEffect(timerState, sessionType) {
        // This effect will restart whenever timerState or sessionType changes.
        // It only runs when the timer is in the RUNNING state.
        if (timerState == TimerState.RUNNING) {
            // Loop while there's time remaining and the coroutine is active
            while (timeRemainingMillis > 0 && isActive) {
                delay(1000L) // Wait for 1 second before decrementing
                // Decrement time, ensuring it doesn't go below zero
                timeRemainingMillis = (timeRemainingMillis - 1000L).coerceAtLeast(0L)
            }

            // If the timer reached 0 and the effect wasn't cancelled (e.g., by pausing or resetting)
            if (timeRemainingMillis <= 0 && isActive) {
                when (sessionType) {
                    SessionType.POMODORO -> {
                        totalPomodoroCompleted++
                        pomodorosInCycle++
                        // Check if it's time for a long break
                        if (pomodorosInCycle % LONG_BREAK_INTERVAL == 0) {
                            sessionType = SessionType.LONG_BREAK
                            timeRemainingMillis = LONG_BREAK_DURATION_MILLIS
                        } else {
                            sessionType = SessionType.SHORT_BREAK
                            timeRemainingMillis = SHORT_BREAK_DURATION_MILLIS
                        }
                    }
                    // After a break, transition back to a Pomodoro session
                    SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                        sessionType = SessionType.POMODORO
                        timeRemainingMillis = POMODORO_DURATION_MILLIS
                    }
                }
                timerState = TimerState.IDLE // Automatically stop after session completion
            }
        }
    }

    // Main UI structure
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Display current session type
        Text(
            text = when (sessionType) {
                SessionType.POMODORO -> "Pomodoro Session"
                SessionType.SHORT_BREAK -> "Short Break"
                SessionType.LONG_BREAK -> "Long Break"
            },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Progress Ring and Timer Display
        PomodoroProgressRing(
            modifier = Modifier.size(250.dp),
            // Progress is calculated as elapsed time (0.0 to 1.0)
            progress = 1f - (timeRemainingMillis.toFloat() / initialTotalDurationMillis.toFloat()),
            timeRemainingMillis = timeRemainingMillis
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Control Buttons (Start/Pause and Reset)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start/Pause Button
            Button(
                onClick = {
                    timerState = when (timerState) {
                        TimerState.IDLE, TimerState.PAUSED -> TimerState.RUNNING
                        TimerState.RUNNING -> TimerState.PAUSED
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (timerState == TimerState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState == TimerState.RUNNING) "Pause" else "Start",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (timerState == TimerState.RUNNING) "Pause" else "Start",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Reset Button
            Button(
                onClick = {
                    timerState = TimerState.IDLE // Stop the timer
                    sessionType = SessionType.POMODORO // Reset to Pomodoro session
                    timeRemainingMillis = POMODORO_DURATION_MILLIS // Reset time to Pomodoro default
                    pomodorosInCycle = 0 // Reset cycle count for long break logic
                    // totalPomodoroCompleted is intentionally not reset, as it's a statistic.
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset",
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Statistics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Statistics",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pomodoros Completed: $totalPomodoroCompleted",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Display remaining Pomodoros until the next long break
                val remainingForLongBreak = LONG_BREAK_INTERVAL - (pomodorosInCycle % LONG_BREAK_INTERVAL)
                Text(
                    text = "Pomodoros in current cycle: $pomodorosInCycle " +
                            if (remainingForLongBreak == LONG_BREAK_INTERVAL) "(Long break after $LONG_BREAK_INTERVAL more)"
                            else "(Next long break after $remainingForLongBreak more)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PomodoroProgressRing(
    modifier: Modifier = Modifier,
    progress: Float, // Represents elapsed progress from 0.0 (start) to 1.0 (end)
    timeRemainingMillis: Long
) {
    // Animate the progress value for a smooth visual transition of the ring
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing) // Animates over 1 second
    ).value

    // Determine colors from the current MaterialTheme
    val ringColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx() // Convert DP to pixels for Canvas drawing
            val sweepAngle = animatedProgress * 360f // Calculate sweep angle based on animated progress

            // Draw the background circle (the track)
            drawCircle(
                color = backgroundColor,
                radius = size.minDimension / 2f,
                style = Stroke(width = strokeWidth)
            )

            // Draw the progress arc, starting from the top (-90 degrees)
            drawArc(
                color = ringColor,
                startAngle = -90f, // Start from the 12 o'clock position
                sweepAngle = sweepAngle,
                useCenter = false, // Do not connect ends to the center
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round) // Rounded ends for the arc
            )
        }

        // Display the time remaining in MM:SS format
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemainingMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemainingMillis) % 60
        Text(
            text = String.format("%02d:%02d", minutes, seconds), // Format to ensure two digits
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainAppScreen() {
    // Provide MaterialTheme and Surface for the preview to render correctly
    MaterialTheme {
        Surface {
            MainAppScreen()
        }
    }
}