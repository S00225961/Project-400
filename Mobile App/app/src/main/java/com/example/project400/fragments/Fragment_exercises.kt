package com.example.project400.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project400.R
import com.example.project400.adapter.ExerciseAdapter
import com.example.project400.data.Exercise
import com.example.project400.data.ExerciseType
import com.example.project400.data.ExerciseVariation

class fragment_exercises : Fragment() {

    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseList: List<Exercise>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercises, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exerciseRecyclerView = view.findViewById(R.id.exerciseRecyclerView)

        exerciseList = listOf(
            Exercise("Barbell Biceps Curl", ExerciseType.Arms, ExerciseVariation.Barbell),
            Exercise("Bench Press", ExerciseType.Arms, ExerciseVariation.Barbell),
            Exercise("Bridge Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Chest Fly Machine", ExerciseType.Chest, ExerciseVariation.Machine),
            Exercise("Child Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Cobra Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Deadlift", ExerciseType.Legs, ExerciseVariation.Barbell),
            Exercise("Decline Bench Press", ExerciseType.Arms, ExerciseVariation.Dumbbell),
            Exercise("Downward Dog Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Hammer Curl", ExerciseType.Arms, ExerciseVariation.Dumbbell),
            Exercise("Hip Thrust", ExerciseType.Legs, ExerciseVariation.Barbell),
            Exercise("Incline Bench Press", ExerciseType.Arms, ExerciseVariation.Dumbbell),
            Exercise("Lat Pulldown", ExerciseType.Back, ExerciseVariation.Machine),
            Exercise("Lateral Raises", ExerciseType.Back, ExerciseVariation.Dumbbell),
            Exercise("Leg Extension", ExerciseType.Legs, ExerciseVariation.Machine),
            Exercise("Leg Raises", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Pigeon Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Plank", ExerciseType.Core, ExerciseVariation.None),
            Exercise("Pull Up", ExerciseType.Arms, ExerciseVariation.None),
            Exercise("Push Up", ExerciseType.Arms, ExerciseVariation.None),
            Exercise("Romanian Deadlift", ExerciseType.Legs, ExerciseVariation.Barbell),
            Exercise("Russian Twist", ExerciseType.Core, ExerciseVariation.Dumbbell),
            Exercise("Shoulder Press", ExerciseType.Arms, ExerciseVariation.Machine),
            Exercise("Squat", ExerciseType.Legs, ExerciseVariation.Dumbbell),
            Exercise("Standing Mountain Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("T Bar Row", ExerciseType.Arms, ExerciseVariation.Machine),
            Exercise("Tree Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Triangle Pose", ExerciseType.Yoga, ExerciseVariation.None),
            Exercise("Tricep Dips", ExerciseType.Arms, ExerciseVariation.None),
            Exercise("Tricep Pushdown", ExerciseType.Arms, ExerciseVariation.None),
            Exercise("Warrior Pose", ExerciseType.Yoga, ExerciseVariation.None)
        )
        exerciseRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exerciseRecyclerView.adapter = ExerciseAdapter(exerciseList)
    }

}