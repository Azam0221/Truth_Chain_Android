package com.example.photosnap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.photosnap.data.CaptureHolder
import com.example.photosnap.ui.screen.CameraScreen
import com.example.photosnap.ui.screen.HomeScreen
import com.example.photosnap.ui.screen.ProcessingScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    )
    {
        composable("home") {
            HomeScreen(
                onCameraClick = { navController.navigate("camera") }
            )
        }

        composable("camera"){
            CameraScreen(
                onCaptureSuccess = { bytes, metadata, signature ->

                    CaptureHolder.imageBytes = bytes
                    CaptureHolder.metadata = metadata
                    CaptureHolder.signature = signature

                    navController.navigate("processing")
                }
            )
        }

        composable("processing") {

            val bytes = CaptureHolder.imageBytes ?: ByteArray(0)
            val meta = CaptureHolder.metadata
            val sig = CaptureHolder.signature

            ProcessingScreen(
                imageBytes = bytes,
                metadata = meta,
                signature = sig,
                onProcessingComplete = {

                    CaptureHolder.clear()

                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}