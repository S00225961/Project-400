package com.example.project400

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat

class TermsAndConditions : AppCompatActivity() {
    private lateinit var termsAndConditionsTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        //change navbar color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.navbar))

        var termsAndConditions = findViewById<TextView>(R.id.termsAndConditions)
        termsAndConditions.text = getString(R.string.terms_and_conditions)

        val acceptButton = findViewById<Button>(R.id.acceptButton)
        val declineButton = findViewById<Button>(R.id.declineButton)

        acceptButton.setOnClickListener {
            //setAcceptedTerms(this)
            // TODO: trigger DB setup here
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        declineButton.setOnClickListener {
            finishAffinity()
        }
    }
}