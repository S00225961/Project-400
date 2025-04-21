package com.example.project400.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project400.MainActivity
import com.example.project400.R
import com.example.project400.WorkoutForm
import com.example.project400.data.Workout
import com.example.project400.adapter.WorkoutAdapter
import com.example.project400.data.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class fragment_history : Fragment() {

    private lateinit var workoutRecyclerView: RecyclerView
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var workoutList: List<Workout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var addWorkoutBtn = view.findViewById<Button>(R.id.addWorkoutBtn)
        addWorkoutBtn.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutForm::class.java)
            startActivity(intent)
        }

        //views
        workoutRecyclerView = view.findViewById(R.id.workoutRecyclerView)

        workoutList = DatabaseHelper(requireContext()).getAllWorkouts()

        workoutAdapter = WorkoutAdapter(workoutList)

        //view set
        workoutRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        workoutRecyclerView.adapter = workoutAdapter
    }

}