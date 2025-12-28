package com.example.photosnap.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.example.photosnap.room.DatabaseProvider
import com.example.photosnap.room.EvidenceEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

class HomeViewModel(application: Application): AndroidViewModel(application){

    private val dao = DatabaseProvider.getDatabase(application).evidenceDao()

    val verifiedList: Flow<List<EvidenceEntity>> = dao.getVerifiedEvidence()
    val pendingList: Flow<List<EvidenceEntity>> = dao.getPendingEvidence()

    fun openFile(context: Context, filePath: String){
        val file = File(filePath)
        if(file.exists()){
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }
}