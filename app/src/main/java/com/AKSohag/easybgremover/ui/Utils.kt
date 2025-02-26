package com.AKSohag.easybgremover.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
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

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
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

}