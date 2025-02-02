package com.example.project400.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project400.R
import com.example.project400.data.Workout
import com.example.project400.adapter.WorkoutAdapter
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

        //views
        workoutRecyclerView = view.findViewById(R.id.workoutRecyclerView)

        // Example data, you can replace this with real data
        workoutList = listOf(
            Workout("Monday", formatDate(Date()), "70kg", "45 mins", 3, listOf("Squats", "Deadlifts"), listOf("45kg x 8", "50kg x 8")),
            Workout("Tuesday", formatDate(Date()), "80kg", "50 mins", 2, listOf("Bench Press", "Pull-ups"), listOf("45kg x 8", "50kg x 8")),
            Workout("Wednesday", formatDate(Date()), "60kg", "30 mins", 4, listOf("Push-ups", "Lunges"), listOf("45kg x 8", "50kg x 8"))
        )

        workoutAdapter = WorkoutAdapter(workoutList)

        //view set
        workoutRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        workoutRecyclerView.adapter = workoutAdapter
    }

    fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd MMMM", Locale.getDefault())
        return format.format(date)
    }

}