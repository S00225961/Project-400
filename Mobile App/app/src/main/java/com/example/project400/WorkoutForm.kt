package com.example.project400

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.project400.data.DatabaseHelper
import com.example.project400.data.Workout
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar

class WorkoutForm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_form)

        //change navbar color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.navbar))

        var enterWorkoutName = findViewById<EditText>(R.id.enterWorkoutName)
        var enterDate = findViewById<EditText>(R.id.enterDate)
        var workoutTimeHours = findViewById<EditText>(R.id.workoutTimeHours)
        var workoutTimeMinutes = findViewById<EditText>(R.id.workoutTimeMinutes)

        enterDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "${selectedDay.toString().padStart(2, '0')}/${(selectedMonth + 1).toString().padStart(2, '0')}/$selectedYear"
                enterDate.setText(date)
            }, year, month, day)

            datePickerDialog.show()
        }

        val exerciseNames = loadPoseLabels(this, "pose_labels.txt")

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, exerciseNames)
        val exerciseDropdown0 = findViewById<AutoCompleteTextView>(R.id.exerciseNameAutoCompleteTextView)
        exerciseDropdown0.setAdapter(adapter)

        var weightEditText0 = findViewById<EditText>(R.id.weightEditText)
        var setsEditText0 = findViewById<EditText>(R.id.setsEditText)

        var addExerciseBtn = findViewById<Button>(R.id.addExerciseButton)
        var submitBtn = findViewById<Button>(R.id.submitButton)

        var linearLayoutContainer = findViewById<LinearLayout>(R.id.containerLayout)

        var exerciseCount = 1

        val exerciseNameList = mutableListOf<EditText>()
        val weightList = mutableListOf<EditText>()
        val setsList = mutableListOf<EditText>()

        addExerciseBtn.setOnClickListener {
            val newRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
            }

            val exerciseDropdown = AutoCompleteTextView(this).apply {
                id = View.generateViewId()
                hint = "Exercise"
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                setHintTextColor(ContextCompat.getColor(context, R.color.white))
                inputType = InputType.TYPE_CLASS_TEXT
                setPadding(4, 4, 4, 4)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)

                val labels = loadPoseLabels(context, "pose_labels.txt")
                val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, labels)
                setAdapter(adapter)
            }

            val weightEditText = EditText(this).apply {
                id = View.generateViewId()
                hint = "Weight"
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                setHintTextColor(ContextCompat.getColor(context, R.color.white))
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setPadding(4, 4, 4, 4)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 4
                }
            }

            val setsEditText = EditText(this).apply {
                id = View.generateViewId()
                hint = "Sets"
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                setHintTextColor(ContextCompat.getColor(context, R.color.white))
                inputType = InputType.TYPE_CLASS_NUMBER
                setPadding(4, 4, 4, 4)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 4
                }
            }

            newRow.addView(exerciseDropdown)
            newRow.addView(weightEditText)
            newRow.addView(setsEditText)

            val index = linearLayoutContainer.indexOfChild(addExerciseBtn)
            linearLayoutContainer.addView(newRow, index)

            exerciseNameList.add(exerciseDropdown)
            weightList.add(weightEditText)
            setsList.add(setsEditText)

            exerciseCount++
        }

        submitBtn.setOnClickListener {
            val workoutName = enterWorkoutName.text.toString().trim()
            val workoutDate = enterDate.text.toString().trim()
            var workoutTime = ""

            val timeHoursText = workoutTimeHours.text.toString().trim()
            val timeMinutesText = workoutTimeMinutes.text.toString().trim()

            if (timeHoursText.isEmpty() || timeMinutesText.isEmpty()) {
                Toast.makeText(this, "Please enter valid workout time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timeHours = timeHoursText.toIntOrNull()
            val timeMinutes = timeMinutesText.toIntOrNull()

            if (timeHours == null || timeMinutes == null) {
                Toast.makeText(this, "Please enter valid numeric values for time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (timeHours > 60 || timeMinutes > 60) {
                Toast.makeText(this, "Please enter a valid time", Toast.LENGTH_SHORT).show()
                Log.d("Time", "Please enter a valid time")
                return@setOnClickListener
            } else {
                workoutTime = "$timeHours hrs $timeMinutes mins"
            }

            if (workoutName.isEmpty() || workoutDate.isEmpty()) {
                Toast.makeText(this, "Please enter workout name and date", Toast.LENGTH_SHORT).show()
                Log.d("Name and Date", "Please enter workout name and date")
                return@setOnClickListener
            }

            val exerciseNames = mutableListOf<AutoCompleteTextView>()
            val setDescriptions = mutableListOf<String>()
            var totalWeightLifted = 0f
            val exercises = mutableListOf<String>()

            // Handle first static inputs
            val exerciseName0 = exerciseDropdown0.text.toString()
            val weight0 = weightEditText0.text.toString().toFloatOrNull() ?: 0f
            val sets0 = setsEditText0.text.toString().toIntOrNull() ?: 0

            if (exerciseName0.isNotEmpty()) {
                exerciseNames.add(exerciseDropdown0)
                exercises.add(exerciseName0)
                setDescriptions.add("${weight0}kg x $sets0")
                totalWeightLifted += weight0 * sets0
            }

            for (i in exerciseNameList.indices) {
                val exercise = exerciseNameList[i].text.toString()
                val weightText = weightList[i].text.toString()
                val setsText = setsList[i].text.toString()

                if (exercise.isNotBlank() && weightText.isNotBlank() && setsText.isNotBlank()) {
                    exercises.add(exercise)

                    val weight = weightList[i].text.toString().toFloatOrNull() ?: 0f
                    val sets = setsList[i].text.toString().toIntOrNull() ?: 0

                    // Simple string for display
                    setDescriptions.add("${weight}kg x $sets")

                    // Add to total weight lifted
                    totalWeightLifted += weight * sets
                }
            }

            val totalWeightStr = "${"%.1f".format(totalWeightLifted)}kg"

            // Insert into DB
            val dbHelper = DatabaseHelper(this)
            val workout = Workout(workoutName, workoutDate, totalWeightStr, workoutTime, 0, exercises, setDescriptions)
            val pr = calculatePRs(workout)
            workout.prs = pr
            dbHelper.insertWorkout(workout)
            Log.d("Database", "Written to database successfully")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun loadPoseLabels(context: Context, labelsFileName: String): List<String> {
        val labels = mutableListOf<String>()
        context.assets.open(labelsFileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    labels.add(line!!)
                }
            }
        }

        return labels
    }

    fun calculatePRs(currentWorkout: Workout): Int {
        val pastWorkouts = DatabaseHelper(this).getAllWorkouts()
        val bestLifts = mutableMapOf<String, Double>()

        for (workout in pastWorkouts) {
            for ((index, exercise) in workout.exercises.withIndex()) {
                val setDesc = workout.sets.getOrNull(index) ?: continue
                val weightRegex = Regex("""(\d+(\.\d+)?)kg""")
                val match = weightRegex.find(setDesc)

                val weight = match?.value?.replace("kg", "")?.toDoubleOrNull() ?: continue

                val currentBest = bestLifts[exercise] ?: 0.0
                if (weight > currentBest) {
                    bestLifts[exercise] = weight
                }
            }
        }

        var prCount = 0
        for ((index, exercise) in currentWorkout.exercises.withIndex()) {
            val setDesc = currentWorkout.sets.getOrNull(index) ?: continue
            val weightRegex = Regex("""(\d+(\.\d+)?)kg""")
            val match = weightRegex.find(setDesc)

            val weight = match?.value?.replace("kg", "")?.toDoubleOrNull() ?: continue

            val previousBest = bestLifts[exercise] ?: 0.0
            if (weight > previousBest) {
                prCount++
            }
        }

        return prCount
    }

}