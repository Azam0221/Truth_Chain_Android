package com.example.photosnap.data

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.photosnap.network.ApiService
import com.example.photosnap.room.EvidenceDao
import com.example.photosnap.room.EvidenceEntity
import com.example.photosnap.trustManager.GalleryUtils
import com.example.photosnap.worker.UploadWorker
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.security.Signature

class EvidenceRepository (
    private val apiService: ApiService,
    private val evidenceDao: EvidenceDao,
    private val context: Context
){
    suspend fun captureAndSecure(
        imageBytes:ByteArray,
        metadata: String,
        signature: String, 
        publicKey: String
    ){

        GalleryUtils.saveImageToGallery(context, imageBytes)

        val file = File(context.filesDir,"evidence_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use{ it.write(imageBytes)}

        val entity = EvidenceEntity(
            imagePath = file.absolutePath,
            metaData = metadata,
            signature = signature,
            publicKey = publicKey,
            isSynced = false
        )

        val rowId = evidenceDao.insertEvidence(entity)

        try {

            uploadToServer(file,metadata,signature,publicKey)

            val updatedEntity = entity.copy(id = rowId.toInt(), isSynced = true)
            evidenceDao.updateEvidence(updatedEntity)


        } catch (e:Exception){
            e.printStackTrace()
            val uploadWorker = OneTimeWorkRequestBuilder<UploadWorker>().build()
            WorkManager.getInstance(context).enqueue(uploadWorker)
        }


    }

    private suspend fun uploadToServer(file:File, metadata: String, signature: String, publicKey: String){

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)


        val metaBody = metadata.toRequestBody("text/plain".toMediaTypeOrNull())
        val sigBody = signature.toRequestBody("text/plain".toMediaTypeOrNull())
        val keyBody = publicKey.toRequestBody("text/plain".toMediaTypeOrNull())


        val response = apiService.uploadEvidence(body, metaBody, sigBody, keyBody)
        if (!response.isSuccessful) {
            throw Exception("Upload Failed: ${response.code()}")
        }
    }

}