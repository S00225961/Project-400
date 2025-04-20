package com.example.project400.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project400.R
import com.example.project400.data.Exercise

class ExerciseAdapter(private val exerciseList: List<Exercise>, private val onItemClick: (Exercise) -> Unit): RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val workout = exerciseList[position]
        holder.bind(workout)
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }
    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val nameTextView: TextView = itemView.findViewById(R.id.exerciseNameTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.exerciseTypeTextView)
        fun bind(exercise: Exercise){
            val exType: String = exercise.type.toString()
            val exVariation: String = exercise.variation.toString()

            var typeTextOutput = ""
            if(exVariation.equals("None")){
                typeTextOutput = "$exType"
            } else {
                typeTextOutput = "$exType ($exVariation)"
            }

            nameTextView.text = exercise.name
            typeTextView.text = typeTextOutput

            itemView.setOnClickListener {
                onItemClick(exercise)
            }
        }
    }
}