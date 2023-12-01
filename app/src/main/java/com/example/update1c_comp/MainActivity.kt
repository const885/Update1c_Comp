package com.example.update1c_comp

import android.app.Activity
import android.app.AppOpsManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.update1c_comp.ui.theme.Update1c_CompTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Update1c_CompTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //Greeting("Android")
                    FileDownloadScreen()
                }
            }
        }
    }
}


class MediaStoreUtil {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveFileToMediaStore(
        context: Context,
        file: File,
        displayName: String,
        mimeType: String
    ): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        var uri: Uri? = null
        try {
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val insertUri = contentResolver.insert(collection, contentValues)

            insertUri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                uri = insertUri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uri
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDownloadScreen() {
    var usernameHttp by remember { mutableStateOf("obmen") }
    var passwordHttp by remember { mutableStateOf("1_superobmen") }
    var usernameFtp by remember { mutableStateOf("ClientFTP") }
    var passwordFtp by remember { mutableStateOf("13461") }
    var ftpAddress by remember { mutableStateOf("145.255.25.109") }
    var downloading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var savedFileUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val ftpDownloader = FTPDownloader()
    val httpDownloader = HTTPDownloader()
    val mediaStoreUtil = MediaStoreUtil() // Создаем экземпляр MediaStoreUtil
    val updateApp = UpdateApp()

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(10.dp)
    ) {

           TextField(
               value = usernameFtp,
               onValueChange = { usernameFtp = it },
               label = { Text("UsernameFtp") },modifier = Modifier.height(50.dp),
               textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
           )
           TextField(
               value = passwordFtp,
               onValueChange = { passwordFtp = it },
               label = { Text("PasswordFtp") },modifier = Modifier.height(50.dp),
               textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
           )
          TextField(
            value = usernameHttp,
            onValueChange = { usernameHttp = it },
            label = { Text("UsernameHttp") },modifier = Modifier.height(50.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
        )
          TextField(
            value = passwordHttp,
            onValueChange = { passwordHttp = it },
            label = { Text("PasswordHttp") },modifier = Modifier.height(50.dp),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
        )
          TextField(
            value = ftpAddress,
            onValueChange = { ftpAddress = it },
            label = { Text("FTP Address") },modifier = Modifier.height(50.dp),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
        )
        // Input fields for username, password, and FTP address

        Spacer(modifier = Modifier.height(10.dp))
        ////
        Button(
            onClick = {
                downloading = true
                scope.launch {
                        updateApp.deleteFile(context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk")
                        success = httpDownloader.downloadFile(
                        usernameHttp,
                        passwordHttp,
                        "/MobileScan/ru.dkn.ms-arm.apk",
                        context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk"
                    ) { progress ->
                        downloadProgress = progress
                    }
                    if (success) {
//                        savedFileUri = mediaStoreUtil.saveFileToMediaStore(
//                            context,
//                            File(context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk"),
//                            "ru.dkn.ms-arm",
//                            "application/vnd.android.package-archive"
//                        )
                        ///запрос Разрешения доступа установки
                    }
                    downloading = false
                }
            },
            enabled = !downloading
        ) {
            Text("Download HTTP")
        }
        Spacer(modifier = Modifier.height(10.dp))
        ///
        Button(
            onClick = {
                downloading = true
                scope.launch {
                    updateApp.deleteFile(context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk")
                        success = ftpDownloader.downloadFileAsync(
                        ftpAddress,
                        usernameFtp,
                        passwordFtp,
                        "/MobileScan/ru.dkn.ms-arm.apk",
                        context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk"
                    ) { progress ->
                        downloadProgress = progress
                    }
                    if (success) {
//                        savedFileUri = mediaStoreUtil.saveFileToMediaStore(
//                            context,
//                            File(context.filesDir.absolutePath + "/ru.dkn.ms-arm.apk"),
//                            "ru.dkn.ms-arm",
//                            "application/vnd.android.package-archive"
//                        )
                        ///запрос Разрешения доступа установки

                        //////////
                    }
                    downloading = false
                }
            },
            enabled = !downloading
        ) {
            Text("Download FTP")
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 300.dp, height = 90.dp)
                .padding(vertical = 20.dp)
        ) {
           if ( downloading ) {
               LinearProgressIndicator(
                   progress = downloadProgress / 100f,
                   color = Color.Blue,
                   modifier = Modifier
                       .fillMaxWidth()
                       .height(30.dp)  // Увеличьте это значение для увеличения толщины полоски прогресса
               )
            //   Spacer(modifier = Modifier.height(20.dp))
               Text(text = "${(downloadProgress).toInt()}%", color = Color.Yellow)
           }
        }
//        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            val packageURI = Uri.parse("package:" + "ru.dkn.ms")
            val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
            context.startActivity(uninstallIntent)

        }) {
            Text("Удалить")
        }
//        savedFileUri?.let
        if (success) {
            if (!downloading) {
                if (!hasUsageStatsPermission(context)) {
                    val intentPerm = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intentPerm)
                }
                /////Запуск файла
                updateApp.setContext(context as Activity)
                updateApp.installApk()
            }
            // Additional operations with the saved file URI
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun InstallA(success: Boolean) {
    val context = LocalContext.current
    var buttonText by remember { mutableStateOf("Download") }
    var progressText by remember { mutableStateOf("Download failed!") }

    LaunchedEffect(success) {
        if (success) {
            progressText = "Download complete!"
            buttonText = "Update"
            var updateApp = UpdateApp()
            updateApp.setContext(context as Activity)
            updateApp.installApk()
        } //else {
//            progressText = "Download failed!"
//            buttonText = "Retry"
//        }
    }

//    Column {
//        Text(text = progressText)
//        Button(onClick = { /* Handle download button click */ }) {
//            Text(text = buttonText)
//        }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
fun PreviewAppContent() {
    MaterialTheme {
        FileDownloadScreen()
    }
}