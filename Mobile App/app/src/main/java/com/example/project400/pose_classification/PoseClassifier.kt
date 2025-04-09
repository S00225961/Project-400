package com.example.project400.pose_classification

import android.content.Context
import android.graphics.PointF
import com.example.project400.data.BodyPart
import com.example.project400.data.KeyPoint
import com.example.project400.data.Person
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

class PoseClassifier (context: Context) {

    private val tfliteInterpreter: Interpreter
    private val poseLabels: List<String>

    init {
        // Load the TFLite model
        tfliteInterpreter = loadModel(context, "pose_classifier.tflite")

        // Load pose labels
        poseLabels = loadPoseLabels(context, "pose_labels.txt")
    }

    // Load the TFLite model from the assets folder
    private fun loadModel(context: Context, modelFileName: String): Interpreter {
        val modelFile = context.assets.open(modelFileName).use { inputStream ->
            val modelByteArray = inputStream.readBytes()
            ByteBuffer.allocateDirect(modelByteArray.size).apply {
                order(ByteOrder.nativeOrder())
                put(modelByteArray)
            }
        }
        return Interpreter(modelFile)
    }

    // Load pose labels from a text file in the assets folder
    private fun loadPoseLabels(context: Context, labelsFileName: String): List<String> {
        val labels = mutableListOf<String>()
        context.assets.open(labelsFileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    labels.add(line!!)
                }
            }
        }

        return labels
    }

    // Classify the pose based on the input data
    fun classify(person: Person?): List<Pair<String, Float>> {
        val input = tfliteInterpreter.getInputTensor(0).shape()
        val output = tfliteInterpreter.getOutputTensor(0).shape()
        // Preprocess the pose estimation result to a flat array
        val inputVector = FloatArray(input[1])
        person?.keyPoints?.forEachIndexed { index, keyPoint ->
            inputVector[index * 3] = keyPoint.coordinate.y
            inputVector[index * 3 + 1] = keyPoint.coordinate.x
            inputVector[index * 3 + 2] = keyPoint.score
        }

        // Postprocess the model output to human readable class names
        val outputTensor = FloatArray(output[1])
        tfliteInterpreter.run(arrayOf(inputVector), arrayOf(outputTensor))
        val classificationResult = mutableListOf<Pair<String, Float>>()
        outputTensor.forEachIndexed { index, score ->
            classificationResult.add(Pair(poseLabels[index], score))
        }
        return classificationResult
    }

    //Functions for feedback
    fun calculateAngle(a: PointF, b: PointF, c: PointF): Float {
        val baX = a.x - b.x
        val baY = a.y - b.y
        val bcX = c.x - b.x
        val bcY = c.y - b.y

        val dotProduct = baX * bcX + baY * bcY
        val magnitudeBA = sqrt(baX.pow(2) + baY.pow(2))
        val magnitudeBC = sqrt(bcX.pow(2) + bcY.pow(2))

        val cosine = dotProduct / (magnitudeBA * magnitudeBC + 1e-6f)
        val angle = acos(cosine.coerceIn(-1f, 1f))
        return Math.toDegrees(angle.toDouble()).toFloat()
    }

    fun extractAngles(keypoints: List<KeyPoint>): Map<String, Float> {
        val kp = keypoints.associateBy { it.bodyPart }

        return mapOf(
            "knee_left" to calculateAngle(
                kp[BodyPart.LEFT_HIP]!!.coordinate,
                kp[BodyPart.LEFT_KNEE]!!.coordinate,
                kp[BodyPart.LEFT_ANKLE]!!.coordinate
            ),
            "knee_right" to calculateAngle(
                kp[BodyPart.RIGHT_HIP]!!.coordinate,
                kp[BodyPart.RIGHT_KNEE]!!.coordinate,
                kp[BodyPart.RIGHT_ANKLE]!!.coordinate
            ),
            "hip_left" to calculateAngle(
                kp[BodyPart.LEFT_SHOULDER]!!.coordinate,
                kp[BodyPart.LEFT_HIP]!!.coordinate,
                kp[BodyPart.LEFT_KNEE]!!.coordinate
            ),
            "hip_right" to calculateAngle(
                kp[BodyPart.RIGHT_SHOULDER]!!.coordinate,
                kp[BodyPart.RIGHT_HIP]!!.coordinate,
                kp[BodyPart.RIGHT_KNEE]!!.coordinate
            ),
            "elbow_left" to calculateAngle(
                kp[BodyPart.LEFT_SHOULDER]!!.coordinate,
                kp[BodyPart.LEFT_ELBOW]!!.coordinate,
                kp[BodyPart.LEFT_WRIST]!!.coordinate
            ),
            "elbow_right" to calculateAngle(
                kp[BodyPart.RIGHT_SHOULDER]!!.coordinate,
                kp[BodyPart.RIGHT_ELBOW]!!.coordinate,
                kp[BodyPart.RIGHT_WRIST]!!.coordinate
            )
        )
    }


    fun close() {
        tfliteInterpreter.close()
    }


}