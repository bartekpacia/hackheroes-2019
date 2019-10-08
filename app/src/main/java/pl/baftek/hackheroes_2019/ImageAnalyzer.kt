package pl.baftek.hackheroes_2019

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions

private const val TAG = "ImageAnalyzer"

class ImageAnalyzer : ImageAnalysis.Analyzer {

    private val labelerOptions = FirebaseVisionCloudImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    private val labeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions)


    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        val mediaImage = image?.image ?: return
        val imageRotation = degreesToFirebaseRotation(rotationDegrees)

        val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)


        labeler.processImage(visionImage)
            .addOnSuccessListener { labels ->
                labels.forEach {
                    Log.d(TAG, "text: ${it.text}, entityId: ${it.entityId}, confidence: ${it.confidence}")
                }
            }
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    companion object {
        fun degreesToFirebaseRotation(rotationDegrees: Int): Int = when (rotationDegrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270")
        }
    }
}