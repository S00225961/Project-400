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
            Exercise("Triceps Extension", ExerciseType.Arms, ExerciseVariation.Barbell),
            Exercise("Bicep Curl", ExerciseType.Arms, ExerciseVariation.Barbell),
            Exercise("Leg Extension", ExerciseType.Legs, ExerciseVariation.Machine),
        )
        exerciseRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exerciseRecyclerView.adapter = ExerciseAdapter(exerciseList)
    }

}