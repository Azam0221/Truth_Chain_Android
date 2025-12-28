package com.example.photosnap.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.photosnap.network.RetrofitClient
import com.example.photosnap.room.DatabaseProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class UploadWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams){

    override suspend fun doWork(): Result {

        val dao = DatabaseProvider.getDatabase(applicationContext).evidenceDao()
        val api = RetrofitClient.apiService

        val pendingList = dao.getAllPendingList()

        if (pendingList.isEmpty()) return Result.success()

        return try {
            pendingList.forEach { evidence->
                val file = File(evidence.imagePath)

                if(file.exists()){
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    val metaBody = evidence.metaData.toRequestBody("text/plain".toMediaTypeOrNull())
                    val sigBody = evidence.signature.toRequestBody("text/plain".toMediaTypeOrNull())
                    val keyBody = evidence.publicKey.toRequestBody("text/plain".toMediaTypeOrNull())

                    val response = api.uploadEvidence(body, metaBody, sigBody, keyBody)

                    if(response.isSuccessful){
                        val updatedEvidence = evidence.copy(isSynced = true)
                        dao.updateEvidence(updatedEvidence)
                    }

                }
                else{
                    dao.deleteEvidence(evidence)
                }
            }

            Result.success()
        }
        catch (e:Exception){
            Result.retry()
        }
    }
} 