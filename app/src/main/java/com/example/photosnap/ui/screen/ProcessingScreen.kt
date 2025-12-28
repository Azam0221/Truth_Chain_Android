package com.example.photosnap.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photosnap.data.EvidenceRepository
import com.example.photosnap.network.RetrofitClient
import com.example.photosnap.room.DatabaseProvider
import com.example.photosnap.trustManager.TrustManager
import com.example.photosnap.ui.theme.DarkBackground
import com.example.photosnap.ui.theme.NeonGreen
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ProcessingScreen(
    onProcessingComplete: () -> Unit,
    imageBytes: ByteArray,
    metadata: String,
    signature: String
) {
    val context = LocalContext.current


    val repository = remember {
        EvidenceRepository(
            apiService = RetrofitClient.apiService,
            evidenceDao = DatabaseProvider.getDatabase(context).evidenceDao(),
            context = context
        )
    }

    var processingStage by remember { mutableStateOf("SECURING EVIDENCE") }
    var progress by remember { mutableFloatStateOf(0.0f) }

    // Animations setup...
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = ""
    )
    val randomHex = remember { mutableStateOf("0x...") }

    LaunchedEffect(Unit) {
        // 1. Start Animation
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 1500) {
            randomHex.value = "0x" + List(16) { Random.nextInt(0, 15).toString(16).uppercase() }.joinToString("")
            progress += 0.02f
            delay(50)
        }


        processingStage = "SIGNING & UPLOADING..."
        val publicKey = TrustManager.getPublicKey()


        repository.captureAndSecure(imageBytes, metadata, signature, publicKey)

        progress = 1.0f
        processingStage = "COMPLETE"
        delay(500)
        onProcessingComplete()
    }


    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(color = NeonGreen.copy(alpha = 0.2f), radius = size.minDimension / 2, style = Stroke(width = 4f))
            }
            Icon(Icons.Default.Lock, null, tint = NeonGreen, modifier = Modifier.size(80.dp).scale(scale))
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(processingStage, color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        Text(randomHex.value, color = Color.Gray, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(32.dp))
        LinearProgressIndicator(progress = progress, modifier = Modifier.width(200.dp).height(4.dp), color = NeonGreen, trackColor = Color.DarkGray)
    }
}