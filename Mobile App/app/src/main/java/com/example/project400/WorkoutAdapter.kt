package com.example.project400

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        private val weightTextView: TextView = itemView.findViewById(R.id.weightTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val prTextView: TextView = itemView.findViewById(R.id.prTextView)
        private val exercisesTextView: TextView = itemView.findViewById(R.id.exercisesTextView)

        fun bind(workout: Workout) {
            weightTextView.text = "Weight: ${workout.weight}"
            timeTextView.text = "Time: ${workout.time}"
            prTextView.text = "PRs: ${workout.prs}"
            exercisesTextView.text = "Exercises: ${workout.exercises.joinToString(", ")}"
        }
    }
}
