package com.example.photosnap.ui.screen

import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import androidx.camera.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.photosnap.sensor.SensorHelper
import com.example.photosnap.trustManager.TrustManager
import com.example.photosnap.utils.LuminosityAnalyzer
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.Executors


@Composable
fun CameraScreen(

    onCaptureSuccess: (
            imageBytes: ByteArray,
            metaData: String,
            signature: String
            ) -> Unit
)  {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // A background thread for the math (Required for ImageAnalysis)
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val sensorHelper = remember { SensorHelper(context)}

    DisposableEffect(Unit){
        sensorHelper.startListening()
        onDispose {
            sensorHelper.stopListening()
        }
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()){

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(640, 480)) // Low Res for Battery
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor, LuminosityAnalyzer { lux ->
                        // PASS DATA TO SENSOR HELPER
                        sensorHelper.updateLightFromCamera(lux)
                    })

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("Camera", "Binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
                },
            modifier = Modifier.fillMaxSize()
            )

        Button(
            onClick = {
                val capture = imageCapture ?: return@Button

                capture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            scope.launch {
                                // A. Get Image Bytes
                                val buffer: ByteBuffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)

                                // B. Get Metadata (Location)

                                val rawLocation = TrustManager.getWitnessData(context)
                                var lat = "0.0"
                                var long = "0.0"

                                try {
                                    val parts = rawLocation.split(",")
                                    if (parts.isNotEmpty()) lat = parts[0].trim()
                                    if (parts.size > 1) long = parts[1].trim()
                                } catch (e: Exception) {
                                    Log.e("TruthChain", "Location Parse Error", e)
                                }

                                val smartMetadata = sensorHelper.getEnrichedMetadata(lat, long)

                                Log.d("TruthChain", "Captured Metadata: $smartMetadata")


                                val combinedData = bytes + smartMetadata.toByteArray(Charsets.UTF_8)

                                val signature = TrustManager.signData(combinedData)


                                onCaptureSuccess(bytes, smartMetadata, signature)

                                image.close()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("Camera", "Capture failed: ${exception.message}")
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text("SECURE CAPTURE")
        }
    }

}