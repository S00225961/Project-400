package com.example.project400.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.project400.R
import com.example.project400.body_tracking.MoveNet
import com.example.project400.data.Person
import com.example.project400.feedback.Feedback
import com.example.project400.hardware.Camera
import com.example.project400.hardware.Device
import com.example.project400.pose_classification.PoseClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.project400.raspberrypi.Bluetooth
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.*

private lateinit var surfaceView: SurfaceView
private lateinit var poseClassifierText: TextView
private lateinit var piData: TextView
private lateinit var modelFeedback: TextView
private lateinit var bluetooth: Bluetooth
lateinit var classifier: PoseClassifier
lateinit var feedback: Feedback
private var device = Device.CPU
private var camera: Camera? = null
// Health data
private var Temperature = -1f
private var Spo2 = -1f
private var Heartrate = -1f
private var Humidity = -1f
//TTS
private lateinit var tts: TextToSpeech
private var isTtsInitialized = false
private val handler = Handler(Looper.getMainLooper())
private lateinit var feedbackRunnable: Runnable
private var pose = ""
private var personObject: Person? = null


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

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val bluetoothGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: true
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: true

            if (bluetoothGranted) {
                initBluetooth()
            } else {
                Log.e("Permissions", "Bluetooth permission denied")
            }

            if (cameraGranted) {
                openCamera()
            } else {
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
        modelFeedback = view.findViewById(R.id.modelFeedback)

        //Permissions
        val permissionList = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA)
        }

        if (permissionList.isNotEmpty()) {
            permissionLauncher.launch(permissionList.toTypedArray())
        } else {
            initBluetooth()
            openCamera()
        }

        // Instantiate the feedback model
        feedback = context?.let { Feedback(it) }!!
        // Instantiate the classifier
        classifier = context?.let { PoseClassifier(it) }!!

        //Instantiate TTS
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.UK)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "UK English not supported")
                } else {
                    isTtsInitialized = true
                }
            } else {
                Log.e("TTS", "TTS initialization failed")
            }
        }

        feedbackRunnable = object : Runnable {
            override fun run() {
                personObject?.let { modelFeedback(requireContext(), it, pose) }
                handler.postDelayed(this, Random.nextLong((0.1 * 60 * 1000).toLong(), (0.3 * 60 * 1000).toLong()))
            }
        }
        handler.postDelayed(feedbackRunnable, Random.nextLong((0.1 * 60 * 1000).toLong(), (0.5 * 60 * 1000).toLong()))

    }

    private fun initBluetooth() {
        bluetooth = Bluetooth(requireContext(), this)
        bluetooth.connectToPairedDevice()
        handler.postDelayed(readDataRunnable, 4000)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val readDataRunnable = object : Runnable {
        override fun run() {
            bluetooth.readAllSensorData()
            handler.postDelayed(this, 3000)
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

    // open camera
    private fun openCamera() {
        if (!isAdded) return

        if (isCameraPermissionGranted()) {
            if (camera == null) {
                classifier = context?.let { PoseClassifier(it) }!!
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
        val output = getString(R.string.pose_classification_text) + " ${mostAccuratePose?.first}"
        pose = mostAccuratePose?.first.toString()
        personObject = person
        requireActivity().runOnUiThread {
            poseClassifierText.text = output
        }
    }

    fun modelFeedback(context:Context, person: Person, poseClassName: String){
        val angles = classifier.extractAngles(person.keyPoints)
        Log.d("Angles", "Angles: $angles")
        Log.d("Points", "Key Points: ${person.keyPoints}")
        Log.d("Feedback", "Temp: $Temperature")
        Log.d("Feedback", "Heartrate: $Heartrate")
        Log.d("Feedback", "Spo2: $Spo2")
        Log.d("Feedback", "Humidity: $Humidity")
        Log.d("Feedback", "Pose Classification: $poseClassName")

        val tags = feedback.generateFeedbackTags(poseClassName, angles, Temperature, Heartrate, Spo2, Humidity)
        Log.d("Tags","Tags: $tags")
        val healthTags = tags.filter {
            it.startsWith("heart") || it.startsWith("spo2") || it.startsWith("temperature") || it.startsWith("skin")
        }
        val poseTags = tags - healthTags.toSet()
        val selectedTag = when (Random.nextInt(100)) {
            in 0..79 -> poseTags.randomOrNull()
            else -> healthTags.randomOrNull()
        } ?: tags.randomOrNull()

        val output = selectedTag?.let { feedback.getFeedbackForTags(context, it) }

        //TTS
        if (output != null) {
            output.forEach { speak(it) }
            modelFeedback.text = "Feedback: ${output.first()}"
        }
        Log.d("Feedback", "Feedback: $output")
    }

    private fun speak(message: String) {
        if (isTtsInitialized) {
            tts.speak(message, TextToSpeech.QUEUE_ADD, null, null)
        }
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
        handler.removeCallbacks(readDataRunnable)
        handler.removeCallbacks(feedbackRunnable)
        bluetooth.disconnect()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        camera?.close()
        camera = null
        personObject = null
    }

    override fun onSensorDataUpdated(temp: String, hr: String, spo2: String, humidity: String) {
        try {
            val tempValue = temp.substringBefore(" ").toFloatOrNull() ?: -1f
            val hrValue = hr.substringBefore(" ").toFloatOrNull() ?: -1f
            val spo2Value = spo2.substringBefore(" ").toFloatOrNull() ?: -1f
            val humidityValue = humidity.substringBefore(" ").toFloatOrNull() ?: -1f

            Temperature = tempValue
            Heartrate = hrValue
            Spo2 = spo2Value
            Humidity = humidityValue

            Log.d("Health", "Temp: $Temperature")
            Log.d("Health", "Heartrate: $Heartrate")
            Log.d("Health", "Spo2: $Spo2")
            Log.d("Health", "Humidity: $Humidity")

            piData.text = "Body Temp: $temp, Heart Rate: $hr, SpO2: $spo2, Humidity: $humidity"
        } catch (e: Exception) {
            Log.e("SensorData", "Failed to parse sensor data: ${e.message}")
        }
    }


}