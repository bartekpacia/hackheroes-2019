package pl.baftek.hackheroes_2019.activities

import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pl.baftek.hackheroes_2019.data.VisionLabel

private const val TAG = "MainActivityVM"

class MainActivityViewModel : ViewModel() {

    private val labelerOptions = FirebaseVisionCloudImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    private val labeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions)

    private val _results: MutableLiveData<MutableList<VisionLabel>> = MutableLiveData()
    val results: LiveData<MutableList<VisionLabel>>
        get() = _results

    init {
        _results.value = mutableListOf()
    }

    fun analyzeImage(img: ImageProxy?, rotation: Int) = viewModelScope.launch {
        val mediaImage: Image = img!!.image!!
        val imageRotation = degreesToFirebaseRotation(rotation)

        val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)

        val visionImageLabels = labeler.processImage(visionImage).await()


        _results.value?.clear()
        visionImageLabels.forEach {
            val visionLabel = VisionLabel(it.text, it.confidence, it.entityId)

            Log.d(TAG, visionLabel.toString())
            _results.value?.add(visionLabel)
        }

        // Notify observers
        _results.value = _results.value

    }

    private fun degreesToFirebaseRotation(rotationDegrees: Int): Int = when (rotationDegrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270")
    }
}