package com.example.update1c_comp


import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64


class HTTPDownloader {
    private val URL = "http://145.255.25.109/TradeN//hs/ft/FileTransfer/"
    private val USERNAME = "obmen"
    private val PASSWORD = "1_superobmen"
    private val TAG = "HTTPDownloader"

    private fun createJsonRequest(file: String, catalog: String, uidUser: String): String? {
        return try {
            val jsonRequest = JSONObject()
            jsonRequest.put("method", "GetFile")
            val params = JSONObject()
            params.put("File", file)
            params.put("Catalog", catalog)
            params.put("UID_USER", uidUser)
            jsonRequest.put("params", params)
            jsonRequest.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    fun getDownloadPath(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Для Android 10 и новее, используем Scoped Storage API
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: ""
        } else {
            // Для более ранних версий Android
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        }
    }


suspend  fun downloadFile(USERNAME:String,
                              PASSWORD:String,
                              filePath: String,
                              destinationPath: String,
                              progressCallback: (Int) -> Unit
    ):Boolean {
        return withContext(Dispatchers.IO) {
            var success = false
            val client = OkHttpClient.Builder()
                .authenticator(object : Authenticator {
                    @RequiresApi(Build.VERSION_CODES.O)
                    @Throws(IOException::class)
                    override fun authenticate(route: Route?, response: Response): Request {
                        val credentials =  USERNAME+":"+PASSWORD
                        val base64Credentials =
                            Base64.getEncoder().encodeToString(credentials.toByteArray())
                        return response.request.newBuilder()
                            .header("Authorization", "Basic $base64Credentials")
                            .build()
                    }
                })
                .build()

            val JSONMediaType = "application/json".toMediaType()

            val jsonRequest = createJsonRequest(
                "ru.dkn.ms-arm.apk",
                "\\\\Serv2014\\Obmen\\MobileScan\\",
                "00000000-0000-0000-0000-000000000000"
            )
            val requestBody = jsonRequest?.toRequestBody("application/json".toMediaTypeOrNull())

            val request = requestBody?.let {
                Request.Builder()
                    .url(URL)
                    .post(it)
                    .build()
            }

            if (request != null) {
                ////////////////////////////////
                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body
                        if (responseBody != null) {
                            val totalBytes = responseBody.contentLength()
                            var downloadedBytes: Long = 0
                            val buffer = ByteArray(1024)
                            var bytesRead: Int
                            val PATH = "/storage/emulated/0/"
//                            val file = File(PATH + "/ru.dkn.ms-arm.apk")
//                            val outputStream = FileOutputStream(file)
                            val outputStream = File(destinationPath).outputStream()
                            val file = File(destinationPath)
                            while (responseBody.byteStream().read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead.toLong()
                                val progress = (downloadedBytes * 100 / totalBytes).toInt()
                                progressCallback(progress)
                            }
                            success = (totalBytes.toInt().toLong() == downloadedBytes)
                            Log.e(TAG, "totalBytes: ${totalBytes}")
                            Log.e(TAG, "downloadedBytes: ${downloadedBytes}")
                            outputStream.close()
                            responseBody.close()
                            //updateDownloadStatus(true)
                        }
                    } else {
                       // updateDownloadStatus(false)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    //updateDownloadStatus(false)
                }


                /////////////////////////////////
            }
            success
        }

    }

}