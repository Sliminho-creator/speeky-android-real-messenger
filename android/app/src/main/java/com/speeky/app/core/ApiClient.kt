package com.speeky.app.core

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {
    const val BASE_URL = "http://10.0.2.2:4000/"
    val okHttp = OkHttpClient.Builder().build()
    fun authedRequest(path: String, token: String): Request.Builder = Request.Builder().url(BASE_URL + path.removePrefix("/")).addHeader("Authorization", "Bearer $token")
    fun jsonBody(raw: String) = raw.toRequestBody("application/json".toMediaType())
}
