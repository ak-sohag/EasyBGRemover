package com.AKSohag.easybgremover.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by ak-sohag on 2/25/2025.
 */


object Utils {

    fun DrawScope.checkeredBackground() {
        val squarePx = 8.dp.toPx()
        val columns = (size.width / squarePx).toInt() + 1
        val rows = (size.height / squarePx).toInt() + 1

        val color1 = Color(0x4DA1A1A1)
        val color2 = Color(0x0DFFFFFF)

        for (col in 0 until columns) {
            for (row in 0 until rows) {
                // Alternate colors based on position
                val color = if ((col + row) % 2 == 0) color1 else color2
                drawRect(
                    color = color,
                    topLeft = Offset(x = col * squarePx, y = row * squarePx),
                    size = Size(squarePx, squarePx)
                )
            }
        }
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    suspend fun Uri.toBitmap(context: Context): Bitmap? {
        return withContext(Dispatchers.IO) {
            getBitmapFromUri(context, this@toBitmap)
        }
    }

    /**
     * Saves a bitmap as PNG using modern Android storage approaches
     *
     * @param context The application context
     * @param bitmap The bitmap to save
     * @param filename The filename without extension
     * @param quality The compression quality (0-100)
     * @return The URI of the saved file or null if saving failed
     */
    suspend fun saveBitmapAsPng(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        quality: Int = 100
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // For Android 10 (API 29) and above, use MediaStore API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return@withContext null

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    if (bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)) {
                        return@withContext uri
                    }
                }
            } else {
                if (requestStoragePermission(context as Activity)) {
                    // For older Android versions, use the File API
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val imageFile = File(imagesDir, "$filename.png")

                    FileOutputStream(imageFile).use { outputStream ->
                        if (bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)) {
                            return@withContext Uri.fromFile(imageFile)
                        }
                    }
                }
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Saves a bitmap as PNG using modern Android storage approaches
     *
     * @param context The application context
     * @param filename The filename without extension
     * @param quality The compression quality (0-100)
     * @return The URI of the saved file or null if saving failed
     */
    suspend fun Bitmap.saveAsPng(
        context: Context,
        filename: String,
        quality: Int = 100
    ): Uri? = saveBitmapAsPng(context, this, filename, quality)

    private fun requestStoragePermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // For Android 6 to 9, request permission at runtime
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true // Permission already granted
            } else {
                // Request permission
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1001 // Request code
                )
                false // Permission not granted, waiting for user response
            }
        } else {
            true // No permission needed for Android 10+
        }
    }


    private fun addBackgroundColorToBitmap(bitmap: Bitmap, backgroundColor: Color): Bitmap {

        if (backgroundColor == Color.Transparent) {
            return bitmap
        }

        // Ensure the original bitmap is in software mode
        val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)

        // Create a new mutable bitmap with the same size
        val newBitmap =
            Bitmap.createBitmap(
                softwareBitmap.width,
                softwareBitmap.height,
                Bitmap.Config.ARGB_8888
            )

        val canvas = Canvas(newBitmap)

        // Draw the background color first
        canvas.drawColor(backgroundColor.toArgb())

        // Draw the original bitmap on top of the background
        canvas.drawBitmap(softwareBitmap, 0f, 0f, null)

        return newBitmap
    }

    /**
     * this function will add background color to the bitmap and return the new bitmap
     * this function is suspend function so it will run on background thread
     *
     * @param color the color to add in background
     */
    suspend fun Bitmap.addBackgroundColor(color: Color): Bitmap {
        return withContext(Dispatchers.Default) {
            addBackgroundColorToBitmap(this@addBackgroundColor, color)
        }
    }


    /**
     * Scales down an input bitmap to fit within 1080p resolution (1920x1080) while maintaining aspect ratio.
     * This optimizes memory usage and processing speed while preserving sufficient detail for segmentation.
     *
     * @return Scaled bitmap with max dimension of 1920x1080, maintaining aspect ratio
     */
    suspend fun Bitmap.scaleDownTo1080p(): Bitmap {
        val maxWidth = 1920
        val maxHeight = 1080

        // Get dimensions
        val width = width
        val height = height

        // Return original if already smaller than 1080p
        if (width <= maxWidth && height <= maxHeight) {
            return this
        }

        // Calculate scaling factors
        val widthRatio = maxWidth.toFloat() / width
        val heightRatio = maxHeight.toFloat() / height

        // Use the smaller ratio to ensure image fits within 1080p bounds
        val scaleFactor = minOf(widthRatio, heightRatio)

        // Calculate new dimensions
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        // Create and return scaled bitmap using bilinear filtering for better quality/speed balance
        val bitmap = Bitmap.createScaledBitmap(this, newWidth, newHeight, true)

        // recycle original for memory optimization
        recycle()

        return bitmap
    }


}