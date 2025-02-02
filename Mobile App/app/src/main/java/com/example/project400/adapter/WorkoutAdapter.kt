package com.example.project400.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project400.R
import com.example.project400.data.Workout
import org.w3c.dom.Text

class WorkoutAdapter(private val workoutList: List<Workout>) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_tile, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workoutList[position]
        holder.bind(workout)
    }

    override fun getItemCount(): Int {
        return workoutList.size
    }

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workoutName: TextView = itemView.findViewById(R.id.workoutName)
        private val workoutDate: TextView = itemView.findViewById(R.id.workoutDate)
        private val weightTextView: TextView = itemView.findViewById(R.id.weightTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val prTextView: TextView = itemView.findViewById(R.id.prTextView)
        private val exerciseListView: ListView = itemView.findViewById(R.id.exercisesListView)
        private val bestSetListView: ListView = itemView.findViewById(R.id.bestSetListView)

        fun bind(workout: Workout) {
            //text views
            weightTextView.text = "Weight: ${workout.weight}"
            timeTextView.text = "Time: ${workout.time}"
            prTextView.text = "PRs: ${workout.prs}"
            workoutName.text = workout.name
            workoutDate.text = workout.date.toString()

            //adapters
            val exerciseAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_list_item_1, workout.exercises)
            val bestSetAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_list_item_1, workout.sets)

            exerciseListView.adapter = exerciseAdapter
            bestSetListView.adapter = bestSetAdapter

        }
    }
}
