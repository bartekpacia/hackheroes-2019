package pl.baftek.hackheroes_2019

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode

private const val TAG = "MainActivity"
private const val REQUEST_CODE_CAMERA_PERMISSION = 10

// Camera related config
private const val WIDTH = 740
private const val HEIGHT = 740
private const val ROTATION = Surface.ROTATION_0

private val RESOULTION = Size(WIDTH, HEIGHT)
private val ASPECT_RATIO = Rational(1, 1)

class MainActivity : AppCompatActivity() {

    private val analyzer: ImageAnalyzer = ImageAnalyzer()

    private lateinit var analysis: ImageAnalysis
    private lateinit var adapter: ArrayAdapter<String>

    private val labelerOptions = FirebaseVisionCloudImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    private val labeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions)

    private val results: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, results)
        listResults.adapter = adapter

        // Request camera permissions
        if (isCameraAccessGranted()) {
            viewfinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION
            )
        }


        // Every time the provided texture view changes, recompute layout
        viewfinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (isCameraAccessGranted()) {
                viewfinder.post { startCamera() }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun isCameraAccessGranted(): Boolean {
        return ContextCompat
            .checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {

        // Create configuration object for the viewfinder use case
        fun initPreview(): Preview {
            val previewConfig = PreviewConfig.Builder()
                .setTargetAspectRatio(ASPECT_RATIO)
                .setTargetResolution(RESOULTION)
                .build()

            val preview = Preview(previewConfig)

            // Every time the viewfinder is updated, recompute layout
            preview.setOnPreviewOutputUpdateListener { previewOutput: Preview.PreviewOutput ->

                // To update the SurfaceTexture, we have to remove it and re-add it
                val parent = viewfinder.parent as ViewGroup
                parent.removeView(viewfinder)
                parent.addView(viewfinder, 0)

                viewfinder.surfaceTexture = previewOutput.surfaceTexture
                updateTransform()
            }

            return preview
        }

        fun initCapture(): ImageCapture {
            val captureConfig = ImageCaptureConfig.Builder()
                .setTargetAspectRatio(ASPECT_RATIO)
                .setTargetRotation(ROTATION)
                .setTargetResolution(RESOULTION)
                .setFlashMode(FlashMode.AUTO)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build()

            val capture = ImageCapture(captureConfig)

            buttonShutter.setOnClickListener {
                capture.takePicture(object : ImageCapture.OnImageCapturedListener() {
                    override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {

                        analysis.analyzer?.analyze(image, rotationDegrees)

                        image?.close()
                    }

                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        val log = "Image capture failed"
                        Log.w(TAG, log)
                        Toast.makeText(this@MainActivity, log, LENGTH_SHORT).show()
                    }

                })
            }

            return capture
        }

        fun initAnalysis(): ImageAnalysis {
            val analysisConfig = ImageAnalysisConfig.Builder()
                .setTargetAspectRatio(ASPECT_RATIO)
                .setTargetRotation(ROTATION)
                .setTargetResolution(RESOULTION)
                .setImageQueueDepth(1)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build()

            val analysis = ImageAnalysis(analysisConfig)
            analysis.setAnalyzer { image: ImageProxy?, rotationDegrees: Int ->
                val mediaImage = image?.image ?: return@setAnalyzer
                val imageRotation = ImageAnalyzer.degreesToFirebaseRotation(rotationDegrees)

                val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)


                labeler.processImage(visionImage)
                    .addOnSuccessListener { labels ->
                        results.clear()

                        labels.forEach {
                            val resultDebug =
                                "text: ${it.text}, entityId: ${it.entityId}, confidence: ${it.confidence}"
                            val result = "${it.text}, ${it.confidence.toBigDecimal().setScale(
                                2,
                                RoundingMode.UP
                            ).toDouble() * 100} %"

                            Log.d(TAG, resultDebug)
                            results.add(result)
                        }

                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception -> exception.printStackTrace() }
            }

            return analysis
        }


        val preview = initPreview()
        val capture = initCapture()
        analysis = initAnalysis()

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, preview)
        CameraX.bindToLifecycle(this, capture)
        // CameraX.bindToLifecycle(this, analysis)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewfinder.width / 2f
        val centerY = viewfinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (viewfinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewfinder.setTransform(matrix)
    }
}
