package com.AKSohag.easybgremover.Screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.AKSohag.easybgremover.ImageSegment
import com.AKSohag.easybgremover.ui.Utils.checkeredBackground
import com.AKSohag.easybgremover.ui.Utils.getBitmapFromUri
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by ak-sohag on 2/25/2025.
 */


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

    // First LaunchedEffect to load the input bitmap from URI
    LaunchedEffect(uriString) {
        Log.d("TAG", "EditorScreen: Loading image from URI $uriString")
        if (!uriString.isNullOrEmpty()) {
            val uri = Uri.parse(uriString)
            Log.d("TAG", "EditorScreen: Parsed URI $uri")
            inputBitmap = getBitmapFromUri(context, uri)
            displayBitmap = inputBitmap
            Log.d("TAG", "EditorScreen: Loaded bitmap ${inputBitmap != null}")
        }
    }

    // Second LaunchedEffect to process the image when module is ready and bitmap is available
    LaunchedEffect(inputBitmap) {
        if (inputBitmap != null && outputBitmap == null) {
            Log.d("TAG", "EditorScreen: Starting image processing")
            loading = true
            try {
                outputBitmap = ImageSegment.processImage(inputBitmap!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (outputBitmap != null) displayBitmap = outputBitmap else outputBitmap = inputBitmap
            loading = false
            Log.d("TAG", "EditorScreen: Image processing completed")
        }
    }

    Scaffold(bottomBar = {
        EditorBottomAppBar(
            onColorSelected = { color ->
                Log.d("TAG", "EditorScreen: Selected color $color")
                // Apply the background color to the bitmap
                if (outputBitmap != null) {
                    processingBackground = true
                    scope.launch {
                        val newBitmap = withContext(Dispatchers.Default) {
                            // Apply background color on a background thread
                            addBackgroundColorToBitmap(outputBitmap!!, color)
                        }
                        displayBitmap = newBitmap
                        processingBackground = false
                        Log.d("TAG", "EditorScreen: Background color applied to bitmap")
                    }
                }
            }
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { /* Handle click */ }, shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Filled.Compare, contentDescription = "Before After")
        }
    }, topBar = { EditorTopAppbar(onBackClick = onBackClick) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUriString.value.isNotBlank()) {
                        if (displayBitmap != null) {
                            Image(
                                bitmap = displayBitmap!!.asImageBitmap(),
                                contentDescription = "Processed Image",
                                modifier = Modifier.drawBehind {
                                    checkeredBackground()
                                }
                            )
                        }
//                        else if (inputBitmap != null) {
//                            Image(
//                                bitmap = inputBitmap!!.asImageBitmap(),
//                                contentScale = ContentScale.Fit,
//                                contentDescription = "Original Image",
//                                modifier = Modifier.drawBehind {
//                                    checkeredBackground()
//                                }
//                            )
//                        }

                        // Display a loading indicator while processing
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
            onDismissRequest = { showColorPicker = false },
            sheetState = sheetState
        ) {
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                text = "Color Picker",
            )
            Box(
                modifier = Modifier
                    .safeGesturesPadding(),
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
                        HsvColorPicker(modifier = Modifier
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
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                Card(colors = CardDefaults.cardColors(containerColor = color),
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
    modifier: Modifier = Modifier, onBackClick: () -> Unit
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
        TextButton(onClick = {}) {
            Text(
                "save".uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W900,
                color = MaterialTheme.colorScheme.primary
            )
        }

    }, title = {})
}


fun addBackgroundColorToBitmap(bitmap: Bitmap, backgroundColor: Color): Bitmap {
    // Ensure the original bitmap is in software mode
    val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)

    // Create a new mutable bitmap with the same size
    val newBitmap = Bitmap.createBitmap(softwareBitmap.width, softwareBitmap.height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(newBitmap)

    // Draw the background color first
    canvas.drawColor(backgroundColor.toArgb())

    // Draw the original bitmap on top of the background
    canvas.drawBitmap(softwareBitmap, 0f, 0f, null)

    return newBitmap
}







