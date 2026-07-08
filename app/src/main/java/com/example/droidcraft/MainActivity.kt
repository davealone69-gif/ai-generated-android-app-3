package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PomodoroTimerScreen()
                }
            }
        }
    }
}

@Composable
fun PomodoroTimerScreen() {
    val totalSeconds = 25 * 60
    var timeLeft by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableStateOf(0) }

    val progress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / totalSeconds,
        label = "progress"
    )

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (isRunning && timeLeft == 0) {
            isRunning = false
            completedSessions++
            timeLeft = totalSeconds
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pomodoro Timer",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "${(timeLeft / 60).toString().padStart(2, '0')}:${(timeLeft % 60).toString().padStart(2, '0')}",
                style = MaterialTheme.typography.displaySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Pause" else "Start")
            }
            OutlinedButton(onClick = {
                isRunning = false
                timeLeft = totalSeconds
            }) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Statistics", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Completed Sessions: $completedSessions")
            }
        }
    }
}