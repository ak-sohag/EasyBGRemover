package com.akSohag.easybgremover.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akSohag.easybgremover.ui.theme.AppTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate


@Composable
fun MLKitModuleInstaller(context: Context, onReady: (Boolean) -> Unit) {
    var progress by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("Checking ML Kit module...") }
    var isModuleReady by remember { mutableStateOf(false) }
    val moduleInstallClient: ModuleInstallClient = remember { ModuleInstall.getClient(context) }

    LaunchedEffect(Unit) {
        val request = ModuleInstallRequest.newBuilder()
            .addApi(ImageSegmentationHelper.segmenter)
            .setListener { update ->
                val state = update.installState
                val progressInfo = update.progressInfo

                statusText = when (state) {
                    ModuleInstallStatusUpdate.InstallState.STATE_DOWNLOADING -> {
                        val progressPercentage = progressInfo?.let {
                            (it.bytesDownloaded * 100 / it.totalBytesToDownload).toInt()
                        } ?: 0
                        progress = progressPercentage
                        "Downloading ML Kit module... $progress%"
                    }

                    ModuleInstallStatusUpdate.InstallState.STATE_INSTALLING -> "Installing ML Kit module..."
                    ModuleInstallStatusUpdate.InstallState.STATE_CANCELED -> "ML Kit module installation canceled."
                    ModuleInstallStatusUpdate.InstallState.STATE_FAILED -> "ML Kit module installation failed."
                    ModuleInstallStatusUpdate.InstallState.STATE_PENDING -> "ML Kit module installation pending."
                    ModuleInstallStatusUpdate.InstallState.STATE_DOWNLOAD_PAUSED -> "ML Kit module download paused."
                    ModuleInstallStatusUpdate.InstallState.STATE_UNKNOWN -> "ML Kit module installation unknown."
                    ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED -> {
                        progress = 100
                        isModuleReady = true
                        onReady(true)
                        "ML Kit module installed."
                    }

                    else -> "Unexpected installation state."
                }
            }
            .build()


        moduleInstallClient.installModules(request)
            .addOnSuccessListener {
                if (it.areModulesAlreadyInstalled()) {
                    statusText = "ML Kit module already installed."
                    progress = 100
                    isModuleReady = true
                    onReady(true)
                }
            }
            .addOnFailureListener { e ->
                statusText = "Failed to install ML Kit module."
                Log.e("MLKit", "Error: ${e.message}")
            }
    }

    // UI with progress bar
    AppTheme {
        Scaffold(contentWindowInsets = WindowInsets.safeContent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = statusText)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())


            }
        }

    }

}

fun isGooglePlayServicesAvailable(context: Context) : Boolean{
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return status == ConnectionResult.SUCCESS
}

private fun openPlayServicesInPlayStore(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("market://details?id=com.google.android.gms")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}


private fun openGooglePlayServicesSettings(context: Context) {
    // Launch the app settings screen for Google Play Services.
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:com.google.android.gms")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}





