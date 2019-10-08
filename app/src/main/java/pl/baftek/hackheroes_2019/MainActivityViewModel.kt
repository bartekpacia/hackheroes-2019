package pl.baftek.hackheroes_2019

import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import kotlinx.coroutines.Dispatchers

private const val TAG = "MainActivityVM"

class MainActivityViewModel : ViewModel() {

    private val labelerOptions = FirebaseVisionCloudImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    private val labeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions)

    val results: LiveData<List<String>> = MutableLiveData()

    fun analyzeImage(image: ImageProxy?, rotation: Int): LiveData<List<String>?> = liveData(Dispatchers.Main) {
        val mediaImage = image?.image ?: return@liveData
        val imageRotation = ImageAnalyzer.degreesToFirebaseRotation(rotation)

        val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)


        // labeler.processImage(visionImage)
    }
}