package com.example.project400.visuals

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.project400.R
import com.example.project400.data.BodyPart
import com.example.project400.data.Person
import com.example.project400.fragments.fragment_workout
import kotlin.math.max

object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 4f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 4f

    /** The text size of the person id that will be displayed when the tracker is available.  */
    private const val PERSON_ID_TEXT_SIZE = 30f

    /** Distance from person id to the nose keypoint.  */
    private const val PERSON_ID_MARGIN = 6f

    /** Pair of keypoints to draw lines between.  */
    private val bodyJoints = listOf(
        Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    // Draw line and point indicate body pose
    @SuppressLint("UseRequireInsteadOfGet")
    fun drawBodyKeypoints(
        fragmentWorkout: fragment_workout,
        input: Bitmap,
        persons: List<Person>,
        isTrackerEnabled: Boolean = false
    ): Bitmap {
        val paintCircle = Paint().apply {
            strokeWidth = CIRCLE_RADIUS
            color = fragmentWorkout.context?.let { ContextCompat.getColor(it, R.color.grass_green) }!!
        }
        val paintLine = Paint().apply {
            strokeWidth = LINE_WIDTH
            color = fragmentWorkout.context?.let { ContextCompat.getColor(it, R.color.red) }!!
            style = Paint.Style.STROKE
        }

        val paintText = Paint().apply {
            textSize = PERSON_ID_TEXT_SIZE
            color = Color.BLUE
            textAlign = Paint.Align.LEFT
        }

        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val originalSizeCanvas = Canvas(output)
        persons.forEach { person ->
            // draw person id if tracker is enable
            if (isTrackerEnabled) {
                person.boundingBox?.let {
                    val personIdX = max(0f, it.left)
                    val personIdY = max(0f, it.top)

                    originalSizeCanvas.drawText(
                        person.id.toString(),
                        personIdX,
                        personIdY - PERSON_ID_MARGIN,
                        paintText
                    )
                    originalSizeCanvas.drawRect(it, paintLine)
                }
            }
            bodyJoints.forEach {
                val pointA = person.keyPoints[it.first.position].coordinate
                val pointB = person.keyPoints[it.second.position].coordinate
                originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
            }

            person.keyPoints.forEach { point ->
                originalSizeCanvas.drawCircle(
                    point.coordinate.x,
                    point.coordinate.y,
                    CIRCLE_RADIUS,
                    paintCircle
                )
            }
        }
        return output
    }
}