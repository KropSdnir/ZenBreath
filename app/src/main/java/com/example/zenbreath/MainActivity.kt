package com.example.zenbreath

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.zenbreath.ui.screens.HomeScreen
import com.example.zenbreath.ui.theme.ZenBreathTheme
import com.example.zenbreath.viewmodel.BreathingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for ZenBreath breathing tracking app
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: BreathingViewModel by viewModels()

    // Permission launcher for body sensors
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // No-op - we check permissions again in onResume if needed
    }

    private var hasRequestedPermissions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Only request once per activity session to avoid loops
        if (!hasRequestedPermissions) {
            checkAndRequestPermissionsSilently()
            hasRequestedPermissions = true
        }

        setContent {
            ZenBreathTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Removed checkAndRequestPermissionsSilently from here to prevent loops
    }

    private fun checkAndRequestPermissionsSilently() {
        val permissionsToRequest = mutableListOf<String>()

        // Check if device actually has heart rate sensor before requesting BODY_SENSORS
        val hasHeartRateSensor = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HEART_RATE)
        
        if (hasHeartRateSensor && ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BODY_SENSORS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            try {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to launch permission request", e)
            }
        }
    }
}