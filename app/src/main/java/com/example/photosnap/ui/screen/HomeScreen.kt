package com.example.photosnap.ui.screen

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photosnap.room.EvidenceEntity
import com.example.photosnap.ui.theme.CardDark
import com.example.photosnap.ui.theme.DarkBackground
import com.example.photosnap.ui.theme.NeonGreen
import com.example.photosnap.viewModel.HomeViewModel

@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(application) as T
            }
        }
    )

    // Collecting the Live Data from Room DB
    val verifiedItems by viewModel.verifiedList.collectAsState(initial = emptyList())
    val pendingItems by viewModel.pendingList.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCameraClick,
                containerColor = NeonGreen,
                contentColor = Color.Black,
                modifier = Modifier.size(80.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Lock, "Capture", Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).padding(top = 12.dp)
        ) {
            // Header
            Text("TruthChain", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Cryptographic Evidence Recorder", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // 1. PENDING UPLOADS (Only show if list is not empty)
            if (pendingItems.isNotEmpty()) {
                Text(" Pending Uploads", color = Color.Yellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(0.4f)) { // Takes up to 40% of screen
                    items(pendingItems) { item ->
                        EvidenceItemCard(
                            item = item,
                            isVerified = false,
                            onClick = { viewModel.openFile(context, item.imagePath) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. VERIFIED EVIDENCE
            Text("Verified Blockchain Evidence", color = NeonGreen, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) { // Takes remaining space
                items(verifiedItems) { item ->
                    EvidenceItemCard(
                        item = item,
                        isVerified = true,
                        onClick = { viewModel.openFile(context, item.imagePath) }
                    )
                }
            }
        }
    }
}

@Composable
fun EvidenceItemCard(
    item: EvidenceEntity,
    isVerified: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Evidence #${item.id}", color = Color.White, fontWeight = FontWeight.Bold)
                // Show lat/long preview from metadata
                Text(
                    text = if(item.metaData.length > 25) item.metaData.take(25) + "..." else item.metaData,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }


            Surface(
                color = if (isVerified) NeonGreen.copy(alpha = 0.1f) else Color.Yellow.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 6.dp, end = 6.dp)) {
                    if(!isVerified) Icon(Icons.Default.Refresh, null, tint = Color.Yellow, modifier = Modifier.size(12.dp))
                    Text(
                        text = if (isVerified) "VERIFIED" else "WAITING...",
                        color = if (isVerified) NeonGreen else Color.Yellow,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) 
                }
            }
        }
    }
}