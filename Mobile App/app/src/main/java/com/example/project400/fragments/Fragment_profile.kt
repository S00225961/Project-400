package com.example.project400.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.project400.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

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

        val workoutBarChart = view.findViewById<BarChart>(R.id.workoutBarChart)
        setupWorkoutChart(workoutBarChart)
    }

    private fun setupWorkoutChart(workoutBarChart: BarChart) {
        val entries = listOf(
            BarEntry(0f, 3f),
            BarEntry(1f, 5f),
            BarEntry(2f, 7f),
            BarEntry(3f, 4f),
            BarEntry(4f, 6f),
            BarEntry(5f, 8f),
            BarEntry(6f, 2f)
        )

        // Create dataset
        val dataSet = BarDataSet(entries, "Workouts per week")
        dataSet.color = context?.let { ContextCompat.getColor(it, R.color.gray) }!!;

        // Assign dataset to BarData
        val barData = BarData(dataSet)
        workoutBarChart.data = barData

        // Customize X-axis
        val xAxis = workoutBarChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))

        // Customize Y-axis
        workoutBarChart.axisLeft.axisMinimum = 0f
        workoutBarChart.axisLeft.axisMaximum = 10f
        workoutBarChart.axisRight.isEnabled = false

        // Customize chart appearance
        workoutBarChart.setFitBars(true)
        workoutBarChart.description = Description().apply { text = "" }  // Remove description text
        workoutBarChart.animateY(1000)  // Smooth animation
        workoutBarChart.invalidate()  // Refresh chart

    }
}