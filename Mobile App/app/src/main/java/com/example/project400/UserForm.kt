package com.example.project400

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.project400.data.DatabaseHelper

class UserForm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_form)

        //change navbar color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.navbar))

        val submitButton = findViewById<Button>(R.id.submitButton)
        val usernameEditText = findViewById<EditText>(R.id.enterUsername)
        val heightEditText = findViewById<EditText>(R.id.enterHeight)
        val weightEditText = findViewById<EditText>(R.id.enterWeight)
        val ageEditText = findViewById<EditText>(R.id.enterAge)

        submitButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val heightStr = heightEditText.text.toString().trim()
            val weightStr = weightEditText.text.toString().trim()
            val ageStr = ageEditText.text.toString().trim()

            if (username.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val height = heightStr.toDouble()
                val weight = weightStr.toDouble()
                val age = ageStr.toInt()

                if (height <= 0 || weight <= 0 || age <= 0) {
                    Toast.makeText(this, "Height, weight, and age must be positive numbers", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val bmi = weight / (height * height)

                // Delete previous user
                val dbHelper = DatabaseHelper(this)
                dbHelper.deleteAllFromTable("user")

                // Insert new user
                dbHelper.insertUser(username, age, weight, height, bmi)

                startActivity(Intent(this, MainActivity::class.java))
                finish()

            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers for height, weight, and age", Toast.LENGTH_SHORT).show()
            }
        }
    }

}