package com.AKSohag.easybgremover.Screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.AKSohag.easybgremover.MLKitModuleInstaller
import com.AKSohag.easybgremover.R
import com.AKSohag.easybgremover.ui.Utils.checkeredBackground
import com.smarttoolfactory.beforeafter.BeforeAfterImage
import com.smarttoolfactory.beforeafter.OverlayStyle
import dashedBorder
import kotlinx.coroutines.delay

/**
 * Created by ak-sohag on 2/25/2025.
 */


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onImageSelected: (Uri) -> Unit,
) {

    MyApp(LocalContext.current, onImageSelected)

}

@Composable
fun MyApp(context: Context, onImageSelected: (Uri) -> Unit) {
    var isModuleReady by remember { mutableStateOf(false) }

    if (!isModuleReady) {
        MLKitModuleInstaller(context) { ready ->
            isModuleReady = ready
        }
    } else {
        TheHomeScreen {
            onImageSelected(it)
        }
    }
}


@Composable
fun TheHomeScreen(modifier: Modifier = Modifier, onImageSelected: (Uri) -> Unit) {
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                onImageSelected(it)
            }
        }

    // multiple image picker
    val pickMultipleMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            // log
            Log.d("TAG", "HomeScreen: $uris ")
        }

    Scaffold(
    ) {
        Box(
            modifier
                .padding(it)
                .fillMaxSize()

        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.W900,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                BeforeAfter(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .dashedBorder(
                            width = 2.dp,
                            color = Color.LightGray,
                            shape = MaterialTheme.shapes.medium, on = 4.dp, off = 4.dp
                        )

                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(15.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Select Image")
                }
                Button(
                    onClick = {
                        pickMultipleMedia.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentPadding = PaddingValues(15.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Select Multiple Images")
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("No Images? Try one of these")
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(R.drawable.dummy_replace),
                    contentDescription = "Dummy Image",
                    contentScale = ContentScale.FillHeight,
                    modifier = modifier
                        .fillMaxWidth()
                        .size(70.dp)
                        .padding(horizontal = 16.dp)
                )

            }

        }
    }


}


@Composable
fun BeforeAfter(modifier: Modifier = Modifier) {
    var progress by remember { mutableFloatStateOf(50f) }

    val beforeImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.girl_2)
    val afterImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.girl_bg_removed)


    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000)
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (progress >= 100f) {
                progress = 0f
            } else {
                progress += 50f
            }
        }
    }

    BeforeAfterImage(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .drawBehind {
                checkeredBackground()
            },
        beforeLabel = {},
        overlayStyle = OverlayStyle(
            verticalThumbMove = false,
            dividerWidth = 0.dp,
            dividerColor = Color.Transparent,
            thumbSize = 0.dp,
        ),
        afterLabel = {},
        enableProgressWithTouch = false,
        beforeImage = afterImage,
        afterImage = beforeImage,
        progress = animatedProgress
    )
}


@Preview
@Composable
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    HomeScreen {

    }
}