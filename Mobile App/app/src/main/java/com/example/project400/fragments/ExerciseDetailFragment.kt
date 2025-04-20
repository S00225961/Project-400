package com.example.project400.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.project400.R

class ExerciseDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exerciseName = arguments?.getString("exercise_name")
        val gifImageView = view.findViewById<ImageView>(R.id.exerciseGifImageView)
        val nameTextView = view.findViewById<TextView>(R.id.exerciseDetailNameTextView)

        nameTextView.text = exerciseName
        when(exerciseName){
            "Barbell Biceps Curl" -> {
                loadGifFromAssets(requireContext(),gifImageView, "barbell-bicep-curl.gif")
            }
            "Bench Press" -> {
                loadGifFromAssets(requireContext(),gifImageView, "bench-press.gif")
            }
            "Bridge Pose" -> {
                loadImageFromAssets(requireContext(), gifImageView, "bridge-pose.jpg")
            }
            "Chest Fly Machine" -> {
                loadGifFromAssets(requireContext(),gifImageView, "chest-fly-machine.gif")
            }
            "Child Pose" -> {
                loadImageFromAssets(requireContext(), gifImageView, "child-pose.jpg")
            }
            "Cobra Pose" -> {
                loadImageFromAssets(requireContext(), gifImageView, "cobra-pose.jpg")
            }
            "Deadlift" -> {
                loadGifFromAssets(requireContext(), gifImageView, "deadlift.gif")
            }
            "Decline Bench Press" -> {
                loadGifFromAssets(requireContext(), gifImageView, "decline-bench-press.gif")
            }
            "Downward Dog Pose" -> {
                loadGifFromAssets(requireContext(), gifImageView, "downward-dog.gif")
            }
            "Hammer Curl" -> {
                loadGifFromAssets(requireContext(), gifImageView, "hammer-curl.gif")
            }
            "Hip Thrust" -> {
                loadGifFromAssets(requireContext(), gifImageView, "hip-thrust.gif")
            }
            "Incline Bench Press" -> {
                loadGifFromAssets(requireContext(), gifImageView, "incline-bench-press.gif")
            }
            "Lat Pulldown" -> {
                loadGifFromAssets(requireContext(), gifImageView, "lat-pulldown.gif")
            }
            "Lateral Raises" -> {
                loadGifFromAssets(requireContext(), gifImageView, "lateral-raises.gif")
            }
            "Leg Extension" -> {
                loadGifFromAssets(requireContext(), gifImageView, "leg-extension.gif")
            }
        }

    }

    fun loadGifFromAssets(context: Context, imageView: ImageView, gifFileName: String) {
        try {
            Glide.with(context)
                .asGif()
                .load("file:///android_asset/gifs/$gifFileName")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView)
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    fun loadImageFromAssets(context: Context, imageView: ImageView, fileName: String) {
        try {
            Glide.with(context)
                .load("file:///android_asset/gifs/$fileName")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
