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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

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
    val totalTime = 25 * 60 * 1000L
    var timeLeft by remember { mutableStateOf(totalTime) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionsCompleted by remember { mutableStateOf(0) }

    val progress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / totalTime.toFloat(),
        animationSpec = androidx.compose.animation.core.ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progress"
    )

    DisposableEffect(isRunning) {
        var timer: CountDownTimer? = null
        if (isRunning) {
            timer = object : CountDownTimer(timeLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished
                }
                override fun onFinish() {
                    isRunning = false
                    timeLeft = totalTime
                    sessionsCompleted++
                }
            }.start()
        }
        onDispose { timer?.cancel() }
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
                strokeWidth = 8.dp
            )
            val minutes = (timeLeft / 1000) / 60
            val seconds = (timeLeft / 1000) % 60
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
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
                Text("Statistics", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Completed Sessions: $sessionsCompleted")
            }
        }
    }
}