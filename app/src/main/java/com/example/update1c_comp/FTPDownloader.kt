package com.example.update1c_comp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File

class FTPDownloader {
    private val TAG = "FTPDownloader"
    suspend fun downloadFileAsync(
        ftpAddress: String,
        username: String,
        password: String,
        filePath: String,
        destinationPath: String,
        progressCallback: (Int) -> Unit
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val ftpClient = FTPClient()
            var success = false
            try {
                ftpClient.connect(ftpAddress)
                ftpClient.login(username, password)
                ftpClient.enterLocalPassiveMode()
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

                val outputStream = File(destinationPath).outputStream()
                val file = File(destinationPath)
                val fileSize = ftpClient.listFiles(filePath).firstOrNull()?.size ?: -1

                val bufferSize = 1024
                var downloaded: Long = 0
                val inputStream = ftpClient.retrieveFileStream(filePath)
                Log.e(TAG, "buffer: ${bufferSize}")
                val buffer = ByteArray(bufferSize)
                var bytesCount: Int
//                try {
                while (true) {
                    Log.e(TAG, "About to read from input stream")
                    val bytesRead = inputStream.read(buffer)
                    Log.e(TAG, "Read $bytesRead bytes from input stream")

                    if (bytesRead == -1) {
                        Log.e(TAG, "End of input stream reached")
                        break
                    }

                    bytesCount = bytesRead
                    Log.e(TAG, "About to write to output stream")
                    outputStream.write(buffer, 0, bytesCount)
                    Log.e(TAG, "Wrote $bytesCount bytes to output stream")

                    downloaded += bytesCount
                    val progress = ((downloaded.toDouble() / fileSize) * 100).toInt()
                    Log.e(TAG, "Progress: $progress")
                    progressCallback(progress)
                }
//                } finally {
//                    inputStream?.close()
//                    outputStream?.close()
//                }

                success = ftpClient.completePendingCommand() && file.length() > 0
                inputStream.close()
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (ftpClient.isConnected) {
                    ftpClient.disconnect()
                }
            }
            success
        }
    }
}
