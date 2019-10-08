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
import kotlinx.coroutines.tasks.await
import java.math.RoundingMode

private const val TAG = "MainActivityVM"

class MainActivityViewModel : ViewModel() {

    private val labelerOptions = FirebaseVisionCloudImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    private val labeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions)

    val results: LiveData<MutableList<String>> = MutableLiveData()

    fun analyzeImage(img: ImageProxy?, rotation: Int): LiveData<MutableList<String>> = liveData(Dispatchers.Main) {
        val mediaImage = img?.image ?: return@liveData
        val imageRotation = ImageAnalyzer.degreesToFirebaseRotation(rotation)

        val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)

        val visionImageLabels = labeler.processImage(visionImage).await()

        val results: MutableList<String> = arrayListOf()

        visionImageLabels.forEach {
            val confidence = "${it.confidence.toBigDecimal().setScale(2, RoundingMode.UP).toDouble() * 100} %"

            val result = "${it.text}, $confidence"

            results.add(result)
        }

        emit(results)
    }
}