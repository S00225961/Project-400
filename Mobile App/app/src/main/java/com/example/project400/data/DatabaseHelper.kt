package com.example.project400.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "neruopi_fitness.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE user (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                age INTEGER,
                weight REAL,
                height REAL,
                bmi REAL
            );
        """.trimIndent()

        val createHistoryTable = """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                workout_name TEXT,
                exercises_json TEXT
            );
        """.trimIndent()

        db.execSQL(createUserTable)
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS user")
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }
    //User
    fun insertUser(
        username: String,
        age: Int,
        weight: Double,
        height: Double,
        bmi: Double
    ): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put("username", username)
        contentValues.put("age", age)
        contentValues.put("weight", weight)
        contentValues.put("height", height)
        contentValues.put("bmi", bmi)

        return db.insert("user", null, contentValues)
    }

    fun getUser(): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM user LIMIT 1", null)

        var user: User? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val age = cursor.getInt(cursor.getColumnIndexOrThrow("age"))
            val weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))
            val height = cursor.getDouble(cursor.getColumnIndexOrThrow("height"))
            val bmi = cursor.getDouble(cursor.getColumnIndexOrThrow("bmi"))

            user = User(id, username, age, weight, height, bmi)
        }

        cursor.close()
        db.close()

        return user
    }

    //Workout
    fun insertWorkout(workout: Workout): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        // Create a combined JSON of exercises and sets
        val exercisesJson = JSONObject().apply {
            put("exercises", JSONArray(workout.exercises))
            put("sets", JSONArray(workout.sets))
            put("weight", workout.weight)
            put("time", workout.time)
            put("prs", workout.prs)
        }.toString()

        contentValues.put("date", workout.date)
        contentValues.put("workout_name", workout.name)
        contentValues.put("exercises_json", exercisesJson)

        return db.insert("history", null, contentValues)
    }

    fun getAllWorkouts(): List<Workout> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM history", null)
        val workouts = mutableListOf<Workout>()

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("workout_name"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val exercisesJson = cursor.getString(cursor.getColumnIndexOrThrow("exercises_json"))

                val json = JSONObject(exercisesJson)
                val exercises = mutableListOf<String>()
                val sets = mutableListOf<String>()

                val exercisesArray = json.getJSONArray("exercises")
                for (i in 0 until exercisesArray.length()) {
                    exercises.add(exercisesArray.getString(i))
                }

                val setsArray = json.getJSONArray("sets")
                for (i in 0 until setsArray.length()) {
                    sets.add(setsArray.getString(i))
                }

                val weight = json.optString("weight", "")
                val time = json.optString("time", "")
                val prs = json.optInt("prs", 0)

                workouts.add(
                    Workout(
                        name = name,
                        date = date,
                        weight = weight,
                        time = time,
                        prs = prs,
                        exercises = exercises,
                        sets = sets
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()
        return workouts
    }

    fun deleteAllFromTable(tableName: String) {
        val db = this.writableDatabase
        db.delete(tableName, null, null)
        db.close()
    }

    fun getWorkoutsForLast7Days(): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val calendar = Calendar.getInstance()
        val today = formatter.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val sevenDaysAgo = formatter.format(calendar.time)

        val query = "SELECT * FROM history WHERE date BETWEEN ? AND ?"
        val cursor = db.rawQuery(query, arrayOf(sevenDaysAgo, today))

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("workout_name"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val exercisesJson = cursor.getString(cursor.getColumnIndexOrThrow("exercises_json"))

                val json = JSONObject(exercisesJson)
                val exercises = mutableListOf<String>()
                val sets = mutableListOf<String>()

                val exercisesArray = json.getJSONArray("exercises")
                for (i in 0 until exercisesArray.length()) {
                    exercises.add(exercisesArray.getString(i))
                }

                val setsArray = json.getJSONArray("sets")
                for (i in 0 until setsArray.length()) {
                    sets.add(setsArray.getString(i))
                }

                val weight = json.optString("weight", "")
                val time = json.optString("time", "")
                val prs = json.optInt("prs", 0)

                workouts.add(
                    Workout(
                        name = name,
                        date = date,
                        weight = weight,
                        time = time,
                        prs = prs,
                        exercises = exercises,
                        sets = sets
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()

        return workouts
    }

}
