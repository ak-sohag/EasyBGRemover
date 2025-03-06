package com.akSohag.easybgremover.helpers

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by ak-sohag on 2/20/2025.
 */


object ImageSegment {

    private val options = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .build()

    val subjectSegmenter = SubjectSegmentation.getClient(options)

    suspend fun processImage(bitmap: Bitmap) = suspendCoroutine {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        subjectSegmenter.process(inputImage)
            .addOnSuccessListener { result ->
                it.resume(result.foregroundBitmap)
            }
            .addOnFailureListener { e ->
                it.resumeWithException(e)
            }
    }

}