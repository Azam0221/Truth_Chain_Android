package com.example.photosnap.ui.screen


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photosnap.viewModel.AuthState
import com.example.photosnap.viewModel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var badgeId by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                Toast.makeText(context, (state as AuthState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TRUTHCHAIN", color = Color.Green, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("DEVICE PROVISIONING", color = Color.White, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = badgeId,
            onValueChange = { badgeId = it },
            label = { Text("Badge ID") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.Green,
                unfocusedLabelColor = Color.Gray,
                focusedBorderColor = Color.Green,
                unfocusedBorderColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("One-Time Token (OTP)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.Green,
                unfocusedLabelColor = Color.Gray,
                focusedBorderColor = Color.Green,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.provisionDevice(badgeId, otp) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            enabled = state !is AuthState.Loading
        ) {
            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
            } else {
                Text("ACTIVATE DEVICE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if(state is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text((state as AuthState.Error).message, color = Color.Red, fontSize = 12.sp)
        }
    }
}