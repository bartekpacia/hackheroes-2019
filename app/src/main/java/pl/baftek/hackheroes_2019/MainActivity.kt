package pl.baftek.hackheroes_2019

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


private const val REQUEST_CODE_CAMERA_PERMISSION = 10
private const val WIDTH = 740
private const val HEIGHT = 740

class MainActivity : AppCompatActivity() {

    private val analyzer: ImageAnalyzer = ImageAnalyzer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            val previewConfig = PreviewConfig.Builder().apply {
                setTargetAspectRatio(Rational(1, 1))
                setTargetResolution(Size(WIDTH, HEIGHT))
            }.build()

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
                .setTargetAspectRatio(Rational(16, 9))
                .setTargetRotation(Surface.ROTATION_0)
                .setTargetResolution(Size(WIDTH, HEIGHT))
                .setFlashMode(FlashMode.AUTO)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build()

            val capture = ImageCapture(captureConfig)

            buttonShutter.setOnClickListener {

                capture.takePicture(object : ImageCapture.OnImageCapturedListener() {
                    override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
                        analyzer.analyze(image, rotationDegrees)

                        super.onCaptureSuccess(image, rotationDegrees)
                    }

                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        super.onError(imageCaptureError, message, cause)
                        Toast.makeText(this@MainActivity, "error while taking picture", LENGTH_SHORT).show()
                    }
                })
            }
            return capture
        }

        val preview = initPreview()
        val capture = initCapture()

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, preview)
        CameraX.bindToLifecycle(this, capture)
    }

    fun updateTransform() {
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
