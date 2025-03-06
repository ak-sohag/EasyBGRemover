package com.akSohag.easybgremover.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.akSohag.easybgremover.helpers.ImageSegment
import com.akSohag.easybgremover.utils.Utils.addBackgroundColor
import com.akSohag.easybgremover.utils.Utils.checkeredBackground
import com.akSohag.easybgremover.utils.Utils.saveAsPng
import com.akSohag.easybgremover.utils.Utils.toBitmap
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.smarttoolfactory.beforeafter.BeforeAfterImage
import com.smarttoolfactory.beforeafter.ContentOrder
import com.smarttoolfactory.beforeafter.OverlayStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by ak-sohag on 2/25/2025.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditorScreen(
    uriString: String? = "", onBackClick: () -> Unit = {}
) {
    val imageUriString = remember { mutableStateOf(uriString ?: "") }
    val context = LocalContext.current
    var inputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var outputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var displayBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var processingBackground by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showSaveBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var savingInProgress by remember { mutableStateOf(false) }


    LaunchedEffect(uriString) {
        if (!uriString.isNullOrEmpty()) {
            val uri = uriString.toUri()
            inputBitmap = uri.toBitmap(context)
            displayBitmap = inputBitmap
        }
    }

    LaunchedEffect(inputBitmap) {
        if (inputBitmap != null && outputBitmap == null) {
            loading = true
            try {
                outputBitmap = withContext(Dispatchers.Default) {
                    ImageSegment.processImage(inputBitmap!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (outputBitmap != null) displayBitmap = outputBitmap else outputBitmap = inputBitmap
            loading = false
        }
    }

    if (showSaveBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSaveBottomSheet = false },
            sheetState = sheetState
        ) {
            var uriOfImage by remember { mutableStateOf<Uri?>(null) }
            LaunchedEffect(showSaveBottomSheet) {
                scope.launch {
                    displayBitmap?.let { bitmap ->
                        savingInProgress = true
                        val uri = withContext(Dispatchers.IO) {
                            bitmap.saveAsPng(context, "my_image_${System.currentTimeMillis()}")
                        }
                        uriOfImage = uri
                        savingInProgress = false
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clip(shape = MaterialTheme.shapes.medium)
                ) {
                    Image(
                        bitmap = displayBitmap!!.asImageBitmap(),
                        contentDescription = "Processed Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                            .clip(shape = MaterialTheme.shapes.medium)
                    )
                    if (savingInProgress) {
                        CircularProgressIndicator()
                        Text("Saving...")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (uriOfImage != null) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Saved Successfully")
                        TextButton(
                            onClick = {
                                // share the image
                                val shareIntent = Intent(Intent.ACTION_SEND)
                                shareIntent.type = "image/png"
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uriOfImage)
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Share Image"
                                    )
                                )
                            }
                        ) {
                            Text("Share Image")
                        }
                    }



                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showSaveBottomSheet = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }

        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeGestures,
        bottomBar = {
            if (loading.not()) {
                EditorBottomAppBar(onColorSelected = { color ->
                    if (outputBitmap != null) {
                        processingBackground = true
                        scope.launch {
                            displayBitmap = outputBitmap!!.addBackgroundColor(color)
                            processingBackground = false
                        }
                    }
                })
            }
        },
        topBar = {
            EditorTopAppbar(onBackClick = onBackClick, onSaveButtonClicked = {
                showSaveBottomSheet = true
            })
        }) { paddingValues ->

        val paddingHorizontal =
            if (paddingValues.calculateLeftPadding(LayoutDirection.Ltr) == 0.dp &&
                paddingValues.calculateRightPadding(LayoutDirection.Ltr) == 0.dp
            ) {
                24
            } else {
                0
            }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = paddingHorizontal.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card {
                Box(contentAlignment = Alignment.Center) {
                    if (imageUriString.value.isNotBlank()) {
                        if (displayBitmap != null && loading.not()) {
                            BeforeAfterImage(
                                overlayStyle = OverlayStyle(
                                    verticalThumbMove = true,

                                    ),
                                contentOrder = ContentOrder.AfterBefore,
                                enableProgressWithTouch = true,
                                beforeImage = inputBitmap!!.asImageBitmap(),
                                afterImage = displayBitmap!!.asImageBitmap(),
                                modifier = Modifier
                                    .drawBehind {
                                        checkeredBackground()
                                    },
                            )
                        } else if (displayBitmap != null) {
                            Image(
                                bitmap = displayBitmap!!.asImageBitmap(),
                                contentDescription = "Processed Image",
                                modifier = Modifier
                                    .drawBehind {
                                        checkeredBackground()
                                    })
                        }
                        if (loading || processingBackground) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorBottomAppBar(
    onColorSelected: (Color) -> Unit = {}  // Callback for color selection
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()


    val controller = rememberColorPickerController()
    var customColor by remember { mutableStateOf(Color.Red) }

    val colors = remember {
        mutableStateListOf(
            Color.Transparent,
            Color.White,
            Color.Black,
            Color.Green,
            Color.Yellow,
            Color.Magenta,
            Color.Blue,
            Color.Red,
            Color.Gray,
            Color.Cyan,
            Color.DarkGray,
            Color.LightGray
        )
    }

    // Color picker dialog
    if (showColorPicker) {
        ModalBottomSheet(
            onDismissRequest = { showColorPicker = false }, sheetState = sheetState
        ) {
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                text = "Color Picker",
            )
            Box(
                modifier = Modifier.safeGesturesPadding(),
            ) {
                Column {

                    Row(
                        modifier = Modifier.height(200.dp)
                    ) {
                        AlphaTile(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                                .clip(MaterialTheme.shapes.medium), controller = controller
                        )
                        HsvColorPicker(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            controller = controller,
                            onColorChanged = { colorEnvelope: ColorEnvelope ->
                                customColor = colorEnvelope.color
                            })


                    }


                    AlphaSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(20.dp),
                        controller = controller,
                    )

                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(20.dp),
                        controller = controller,
                    )

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .safeGesturesPadding(),
                        contentPadding = PaddingValues(16.dp),
                        onClick = {
                            // Apply the selected custom color when done
                            onColorSelected(customColor)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showColorPicker = false
                                }
                            }
                        }) {
                        Text("Done".uppercase())
                    }

                }
            }
        }
    }

    BottomAppBar {
        LazyRow(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .size(70.dp)
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.medium),
                    onClick = {
                        showColorPicker = true
                    }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Red,
                                        Color.Yellow,
                                        Color.Green,
                                        Color.Cyan,
                                        Color.Blue,
                                        Color.Magenta
                                    )
                                )
                            ),
                    )
                }
            }

            // Predefined color items
            items(colors.size) {
                val color = colors[it]
                Card(
                    colors = CardDefaults.cardColors(containerColor = color),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .size(70.dp)
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .drawBehind {
                            if (color == Color.Transparent) {
                                checkeredBackground()
                            }
                        },
                    onClick = {
                        onColorSelected(color)
                    }) {

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopAppbar(
    modifier: Modifier = Modifier, onBackClick: () -> Unit, onSaveButtonClicked: () -> Unit = {}
) {


    CenterAlignedTopAppBar(modifier = modifier, navigationIcon = {
        IconButton(onClick = {
            onBackClick()
        }) {
            Icon(
                imageVector = Icons.Default.Close, // Example: Heart icon
                contentDescription = "Back",
            )
        }
    }, actions = {
        TextButton(onClick = {
            onSaveButtonClicked()
        }) {
            Text(
                "save".uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W900,
                color = MaterialTheme.colorScheme.primary
            )
        }

    }, title = {})


}









