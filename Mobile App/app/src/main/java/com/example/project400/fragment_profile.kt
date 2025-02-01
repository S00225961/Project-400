package com.example.project400

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment_profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment_profile : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment fragment_profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

//        // Sample workout data (x: Week Number, y: Workout Count)
//        val entries = listOf(
//            BarEntry(1f, 3f),  // Week 1: 3 workouts
//            BarEntry(2f, 5f),  // Week 2: 5 workouts
//            BarEntry(3f, 2f),  // Week 3: 2 workouts
//            BarEntry(4f, 6f),  // Week 4: 6 workouts
//            BarEntry(5f, 4f)   // Week 5: 4 workouts
//        )
//
//        val dataSet = BarDataSet(entries, "Workouts per Week")
//        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
//        dataSet.valueTextColor = Color.WHITE
//        dataSet.valueTextSize = 12f
//
//        val barData = BarData(dataSet)
//        barChart.data = barData
//
//        val xAxis = chart.xAxis
//        xAxis.position = XAxis.XAxisPosition.BOTTOM // Set position
//        xAxis.granularity = 1f // Minimum interval
//        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")) // Custom labels
//
//
//        // Customize chart appearance
//        barChart.setFitBars(true)
//        barChart.description = Description().apply { text = "" }  // Remove description text
//        barChart.animateY(1000)  // Smooth animation
//        barChart.invalidate()  // Refresh chart
    }
}