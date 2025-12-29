package com.example.photosnap.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("/api/evidence/upload")
    suspend fun uploadEvidence(
        @Part image: MultipartBody.Part,
        @Part("metadata") metadata: RequestBody,
        @Part("signature") signature: RequestBody,
        @Part("publicKey") publicKey: RequestBody
    ): Response<Void>

    @FormUrlEncoded
    @POST("/api/auth/register")
    suspend fun registerDevice(
        @Field("publicKey") publicKey: String,
        @Field("deviceId") deviceId: String,
        @Field("otp") otp: String
    ): Response<String>
}