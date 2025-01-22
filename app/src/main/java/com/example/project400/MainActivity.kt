package com.example.project400

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.view.SurfaceView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var surfaceView: SurfaceView
    private var device = Device.CPU
    private var isClassifyPose = false
    private var camera: Camera? = null
    companion object {
        private const val FRAGMENT_DIALOG = "dialog"
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                openCamera()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                ErrorDialog.newInstance(getString(R.string.camera_request_permission))
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            }
        }
    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // do nothing
                }
                .create()

        companion object {

            @JvmStatic
            private val ARG_MESSAGE = "message"

            @JvmStatic
            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        if (!isCameraPermissionGranted()) {
            requestPermission()
        }
        else {
            openCamera()
        }

    }

    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }

    // request camera permission
    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                // You can use the API that requires the permission.
                openCamera()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    // open camera
    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            if (camera == null) {
                camera =
                    Camera(surfaceView, object : Camera.CameraSourceListener {
                        override fun onFPSListener(fps: Int) {

                        }

                        override fun onDetectedInfo(
                            personScore: Float?,
                            poseLabels: List<Pair<String, Float>>?
                        ) {

                        }

                    }).apply {
                        prepareCamera()
                    }
                isPoseClassifier()
                lifecycleScope.launch(Dispatchers.Main) {
                    camera?.initCamera()
                }
            }
            createPoseEstimator()
        }
    }

    // pose classifier
    private fun isPoseClassifier() {
        camera?.setClassifier(if (isClassifyPose) PoseClassifier.create(this) else null)
    }

    // create pose estimator
    private fun createPoseEstimator() {
        // Create MoveNet Lightning (SinglePose)
        val poseDetector = MoveNet.create(this, device)

        // Configure display options for SinglePose
//        showPoseClassifier(true)
//        showDetectionScore(true)
//        showTracker(false)

        // Set the pose detector on the camera source
        poseDetector?.let { detector ->
            camera?.setDetector(detector)
        }
    }


}