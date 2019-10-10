package pl.baftek.hackheroes_2019

import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.math.RoundingMode

private const val TAG = "MainActivityVM"

class MainActivityViewModel : ViewModel() {

    private val labelerOptions = FirebaseVisionCloudImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    private val labeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions)

    private val _results: MutableLiveData<MutableList<String>> = MutableLiveData()
    val results: LiveData<MutableList<String>>
        get() = _results

    init {
        _results.value = mutableListOf()
    }

    fun analyzeImage(img: ImageProxy?, rotation: Int) = viewModelScope.launch {
        val mediaImage: Image = img!!.image!!
        val imageRotation = ImageAnalyzer.degreesToFirebaseRotation(rotation)

        val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)

        val visionImageLabels = labeler.processImage(visionImage).await()


        _results.value?.clear()
        visionImageLabels.forEach {
            val confidence = "${it.confidence.toBigDecimal().setScale(2, RoundingMode.UP).toDouble() * 100} %"

            val result = "${it.text}, $confidence"
            val resultDebug = "$result entityId: ${it.entityId}"

            Log.d(TAG, resultDebug)
            _results.value?.add(result)
        }

        // Notify observers
        _results.value = _results.value

    }
}