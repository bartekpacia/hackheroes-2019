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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val REQUEST_CODE_CAMERA_PERMISSION = 10

// Camera related config
private const val WIDTH = 720
private const val HEIGHT = 1280
private const val ROTATION = Surface.ROTATION_0

private val RESOULTION = Size(WIDTH, HEIGHT)
private val ASPECT_RATIO = Rational(16, 9)

class MainActivity : AppCompatActivity() {

    private lateinit var analysis: ImageAnalysis
    private lateinit var adapter: ArrayAdapter<String>

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.results.value!!)
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

        viewModel.results.observe(this) { results: MutableList<String> ->
            listResults.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, results)
            listResults.invalidate()
            Toast.makeText(this, "new results!", LENGTH_SHORT).show()
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
                viewModel.analyzeImage(image, rotationDegrees)
            }

            return analysis
        }


        val preview = initPreview()
        val capture = initCapture()
        analysis = initAnalysis()

        CameraX.bindToLifecycle(this, preview)
        CameraX.bindToLifecycle(this, capture)
        // CameraX.bindToLifecycle(this, analysis) // It throttles the device heavily
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
