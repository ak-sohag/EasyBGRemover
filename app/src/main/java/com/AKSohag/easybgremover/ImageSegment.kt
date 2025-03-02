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

    val options = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .build()

    val segmenter = SubjectSegmentation.getClient(options)

    suspend fun processImage(bitmap: Bitmap) = suspendCoroutine {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                it.resume(result.foregroundBitmap)
            }
            .addOnFailureListener { e ->
                it.resumeWithException(e)
            }
    }

}