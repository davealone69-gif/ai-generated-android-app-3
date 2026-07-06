package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFEF5350),
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    onPrimary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PomodoroApp()
                }
            }
        }
    }
}

enum class TimerMode(val displayName: String, val defaultMinutes: Int, val primaryColor: Color) {
    WORK("Work", 25, Color(0xFFEF5350)),
    SHORT_BREAK("Short Break", 5, Color(0xFF66BB6A)),
    LONG_BREAK("Long Break", 15, Color(0xFF42A5F5))
}

@Composable
fun PomodoroApp() {
    // Current state configurations
    var currentMode by rememberSaveable { mutableStateOf(TimerMode.WORK) }
    
    // Custom durations managed in minutes
    var workDurationSetting by rememberSaveable { mutableStateOf(25) }
    var shortBreakDurationSetting by rememberSaveable { mutableStateOf(5) }
    var longBreakDurationSetting by rememberSaveable { mutableStateOf(15) }

    // Dynamic current max duration
    val currentMaxDurationSeconds = remember(currentMode, workDurationSetting, shortBreakDurationSetting, longBreakDurationSetting) {
        when (currentMode) {
            TimerMode.WORK -> workDurationSetting * 60
            TimerMode.SHORT_BREAK -> shortBreakDurationSetting * 60
            TimerMode.LONG_BREAK -> longBreakDurationSetting * 60
        }
    }

    // Active timer tracking
    var secondsLeft by rememberSaveable { mutableStateOf(currentMaxDurationSeconds) }
    var isTimerRunning by rememberSaveable { mutableStateOf(false) }

    // Synchronize secondsLeft if max configuration or mode changes while timer isn't running
    LaunchedEffect(currentMaxDurationSeconds) {
        if (!isTimerRunning) {
            secondsLeft = currentMaxDurationSeconds
        }
    }

    // Statistics state
    var completedWorkSessions by rememberSaveable { mutableStateOf(0) }
    var completedShortBreaks by rememberSaveable { mutableStateOf(0) }
    var completedLongBreaks by rememberSaveable { mutableStateOf(0) }
    var totalMinutesFocused by rememberSaveable { mutableStateOf(0) }

    // Ticking Timer Engine
    LaunchedEffect(isTimerRunning, secondsLeft) {
        if (isTimerRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        } else if (isTimerRunning && secondsLeft == 0) {
            isTimerRunning = false
            // Record stats completion
            when (currentMode) {
                TimerMode.WORK -> {
                    completedWorkSessions++
                    totalMinutesFocused += workDurationSetting
                }
                TimerMode.SHORT_BREAK -> {
                    completedShortBreaks++
                }
                TimerMode.LONG_BREAK -> {
                    completedLongBreaks++
                }
            }
            // Transition warning or auto-reset state
            secondsLeft = currentMaxDurationSeconds
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "Droid Pomodoro",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.5.sp
            ),
            color = currentMode.primaryColor,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Text(
            text = "Boost your daily productivity safely",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Segmented Mode Chooser
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimerMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                val targetBgColor = if (isSelected) mode.primaryColor else Color.Transparent
                val targetContentColor = if (isSelected) Color.White else Color.Gray

                Button(
                    onClick = {
                        isTimerRunning = false
                        currentMode = mode
                        secondsLeft = when (mode) {
                            TimerMode.WORK -> workDurationSetting * 60
                            TimerMode.SHORT_BREAK -> shortBreakDurationSetting * 60
                            TimerMode.LONG_BREAK -> longBreakDurationSetting * 60
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = targetBgColor,
                        contentColor = targetContentColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Ring Section
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            val progressFraction = if (currentMaxDurationSeconds > 0) {
                secondsLeft.toFloat() / currentMaxDurationSeconds.toFloat()
            } else {
                1f
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                // Track arc
                drawCircle(
                    color = Color(0xFF2E2E2E),
                    radius = size.minDimension / 2 - strokeWidth,
                    style = Stroke(width = strokeWidth)
                )
                // Remaining active progress arc
                drawArc(
                    color = currentMode.primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progressFraction,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central time layout
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val displayMin = secondsLeft / 60
                val displaySec = secondsLeft % 60
                val formattedTime = String.format("%02d:%02d", displayMin, displaySec)

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentMode.displayName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    ),
                    color = currentMode.primaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Core Controls (Play, Pause, Reset, Skip)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset / Skip back Button
            IconButton(
                onClick = {
                    isTimerRunning = false
                    secondsLeft = currentMaxDurationSeconds
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF262626), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Play / Pause central CTA
            Button(
                onClick = { isTimerRunning = !isTimerRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentMode.primaryColor,
                    contentColor = Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Start/Pause Timer",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Fast forward or skip to full time button
            IconButton(
                onClick = {
                    // Quick skip helper (triggers state completion safely)
                    secondsLeft = 0
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF262626), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, // Simulates forward action
                    contentDescription = "Complete Session Instantly",
                    tint = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Custom Duration Adjusters inside collapsible setting cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Adjust Durations (minutes)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Adjust Work Setting Row
                DurationAdjustmentRow(
                    label = "Work Session",
                    minutes = workDurationSetting,
                    onIncrease = {
                        if (workDurationSetting < 90) workDurationSetting++
                    },
                    onDecrease = {
                        if (workDurationSetting > 1) workDurationSetting--
                    },
                    accentColor = TimerMode.WORK.primaryColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Adjust Short Break Setting Row
                DurationAdjustmentRow(
                    label = "Short Break",
                    minutes = shortBreakDurationSetting,
                    onIncrease = {
                        if (shortBreakDurationSetting < 45) shortBreakDurationSetting++
                    },
                    onDecrease = {
                        if (shortBreakDurationSetting > 1) shortBreakDurationSetting--
                    },
                    accentColor = TimerMode.SHORT_BREAK.primaryColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Adjust Long Break Setting Row
                DurationAdjustmentRow(
                    label = "Long Break",
                    minutes = longBreakDurationSetting,
                    onIncrease = {
                        if (longBreakDurationSetting < 60) longBreakDurationSetting++
                    },
                    onDecrease = {
                        if (longBreakDurationSetting > 1) longBreakDurationSetting--
                    },
                    accentColor = TimerMode.LONG_BREAK.primaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            completedWorkSessions = 0
                            completedShortBreaks = 0
                            completedLongBreaks = 0
                            totalMinutesFocused = 0
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Reset Stats",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(
                        value = completedWorkSessions.toString(),
                        label = "Focus Session",
                        icon = Icons.Default.CheckCircle,
                        color = TimerMode.WORK.primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatBox(
                        value = (completedShortBreaks + completedLongBreaks).toString(),
                        label = "Breaks Done",
                        icon = Icons.Default.Check,
                        color = TimerMode.SHORT_BREAK.primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatBox(
                        value = "${totalMinutesFocused}m",
                        label = "Total Focus",
                        icon = Icons.Default.Star,
                        color = TimerMode.LONG_BREAK.primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun DurationAdjustmentRow(
    label: String,
    minutes: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2B2B2B), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF3A3A3A), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Decrease",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "$minutes min",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF3A3A3A), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StatBox(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF262626), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}