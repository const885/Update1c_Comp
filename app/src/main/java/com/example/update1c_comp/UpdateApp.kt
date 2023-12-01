package com.example.update1c_comp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Environment

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider

import java.io.File


class UpdateApp {
    private var mContext: Context? = null
    fun setContext(context: Context) {
        mContext = context
    }

    fun showDialog(context: Context) {
        // Запуск диалога
    }

    fun hideDialog() {
        // Закрытие диалога
    }

    // ... остальной код

@Composable
fun MyApp(updateApp: UpdateApp) {
    var isDialogVisible by remember { mutableStateOf(false) }
    var mContext: Context? = null
    if (isDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                isDialogVisible = false
                updateApp.hideDialog()
            },
            title = { Text("Please wait...") },
            confirmButton = {
                TextButton(onClick = {
                    isDialogVisible = false
                    updateApp.hideDialog()
                }) {
                    Text("OK")
                }
            }
        )
    }
 }
    fun doInBackground() {
        // Your background task here
    }

    fun getDownloadPath(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Для Android 10 и новее, используем Scoped Storage API
            //context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: ""
            context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk"
        } else {
            // Для более ранних версий Android
           // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk"
        }
      }
    fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val isDeleted = file.delete()
            if (isDeleted) {
                println("Файл успешно удален.")
            } else {
                println("Не удалось удалить файл.")
            }
        } else {
            println("Файл не найден.")
        }
    }

    fun installApk() {
        try {
            val PATH = getDownloadPath(mContext!!)
           // val file = File("$PATH/ru.dkn.ms-arm.apk")
            val file = File("$PATH")
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= 24) {
                val downloadedApk = FileProvider.getUriForFile(
                    mContext!!,
                    "${mContext!!.packageName}.provider",
                    file
                )
                intent.setDataAndType(downloadedApk, "application/vnd.android.package-archive")
                val resInfoList: List<ResolveInfo> =
                    mContext!!.packageManager.queryIntentActivities(
                        intent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                for (resolveInfo in resInfoList) {
                    mContext!!.grantUriPermission(
                        "${mContext!!.packageName}.provider",
                        downloadedApk,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                mContext!!.startActivity(intent)
            } else {
                intent.setAction(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                mContext!!.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}