package com.akSohag.easybgremover.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akSohag.easybgremover.R
import com.akSohag.easybgremover.helpers.ImageSegment
import com.akSohag.easybgremover.utils.Utils
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate

/**
 * Created by ak-sohag on 3/6/2025.
 */


@Composable
fun SplashScreen(modifier: Modifier = Modifier, onReady: (Boolean) -> Unit) {

    Scaffold { paddingValues ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {

                Image(
                    painter = painterResource(R.drawable.logo_v2),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )

            }
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                AppLaunchChecker(onReady = onReady)

            }

        }

    }

}

@Composable
fun AppLaunchChecker(
    modifier: Modifier = Modifier,
    onReady: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isProbablyEmulator by remember { mutableStateOf(true) }
    var isPlayServicesAvailable by remember { mutableStateOf(false) }

    var isMlKitModulesAvailable by remember { mutableStateOf(false) }
    val moduleInstallClient: ModuleInstallClient = remember { ModuleInstall.getClient(context) }
    var statusText by remember { mutableStateOf("Checking Mandatory App Feature...") }
    var progress by remember { mutableIntStateOf(0) }


    LaunchedEffect(Unit) {
        isProbablyEmulator = Utils.isProbablyEmulator()
        isPlayServicesAvailable = Utils.isPlayServicesAvailable(context)

        if (isProbablyEmulator.not() && isPlayServicesAvailable) {
            val request = ModuleInstallRequest.newBuilder()
                .addApi(ImageSegment.subjectSegmenter)
                .setListener { update ->
                    val state = update.installState
                    val progressInfo = update.progressInfo

                    statusText = when (state) {
                        ModuleInstallStatusUpdate.InstallState.STATE_DOWNLOADING -> {
                            val progressPercentage = progressInfo?.let {
                                (it.bytesDownloaded * 100 / it.totalBytesToDownload).toInt()
                            } ?: 0
                            progress = progressPercentage
                            "Downloading Mandatory App Feature... $progress%"
                        }

                        ModuleInstallStatusUpdate.InstallState.STATE_INSTALLING -> "Installing Mandatory App Feature..."
                        ModuleInstallStatusUpdate.InstallState.STATE_CANCELED -> "Mandatory App Feature installation canceled."
                        ModuleInstallStatusUpdate.InstallState.STATE_FAILED -> "Mandatory App Feature installation failed."
                        ModuleInstallStatusUpdate.InstallState.STATE_PENDING -> "Mandatory App Feature installation pending."
                        ModuleInstallStatusUpdate.InstallState.STATE_DOWNLOAD_PAUSED -> "Mandatory App Feature download paused."
                        ModuleInstallStatusUpdate.InstallState.STATE_UNKNOWN -> "Mandatory App Feature installation unknown."
                        ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED -> {
                            progress = 100
                            isMlKitModulesAvailable = true
                            onReady(true)
                            "Mandatory App Feature installed."
                        }

                        else -> "Unexpected installation state."
                    }
                }
                .build()

            moduleInstallClient.installModules(request)
                .addOnSuccessListener {
                    if (it.areModulesAlreadyInstalled()) {
                        statusText = "App feature already installed."
                        progress = 100
                        isMlKitModulesAvailable = true
                        onReady(true)
                    }
                }
                .addOnFailureListener { e ->
                    statusText = "Failed to install app feature."
                    Log.e("MLKit", "Error: ${e.message}")
                }
        }

    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        if (isProbablyEmulator) {
            Text(
                "App feature is not available on Emulator"
            )
        } else if (isPlayServicesAvailable.not()) {
            TextButton(onClick = {
                Utils.openPlayServicesInPlayStore(context)
            }) {
                Text("Please Update Play Service")
            }
        } else {
            Text(text = statusText)
            Spacer(modifier = Modifier.height(8.dp))
            if (progress > 0) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator()

    }

}