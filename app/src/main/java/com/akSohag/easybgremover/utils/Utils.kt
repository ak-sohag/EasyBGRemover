package com.akSohag.easybgremover.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
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
    private suspend fun saveBitmapAsPng(
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
            createBitmap(softwareBitmap.width, softwareBitmap.height)

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


    fun isPlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)

        return when (resultCode) {
            ConnectionResult.SUCCESS -> true
            else -> {
                if (googleApiAvailability.isUserResolvableError(resultCode)) {
                    googleApiAvailability.getErrorDialog(context as Activity, resultCode, 9999)
                        ?.show()
                }
                false
            }
        }
    }

    fun isProbablyEmulator(): Boolean {
        // Check build fingerprint for generic or unknown values.
        if (Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown")) {
            return true
        }

        // Check for typical emulator models.
        val model = Build.MODEL
        if (model.contains("google_sdk") || model.contains("Emulator") ||
            model.contains("Android SDK built for")
        ) {
            return true
        }

        // Check manufacturer and brand.
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val device = Build.DEVICE
        if (manufacturer.contains("Genymotion") ||
            (brand.startsWith("generic") && device.startsWith("generic"))
        ) {
            return true
        }

        // Check product names that are known to be associated with emulators.
        val product = Build.PRODUCT
        if (product == "sdk_google" || product == "google_sdk" || product == "sdk") {
            return true
        }

        // Check hardware properties commonly used in emulators.
        val hardware = Build.HARDWARE
        if (hardware.contains("goldfish") || hardware.contains("ranchu") || hardware.contains("gki")) {
            return true
        }

        // Check for the QEMU flag via system properties.
        try {
            val qemu = System.getProperty("ro.kernel.qemu")
            if (qemu?.toIntOrNull() == 1) {
                return true
            }
        } catch (e: Exception) {
            // Ignore exceptions, as this is just a heuristic check.
        }

        // If none of the conditions matched, likely not an emulator.
        return false
    }

    fun openPlayServicesInPlayStore(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=com.google.android.gms".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }


}