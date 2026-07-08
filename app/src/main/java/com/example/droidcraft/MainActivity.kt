package com.example.droidcraft

import android.os.Bundle
import android.os.CountDownTimer
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
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PomodoroApp()
                }
            }
        }
    }
}

@Composable
fun PomodoroApp() {
    val totalTime = 25 * 60 * 1000L
    var timeLeft by remember { mutableLongStateOf(totalTime) }
    var isRunning by remember { mutableStateOf(false) }
    var pomodorosCompleted by remember { mutableIntStateOf(0) }

    val progress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / totalTime.toFloat(),
        label = "progress"
    )

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val timer = object : CountDownTimer(timeLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished
                }
                override fun onFinish() {
                    isRunning = false
                    timeLeft = totalTime
                    pomodorosCompleted++
                }
            }
            timer.start()
            // Cleanup when the effect is cancelled or isRunning becomes false
            onDispose { timer.cancel() }
        }
    }

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
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", timeLeft / 60000, (timeLeft % 60000) / 1000),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(48.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Statistics", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Pomodoros completed today: $pomodorosCompleted")
            }
        }
    }
}