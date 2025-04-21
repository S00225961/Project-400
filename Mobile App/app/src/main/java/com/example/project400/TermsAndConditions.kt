package com.example.project400

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.project400.data.DatabaseHelper

class TermsAndConditions : AppCompatActivity() {
    private lateinit var termsAndConditionsTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        //check db creation
        if(isDatabaseCreated(this, "neruopi_fitness.db")){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //change navbar color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.navbar))

        var termsAndConditions = findViewById<TextView>(R.id.termsAndConditions)
        termsAndConditions.text = getString(R.string.terms_and_conditions)

        val acceptButton = findViewById<Button>(R.id.acceptButton)
        val declineButton = findViewById<Button>(R.id.declineButton)

        acceptButton.setOnClickListener {
            val dbHelper = DatabaseHelper(this)
            dbHelper.writableDatabase

            startActivity(Intent(this, UserForm::class.java))
            finish()
        }

        declineButton.setOnClickListener {
            finishAffinity()
        }
    }

    fun isDatabaseCreated(context: Context, dbName: String): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        return dbFile.exists()
    }
}