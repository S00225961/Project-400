package com.example.project400.feedback

import android.content.Context
import android.util.Log
import com.example.project400.data.Person
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class Feedback(context: Context) {

    fun generateFeedbackTags(
        poseClassName: String,
        angles: Map<String, Float>,
        temp: Float,
        hr: Float,
        spo2: Float,
        humidity: Float,
    ): List<String> {
        val tags = mutableListOf<String>()

        // Health metrics
        if(hr != -1f){
            if (hr > 160) {
                tags.add("heart_rate_very_high")
            } else if (hr > 135) {
                tags.add("heart_rate_high")
            } else if (hr < 65) {
                tags.add("heart_rate_very_low")
            } else if (hr < 85) {
                tags.add("heart_rate_low")
            }
        }

        if(spo2 != -1f){
            if (spo2 < 93) {
                tags.add("spo2_critical")
            } else if (spo2 < 94) {
                tags.add("spo2_low")
            } else if (spo2 in 98.0..100.0) {
                tags.add("spo2_optimal")
            }
        }

        if(temp != -1f){
            if (temp > 38.5) {
                tags.add("temperature_high")
            } else if (temp > 37.8) {
                tags.add("temperature_slightly_high")
            } else if (temp <= 34) {
                tags.add("temperature_low")
            }
        }

        if(humidity != -1f){
            if (humidity > 75) {
                tags.add("skin_humidity_very_high")
            } else if (humidity > 65) {
                tags.add("skin_humidity_high")
            } else if (humidity < 40) {
                tags.add("skin_humidity_low")
            }
        }

        // Pose angles
        val kneeL = angles["knee_left"] ?: 0f
        val kneeR = angles["knee_right"] ?: 0f
        val hipL = angles["hip_left"] ?: 0f
        val hipR = angles["hip_right"] ?: 0f
        val elbowL = angles["elbow_left"] ?: 0f
        val elbowR = angles["elbow_right"] ?: 0f
        Log.d("Pose Class Name", "$poseClassName")
        when (poseClassName.lowercase(Locale.ROOT)) {
            "push up" -> {
                if (elbowL > 135 && elbowR > 135) {
                    tags.add("pushup_depth_insufficient")
                } else if (hipL > 160 && hipR > 160 && elbowL <= 90 && elbowR <= 90) {
                    tags.add("pushup_good_form")
                }
            }
            "squat" -> {
                val isExtended = kneeL > 150 && kneeR > 150 && hipL > 150 && hipR > 150
                val isContracted = kneeL < 135 && kneeR < 135 && hipL < 135 && hipR < 135

                if (isExtended) {
                    tags.add("squat_extension_good_form")
                } else if (isContracted) {
                    tags.add("squat_contraction_good_form")
                } else {
                    tags.add("squat_in_transition")
                }
            }
            "deadlift", "romanian deadlift" -> {
                val isExtended = kneeL > 150 && kneeR > 150 && hipL > 150 && hipR > 150
                val isContracted = kneeL < 135 && kneeR < 135 && hipL < 135 && hipR < 135

                if (isExtended) {
                    tags.add("deadlift_extension_good_form")
                } else if (isContracted) {
                    tags.add("deadlift_contraction_good_form")
                } else {
                    tags.add("deadlift_in_transition")
                }
            }
            "bench press" -> {
                val isExtended = elbowL > 160 && elbowR > 160
                val isContracted = elbowL in 40f..100f && elbowR in 40f..100f

                if (isExtended) {
                    tags.add("bench_press_extension_good_form")
                } else if (isContracted) {
                    tags.add("bench_press_contraction_good_form")
                } else {
                    tags.add("bench_press_in_transition")
                }
            }
            "lat pulldown", "pull up" -> {
                val isExtended = elbowL > 160 && elbowR > 160
                val isContracted = elbowL < 120 && elbowR < 120
                val isNotEngaged = elbowL > 160 || elbowR > 160

                if (isContracted) {
                    tags.add("pull_contraction_good_form")
                } else if (isExtended) {
                    tags.add("pull_extension_good_form")
                } else if (isNotEngaged) {
                    tags.add("pull_elbow_not_engaged")
                } else {
                    tags.add("pull_in_transition")
                }
            }
            "shoulder press", "lateral raises" -> {
                val isExtended = elbowL > 160 && elbowR > 160
                val isContracted = elbowL in 60f..140f && elbowR in 60f..140f

                if (isExtended) {
                    tags.add("shoulder_extension_good_form")
                } else if (isContracted) {
                    tags.add("shoulder_contraction_good_form")
                } else {
                    tags.add("shoulder_in_transition")
                }
            }
            "chest fly machine" -> {
                val isExtended = elbowL > 140 && elbowR > 140
                val isContracted = elbowL in 110f..140f && elbowR in 110f..140f
                val isTooBent = elbowL < 100 || elbowR < 100

                if (isTooBent) {
                    tags.add("fly_arms_too_bent")
                } else if (isExtended) {
                    tags.add("chest_fly_extension_good_form")
                } else if (isContracted) {
                    tags.add("chest_fly_contraction_good_form")
                } else {
                    tags.add("chest_fly_in_transition")
                }
            }
            "barbell biceps curl", "hammer curl" -> {
                val isExtended = elbowL > 120 && elbowR > 120
                val isContracted = elbowL in 60f..120f && elbowR in 60f..120f

                if (isContracted) {
                    tags.add("biceps_curl_contraction_good_form")
                } else if (isExtended) {
                    tags.add("biceps_curl_extension_good_form")
                } else {
                    tags.add("biceps_curl_in_transition")
                }
            }
            "tricep dips", "tricep pushdown" -> {
                val isExtended = elbowL > 140 && elbowR > 140
                val isContracted = elbowL in 80f..140f && elbowR in 80f..140f

                if (isExtended) {
                    tags.add("tricep_extension_good_form")
                } else if (isContracted) {
                    tags.add("tricep_contraction_good_form")
                } else {
                    tags.add("tricep_in_transition")
                }
            }
            "bridge pose", "hip thrust" -> {
                val isExtended = hipL > 150 && hipR > 150
                val isContracted = hipL in 100f..150f && hipR in 100f..150f
                val isIncomplete = hipL < 100 || hipR < 100

                if (isIncomplete && !isContracted) {
                    tags.add("hip_thrust_incomplete_extension")
                } else if (isExtended) {
                    tags.add("hip_thrust_extension_good_form")
                } else if (isContracted) {
                    tags.add("hip_thrust_contraction_good_form")
                } else {
                    tags.add("hip_thrust_in_transition")
                }
            }
            "plank" -> {
                if (hipL > 170 || hipR > 170) {
                    tags.add("plank_hip_dip")
                } else if (hipL in 150f..170f && hipR in 150f..170f && elbowL < 110f && elbowR < 110f) {
                    tags.add("plank_good_form")
                }
            }
            "leg raises" -> {
                if (kneeL > 160 || kneeR > 160) {
                    tags.add("leg_raise_knees_not_engaged")
                } else if (kneeL in 80f..160f && kneeR in 80f..160f) {
                    tags.add("leg_raise_good_form")
                }
            }
            "leg extension" -> {
                val isExtended = kneeL > 120 && kneeR > 120
                val isContracted = kneeL in 80f..120f && kneeR in 80f..120f
                val isLowRange = kneeL < 80 || kneeR < 80

                if (isLowRange) {
                    tags.add("leg_extension_low_range")
                } else if (isExtended) {
                    tags.add("leg_extension_extension_good_form")
                } else if (isContracted) {
                    tags.add("leg_extension_contraction_good_form")
                } else {
                    tags.add("leg_extension_in_transition")
                }
            }
            "russian twist" -> {
                if (hipL < 100 || hipR < 100) {
                    tags.add("twist_posture_slouching")
                } else if (hipL > 120 && hipR > 120) {
                    tags.add("russian_twist_good_form")
                }
            }
            "t bar row" -> {
                val isExtended = elbowL > 140 && elbowR > 140
                val isContracted = elbowL <= 140 && elbowR <= 140

                if (isContracted) {
                    tags.add("tbar_row_contraction_good_form")
                } else if (isExtended) {
                    tags.add("tbar_row_extension_good_form")
                } else {
                    tags.add("tbar_row_in_transition")
                }
            }
            "tree pose" -> {
                if (kneeL < 90 || kneeR < 90) {
                    tags.add("tree_pose_good_form")
                } else if (kneeL >= 90 || kneeR >= 90) {
                    tags.add("tree_pose_bad_form")
                }
            }
            "triangle pose" -> {
                if (hipL > 170 || hipR > 170) {
                    tags.add("triangle_pose_bad_form")
                } else if (hipL < 90 && hipR in 90f..170f || hipR < 90 && hipL in 90f..170f) {
                    tags.add("triangle_pose_good_form")
                }
            }
            "child pose" -> {
                if (kneeL < 50 || kneeR < 50) {
                    tags.add("child_pose_good_form")
                } else if (kneeL >= 50 || kneeR >= 50) {
                    tags.add("child_pose_bad_form")
                }
            }
            "cobra pose" -> {
                if (elbowL <= 90 || elbowR <= 90) {
                    tags.add("cobra_pose_bad_form")
                } else if (elbowL > 90 && elbowR > 90) {
                    tags.add("cobra_pose_good_form")
                }
            }
            "pigeon pose" -> {
                if (hipL < 90 || hipR < 90) {
                    tags.add("pigeon_pose_bad_form")
                } else if (hipL > 110 && hipR > 110) {
                    tags.add("pigeon_pose_good_form")
                }
            }
            "warrior pose" -> {
                if (kneeR >= 150 && kneeL < 150 || kneeL >= 150 && kneeR < 150) {
                    tags.add("warrior_pose_good_form")
                } else {
                    tags.add("warrior_pose_bad_form")
                }
            }
            "standing mountain pose" -> {
                if (elbowL > 120 && elbowR > 120 && hipR > 120 && hipL > 120) {
                    tags.add("mountain_pose_good_form")
                } else {
                    tags.add("mountain_pose_bad_form")
                }
            }
            "downward dog pose" -> {
                if (hipL <= 110 && hipR <= 110) {
                    tags.add("downward_dog_good_form")
                } else {
                    tags.add("downward_dog_bad_form")
                }
            }
        }

        return tags
    }

    fun getFeedbackForTags(context: Context, tag: String): List<String> {
        val feedbackMessages = mutableListOf<String>()

        try {
            // Load JSON from assets
            val inputStream = context.assets.open("feedback_map.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val feedbackMap = JSONObject(jsonString)

            // Map matching feedback string
            if (feedbackMap.has(tag)) {
                feedbackMessages.add(feedbackMap.getString(tag))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            feedbackMessages.add("Unable to load feedback messages at this time.")
        }

        return feedbackMessages
    }
}