package com.example.project400.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.project400.R
import com.example.project400.body_tracking.MoveNet
import com.example.project400.data.Person
import com.example.project400.hardware.Camera
import com.example.project400.hardware.Device
import com.example.project400.pose_classification.PoseClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.project400.raspberrypi.Bluetooth

private lateinit var surfaceView: SurfaceView
private lateinit var poseClassifierText: TextView
private lateinit var piData: TextView
private lateinit var bluetoothStatus: TextView
private lateinit var bluetooth: Bluetooth
lateinit var classifier: PoseClassifier
private var device = Device.CPU
private var camera: Camera? = null

class fragment_workout : Fragment(), Bluetooth.SensorDataListener  {

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
                    .show(requireActivity().supportFragmentManager, FRAGMENT_DIALOG)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surfaceView = view.findViewById(R.id.surfaceView)
        poseClassifierText = view.findViewById(R.id.poseClassifierText)
        piData = view.findViewById(R.id.raspberryPiData)
        bluetoothStatus = view.findViewById(R.id.bluetoothStatus)

        bluetooth = Bluetooth(requireContext(), this)
        bluetooth.connectToPairedDevice()
        handler.postDelayed(readDataRunnable, 4000)

        // Instantiate the classifier
        classifier = context?.let { PoseClassifier(it) }!!

        if (!isCameraPermissionGranted()) {
            requestPermission()
        }
        else {
            openCamera()
        }

    }

    private val handler = Handler(Looper.getMainLooper())
    private val readDataRunnable = object : Runnable {
        override fun run() {
            bluetooth.readAllSensorData()
            handler.postDelayed(this, 3000) // Every 3 sec
        }
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onResume() {
        camera?.resume()
        super.onResume()
    }

    override fun onPause() {
        camera?.close()
        camera = null
        super.onPause()
    }

    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // request camera permission
    private fun requestPermission() {
        // Check if the camera permission is granted
        if (!isAdded) return

        if (isCameraPermissionGranted()) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // open camera
    private fun openCamera() {
        if (!isAdded) return

        if (isCameraPermissionGranted()) {
            if (camera == null) {
                camera = Camera(surfaceView, this).apply { prepareCamera() }
                lifecycleScope.launch(Dispatchers.Main) {
                    camera?.initCamera()
                }
            }
            createPoseEstimator()
        }
    }

    fun displayPoseClassification(person: Person){
        // Classify
        if(!isAdded) return
        val classificationResult = classifier.classify(person)
        val sortedResults = classificationResult.sortedByDescending { it.second }
        var mostAccuratePose = sortedResults.firstOrNull()
        val output = getString(R.string.pose_classification_text) + " $mostAccuratePose"
        poseClassifierText.text = output

        // Temp code?
        val angles = classifier.extractAngles(person.keyPoints)
        bluetoothStatus.text = angles.toString()
    }

    // create pose estimator
    private fun createPoseEstimator() {
        if(!isAdded) return
        // Create MoveNet Lightning (SinglePose)
        val poseDetector = context?.let { MoveNet.create(it, device) }

        // Set the pose detector on the camera source
        poseDetector?.let { detector ->
            camera?.setDetector(detector)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(readDataRunnable) // Stop reading when fragment is destroyed
        bluetooth.disconnect()
    }

    override fun onSensorDataUpdated(temp: String, hr: String, spo2: String, humidity: String) {
        piData.text = "Body Temp: $temp°C, Heart Rate: $hr bpm, SpO2: $spo2%, Humidity: $humidity%"
    }

}