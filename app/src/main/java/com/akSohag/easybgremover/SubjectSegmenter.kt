package com.akSohag.easybgremover

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

sealed class SegmentationState {
    object Idle : SegmentationState()
    object Processing : SegmentationState()
    data class Success(val maskBitmap: Bitmap) : SegmentationState()
    data class Error(val exception: Exception) : SegmentationState()
}

class SubjectSegmenter {
    private val _segmentationState = MutableStateFlow<SegmentationState>(SegmentationState.Idle)
    val segmentationState: StateFlow<SegmentationState> = _segmentationState

    private var inputImage: InputImage? = null
    private var sourceBitmap: Bitmap? = null

    private val segmenter by lazy {
        val subjectResultOptions = SubjectSegmenterOptions.SubjectResultOptions.Builder()
            .enableConfidenceMask()
            .build()

        val options = SubjectSegmenterOptions.Builder()
            .enableMultipleSubjects(subjectResultOptions)
            .build()

        SubjectSegmentation.getClient(options)
    }

    fun setBitmap(bitmap: Bitmap) {
        sourceBitmap = bitmap
        inputImage = InputImage.fromBitmap(bitmap, 0)
    }

    suspend fun processImage(): Result<Bitmap> = withContext(Dispatchers.Default) {
        try {
            requireNotNull(inputImage) { "Input image not set. Call setBitmap() first." }
            requireNotNull(sourceBitmap) { "Source bitmap not set. Call setBitmap() first." }

            _segmentationState.value = SegmentationState.Processing

            val result = processImageInternal()
            val maskBitmap = createMaskBitmap(result)

            _segmentationState.value = SegmentationState.Success(maskBitmap)
            Result.success(maskBitmap)
        } catch (e: Exception) {
            _segmentationState.value = SegmentationState.Error(e)
            Result.failure(e)
        }
    }

    private suspend fun processImageInternal(): SubjectSegmentationResult =
        suspendCancellableCoroutine { continuation ->
            segmenter.process(inputImage!!)
                .addOnSuccessListener { result ->
                    continuation.resume(result)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }

            continuation.invokeOnCancellation {
                // Clean up resources if needed
            }
        }

    private fun createMaskBitmap(result: SubjectSegmentationResult): Bitmap {
        val width = sourceBitmap!!.width
        val height = sourceBitmap!!.height
        val colors = IntArray(width * height)

        result.subjects.forEach { subject ->
            val mask = subject.confidenceMask
            val startX = subject.startX
            val startY = subject.startY

            for (y in 0 until subject.height) {
                for (x in 0 until subject.width) {
                    val confidence = mask!![y * subject.width + x]
                    if (confidence > CONFIDENCE_THRESHOLD) {
                        val index = (startY + y - 1) * width + (startX + x)
                        if (index in colors.indices) {
                            colors[index] = MASK_COLOR
                        }
                    }
                }
            }
        }

        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888)
    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private val MASK_COLOR = Color.argb(128, 255, 0, 255)
    }
}