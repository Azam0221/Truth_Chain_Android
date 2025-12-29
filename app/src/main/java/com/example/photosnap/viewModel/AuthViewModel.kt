package com.example.photosnap.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.photosnap.network.RetrofitClient
import com.example.photosnap.trustManager.TrustManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val prefs = application.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    init {
        checkProvisioningStatus()
    }

    private fun checkProvisioningStatus() {
        if (prefs.getBoolean("is_provisioned", false)) {
            _authState.value = AuthState.Success
        }
    }

    fun provisionDevice(badgeId: String, otp: String) {
        if (badgeId.isBlank() || otp.isBlank()) {
            _authState.value = AuthState.Error("Please enter Badge ID and Token")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                TrustManager.signData("init".toByteArray())
                val pubKey = TrustManager.getPublicKey()


                val response = RetrofitClient.apiService.registerDevice(pubKey, badgeId, otp)

                if (response.isSuccessful) {

                    prefs.edit().putBoolean("is_provisioned", true).apply()
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Invalid Credentials"
                    _authState.value = AuthState.Error("Provisioning Failed: $errorMsg")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network Error: ${e.message}")
            }
        }
    }
}