package com.AKSohag.easybgremover

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by ak-sohag on 2/20/2025.
 */


object ImageSegment {

    val subjectResultOptions = SubjectSegmenterOptions.SubjectResultOptions.Builder()
        .enableConfidenceMask()
        .enableSubjectBitmap()
        .build()

    val options = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .enableForegroundConfidenceMask()
        .build()

    val segmenter = SubjectSegmentation.getClient(options)

    suspend fun processImage(bitmap: Bitmap) = suspendCoroutine {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
//                getForegroundConfidenceMask(result, bitmap)
              it.resume(result.foregroundBitmap)
            }
            .addOnFailureListener { e ->
                it.resumeWithException(e)
            }
    }

    fun getForegroundConfidenceMask(result: SubjectSegmentationResult, bitmap: Bitmap): Bitmap {
        val colors = IntArray(bitmap.width * bitmap.height)

        val foregroundMask = result.foregroundConfidenceMask
        for (i in 0 until bitmap.width * bitmap.height) {
            if (foregroundMask!![i] > 0.5f) {
                colors[i] = Color.argb(128, 255, 0, 255)
            }
        }

        val bitmapMask = Bitmap.createBitmap(
            colors, bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888
        )
        return bitmapMask
    }

    fun getMaskForEach(result: SubjectSegmentationResult, bitmap: Bitmap): Bitmap {
        val subjects = result.subjects

        val colors = IntArray(bitmap.width * bitmap.height)
        for (subject in subjects) {
            val mask = subject.confidenceMask
            for (i in 0 until subject.width * subject.height) {
                val confidence = mask!![i]
                if (confidence > 0.5f) {
                    colors[bitmap.width * (subject.startY - 1) + subject.startX] =
                        Color.argb(128, 255, 0, 255)
                }
            }
        }

        val bitmapMask = Bitmap.createBitmap(
            colors, bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888
        )

        return bitmapMask
    }

}