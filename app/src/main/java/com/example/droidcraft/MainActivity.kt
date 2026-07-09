package com.example.droidcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
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
    var sessionsCompleted by remember { mutableIntStateOf(0) }

    val progress = timeLeft.toFloat() / totalTime.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft -= 1000L
            }
            if (timeLeft <= 0L) {
                sessionsCompleted++
                timeLeft = totalTime
                isRunning = false
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Pomodoro Timer", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    drawCircle(Color.LightGray, style = Stroke(width = 20f))
                    drawArc(
                        color = Color(0xFF6200EE),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 20f, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "%02d:%02d".format(timeLeft / 60000, (timeLeft % 60000) / 1000),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { isRunning = !isRunning }) {
                    Text(if (isRunning) "Pause" else "Start")
                }
                Button(onClick = { 
                    isRunning = false
                    timeLeft = totalTime 
                }) {
                    Text("Reset")
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Statistics", fontWeight = FontWeight.Bold)
                    Text(text = "Sessions completed: $sessionsCompleted")
                }
            }
        }
    }
}