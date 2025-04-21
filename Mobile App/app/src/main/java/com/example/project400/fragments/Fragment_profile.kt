package com.example.project400.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.project400.R
import com.example.project400.UserForm
import com.example.project400.data.DatabaseHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class fragment_profile : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
        var currentBmi = view.findViewById<TextView>(R.id.bmiTextView)
        var workoutNumber = view.findViewById<TextView>(R.id.workoutNumber)
        var profilePicture = view.findViewById<ImageView>(R.id.profilePicture)

        profilePicture.setOnClickListener {
            val intent = Intent(requireContext(), UserForm::class.java)
            startActivity(intent)
        }

        val dbHelper = DatabaseHelper(requireContext())
        val workoutCount = dbHelper.getAllWorkouts().count().toString()
        workoutNumber.text = "Workouts: $workoutCount"
        val user = dbHelper.getUser()
        if(user != null){
            usernameTextView.text = user.username
            val bmi = user.bmi
            val roundedBmi = String.format("%.2f", bmi).toDouble()
            currentBmi.text = "Current BMI: $roundedBmi"
        }

        val workoutBarChart = view.findViewById<BarChart>(R.id.workoutBarChart)
        setupWorkoutChart(requireContext(), workoutBarChart)
    }

    private fun setupWorkoutChart(context: Context, workoutBarChart: BarChart) {
        val dbHelper = DatabaseHelper(context)
        val workouts = dbHelper.getWorkoutsForLast7Days()
        Log.d("Workouts", "$workouts")

        val workoutCountMap = mutableMapOf<String, Int>()
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        daysOfWeek.forEach { workoutCountMap[it] = 0 } // Initialize counts

        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())
        for (workout in workouts) {
            try {
                val date = inputFormat.parse(workout.date)
                val dayName = outputFormat.format(date)
                workoutCountMap[dayName] = workoutCountMap.getOrDefault(dayName, 0) + 1
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val entries = daysOfWeek.mapIndexed { index, day ->
            BarEntry(index.toFloat(), workoutCountMap[day]?.toFloat() ?: 0f)
        }

        val dataSet = BarDataSet(entries, "Workouts per Week")
        dataSet.color = ContextCompat.getColor(context, R.color.gray)

        val barData = BarData(dataSet)
        workoutBarChart.data = barData

        val xAxis = workoutBarChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek)

        workoutBarChart.axisLeft.axisMinimum = 0f
        workoutBarChart.axisRight.isEnabled = false

        workoutBarChart.setFitBars(true)
        workoutBarChart.description = Description().apply { text = "" }
        workoutBarChart.animateY(1000)
        workoutBarChart.invalidate()
    }

}