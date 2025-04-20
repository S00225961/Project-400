package com.example.project400.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        val exerciseName = arguments?.getString("exerciseName")
        val gifImageView = view.findViewById<ImageView>(R.id.exerciseGifImageView)
        val nameTextView = view.findViewById<TextView>(R.id.exerciseDetailNameTextView)

        nameTextView.text = exerciseName

        // Set image from assets or URL based on name
        val gifResId = getGifResourceId(exerciseName)
        if (gifResId != null) {
            gifImageView.setImageResource(gifResId)
        } else {
            gifImageView.setImageResource(context.assets.open("gifs/barbell-bicep-curl.gif"))
        }
    }

    private fun getGifResourceId(name: String?): Int? {
        val context = requireContext()
        val cleanName = name?.lowercase()?.replace(" ", "_") ?: return null
        return context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    }
}
