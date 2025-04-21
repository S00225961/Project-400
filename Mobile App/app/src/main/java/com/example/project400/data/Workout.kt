package com.example.project400.data

data class Workout(val name: String, val date: String, val weight: String, val time: String, var prs: Int, val exercises: List<String>, val sets: List<String>)
