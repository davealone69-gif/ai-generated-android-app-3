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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PomodoroScreen()
                }
            }
        }
    }
}

@Composable
fun PomodoroScreen() {
    val totalTime = 25 * 60L
    var timeLeft by remember { mutableStateOf(totalTime) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionsCompleted by remember { mutableStateOf(0) }
    
    val progress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / totalTime.toFloat(),
        animationSpec = androidx.compose.animation.core.ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progress"
    )

    LaunchedEffect(key1 = isRunning, key2 = timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (timeLeft == 0L && isRunning) {
            isRunning = false
            sessionsCompleted++
            timeLeft = totalTime
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pomodoro Timer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.displayMedium
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Pause" else "Start")
            }
            OutlinedButton(onClick = { 
                isRunning = false
                timeLeft = totalTime 
            }) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Statistics", style = MaterialTheme.typography.titleMedium)
                Text("Completed Sessions: $sessionsCompleted", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}