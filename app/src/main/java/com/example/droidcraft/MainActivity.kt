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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PomodoroTimerApp()
                }
            }
        }
    }
}

@Composable
fun PomodoroTimerApp() {
    val totalTimeMs = 25 * 60 * 1000L
    var timeLeft by remember { mutableLongStateOf(totalTimeMs) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionsCompleted by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1000L
        } else if (timeLeft <= 0L && isRunning) {
            isRunning = false
            timeLeft = totalTimeMs
            sessionsCompleted++
        }
    }

    val progress = timeLeft.toFloat() / totalTimeMs.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pomodoro Timer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", (timeLeft / 60000), (timeLeft / 1000) % 60),
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
                timeLeft = totalTimeMs 
            }) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Sessions completed today: $sessionsCompleted")
            }
        }
    }
}