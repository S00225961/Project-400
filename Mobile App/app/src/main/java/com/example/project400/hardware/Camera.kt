package com.example.project400.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.SurfaceView
import com.example.project400.body_tracking.PoseDetector
import com.example.project400.data.Person
import com.example.project400.fragments.classifier
import com.example.project400.fragments.fragment_workout
import com.example.project400.visuals.VisualizationUtils
import com.example.project400.visuals.YuvToRgbConverter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Camera(private val surfaceView: SurfaceView, private val fragmentWorkout: fragment_workout) {
    companion object {
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480

        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .2f
    }

    private val lock = Any()
    private var detector: PoseDetector? = null
    private var isTrackerEnabled = false
    private var yuvConverter: YuvToRgbConverter = YuvToRgbConverter(surfaceView.context)
    private lateinit var imageBitmap: Bitmap

    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var imageReader: ImageReader? = null
    private var camera: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var imageReaderThread: HandlerThread? = null
    private var imageReaderHandler: Handler? = null
    private var cameraId: String = ""

    suspend fun initCamera() {
        camera = openCamera(cameraManager, cameraId)
        imageReader =
            ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 3)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                if (!::imageBitmap.isInitialized) {
                    imageBitmap =
                        Bitmap.createBitmap(
                            PREVIEW_WIDTH,
                            PREVIEW_HEIGHT,
                            Bitmap.Config.ARGB_8888
                        )
                }
                imageBitmap = yuvConverter.yuvToRgb(image)

                // Rotate for display only
                val rotateMatrix = Matrix()
                rotateMatrix.postRotate(90.0f)
                val rotatedBitmap = Bitmap.createBitmap(
                    imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    rotateMatrix, false
                )

                // Use original bitmap for model input
                val persons = processImage(imageBitmap)

                // Use rotated bitmap for displaying results
                visualize(persons, imageBitmap)

                image.close()
            }
        }, imageReaderHandler)

        imageReader?.surface?.let { surface ->
            session = createSession(listOf(surface))
            val cameraRequest = camera?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )?.apply {
                addTarget(surface)
            }
            cameraRequest?.build()?.let {
                session?.setRepeatingRequest(it, null, null)
            }
        }
    }

    private suspend fun createSession(targets: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            camera?.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(captureSession: CameraCaptureSession) =
                    cont.resume(captureSession)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(Exception("Session error"))
                }
            }, null)
        }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                }
            }, imageReaderHandler)
        }

    fun prepareCamera() {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraDirection != null &&
                cameraDirection == CameraCharacteristics.LENS_FACING_FRONT
            ) {
                continue
            }
            this.cameraId = cameraId
        }
    }

    fun setDetector(detector: PoseDetector) {
        synchronized(lock) {
            this.detector?.close()
            this.detector = detector
        }
    }

    private fun processImage(bitmap: Bitmap): List<Person> {
        val persons = mutableListOf<Person>()
        synchronized(lock) {
            detector?.estimatePoses(bitmap)?.let {
                persons.addAll(it)
                if (persons.isNotEmpty()) {
                    fragmentWorkout.displayPoseClassification(persons[0])
                }
            }
        }
        return persons
    }

    private fun visualize(persons: List<Person>, bitmap: Bitmap) {
        // Step 1: Draw the keypoints and pose information on the original bitmap
        val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            fragmentWorkout,
            bitmap,  // Use the original bitmap (before rotation)
            persons.filter { it.score > MIN_CONFIDENCE },
            isTrackerEnabled
        )

        // Step 2: Now apply the rotation to the output bitmap (90 degrees clockwise)
        val rotateMatrix = Matrix().apply {
            postRotate(90f) // Rotate 90 degrees clockwise for display
        }

        val rotatedBitmap = Bitmap.createBitmap(
            outputBitmap, 0, 0, outputBitmap.width, outputBitmap.height, rotateMatrix, false
        )

        // Step 3: Render the rotated bitmap on the canvas
        val holder = surfaceView.holder
        val surfaceCanvas = holder.lockCanvas()

        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val screenHeight: Int
            val left: Int
            val top: Int

            // Calculate aspect ratio and scaling
            if (canvas.height > canvas.width) {
                val ratio = rotatedBitmap.height.toFloat() / rotatedBitmap.width
                screenWidth = canvas.width
                left = 0
                screenHeight = (canvas.width * ratio).toInt()
                top = (canvas.height - screenHeight) / 2
            } else {
                val ratio = rotatedBitmap.width.toFloat() / rotatedBitmap.height
                screenHeight = canvas.height
                top = 0
                screenWidth = (canvas.height * ratio).toInt()
                left = (canvas.width - screenWidth) / 2
            }

            val right: Int = left + screenWidth
            val bottom: Int = top + screenHeight

            // Draw the rotated bitmap to the canvas (this is the final image with keypoints)
            canvas.drawBitmap(
                rotatedBitmap, Rect(0, 0, rotatedBitmap.width, rotatedBitmap.height),
                Rect(left, top, right, bottom), null
            )

            // Unlock the canvas after drawing
            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }


    fun resume() {
        imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
    }

    fun close() {
        session?.close()
        session = null
        camera?.close()
        camera = null
        imageReader?.close()
        imageReader = null
        detector?.close()
        detector = null
        classifier?.close()
    }
}
