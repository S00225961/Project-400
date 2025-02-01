package com.example.project400

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.SurfaceView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.project400.body_tracking.MoveNet
import com.example.project400.hardware.Camera
import com.example.project400.hardware.Device
import com.example.project400.pose_classification.PoseClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.project400.body_tracking.Person
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    /**
     * Shows an error message dialog.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ui bottom navbar
        // Get the NavController correctly using NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

    }

}