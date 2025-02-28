package com.AKSohag.easybgremover.Screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.AKSohag.easybgremover.ImageSegment
import com.AKSohag.easybgremover.ui.Utils.checkeredBackground
import com.AKSohag.easybgremover.ui.Utils.getBitmapFromUri

/**
 * Created by ak-sohag on 2/25/2025.
 */


@Preview
@Composable
fun EditorScreen(
    uriString: String? = "",
    onBackClick: () -> Unit = {}
) {
    val imageUriString = remember { mutableStateOf(uriString ?: "") }
    val context = LocalContext.current
    var inputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var outputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }


    // First LaunchedEffect to load the input bitmap from URI
    LaunchedEffect(uriString) {
        Log.d("TAG", "EditorScreen: Loading image from URI $uriString")
        if (!uriString.isNullOrEmpty()) {
            val uri = Uri.parse(uriString)
            Log.d("TAG", "EditorScreen: Parsed URI $uri")
            inputBitmap = getBitmapFromUri(context, uri)
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
            // Consider using a coroutine for heavy processing
            loading = false
            Log.d("TAG", "EditorScreen: Image processing completed")
        }
    }

    Scaffold(
        bottomBar = {
            EditorBottomAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle click */ },
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.Compare, contentDescription = "Before After")
            }
        },
        topBar = { EditorTopAppbar(onBackClick = onBackClick) }
    ) {
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
                Box {
                    if (imageUriString.value.isNotBlank()) {
                        if (outputBitmap != null) {
                            Image(
                                bitmap = outputBitmap!!.asImageBitmap(),
                                contentDescription = "Processed Image",
                                modifier = Modifier.drawBehind {
                                    checkeredBackground()
                                }
                            )
                        } else if (inputBitmap != null) {
                            Image(
                                bitmap = inputBitmap!!.asImageBitmap(),
                                contentDescription = "Original Image"
                            )
                        }

                        // Display a loading indicator while image segmentation is in progress
                        if (loading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun EditorBottomAppBar() {
    var showColorPicker by remember { mutableStateOf(false) }
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
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Select Color") },
            text = {
                // Replace this with your preferred color picker implementation
                // For example, you might use a library like ComposeColorPicker
                // This is a simplified placeholder
                Column {
                    // Simple RGB sliders could go here
                    Text("Color Picker Dialog Content")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        // Add the custom color to the list if it's not already there
                        if (!colors.contains(customColor)) {
                            colors.add(1, customColor) // Add after transparent
                        }
                        showColorPicker = false
                    }) {
                        Text("Add Color")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) {
                    Text("Close")
                }
            }
        )
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
                        Log.d("TAG", "EditorBottomAppBar: Custom color picker clicked")
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Red, Color.Yellow, Color.Green,
                                        Color.Cyan, Color.Blue, Color.Magenta
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
                            checkeredBackground()
                        },
                    onClick = {
                        Log.d("TAG", "EditorBottomAppBar: Clicked on color $color index $it")
                    }
                ) {

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopAppbar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = {
                onBackClick()
            }) {
                Icon(
                    imageVector = Icons.Default.Close, // Example: Heart icon
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            TextButton(onClick = {}) {
                Text(
                    "save".uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W900,
                    color = MaterialTheme.colorScheme.primary
                )
            }

        },
        title = {
        }
    )
}