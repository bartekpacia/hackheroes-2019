package pl.baftek.hackheroes_2019

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val REQUEST_CODE_CAMERA_PERMISSION = 10

class MainActivity : AppCompatActivity() {


    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (isCameraAccessGranted()) cameraView.post { startCamera() }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_CAMERA_PERMISSION
            )
        }

        cameraView.bindToLifecycle(this)

        viewModel.results.observe(this) { results: MutableList<String>? ->
            if (results != null) {
                val intent = Intent(this, ResultsActivity::class.java)
                intent.putExtra("results", results.toTypedArray())

                startActivity(intent)
            }
            Toast.makeText(this, "new results!", LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        buttonShutter.setOnClickListener {
            cameraView.takePicture(object : ImageCapture.OnImageCapturedListener() {
                override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
                    viewModel.analyzeImage(image, rotationDegrees)

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
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (isCameraAccessGranted()) {
                cameraView.post { startCamera() }
            } else {
                Toast.makeText(this, "permissions error.", LENGTH_SHORT).show()
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
}
