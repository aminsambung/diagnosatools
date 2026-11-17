package com.example.obdlite

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.obdlite.ui.DashboardScreen
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    private lateinit var bt: BluetoothManager
    private lateinit var obd: ObdService

    private var connected by mutableStateOf(false)
    private var rpm by mutableStateOf<Int?>(null)
    private var speed by mutableStateOf<Int?>(null)
    private var coolant by mutableStateOf<Int?>(null)
    private var throttle by mutableStateOf<Int?>(null) // State baru

    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            doConnect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() 

        bt = BluetoothManager(this)
        obd = ObdService(bt)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = Color(0xFF121212) 
                ) {
                    DashboardScreen(
                        connected = connected,
                        rpm = rpm,
                        speed = speed,
                        coolant = coolant,
                        throttle = throttle, // Kirim ke UI
                        onConnect = { requestPermissions() },
                        onDisconnect = { disconnect() }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val perms = if (Build.VERSION.SDK_INT >= 31) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permLauncher.launch(perms)
    }

    private fun doConnect() {
        scope.launch {
            val success = bt.connectToFirstPaired()
            
            if (success) {
                obd.init() 
                withContext(Dispatchers.Main) {
                    connected = true
                }
                startPolling()
            } else {
                withContext(Dispatchers.Main) {
                    connected = false
                }
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                val r = obd.getRpm()
                val s = obd.getSpeed()
                val c = obd.getCoolantTemp()
                val t = obd.getThrottle() // Baca throttle

                withContext(Dispatchers.Main) {
                    rpm = r
                    speed = s
                    coolant = c
                    throttle = t
                }
                
                delay(500) 
            }
        }
    }

    private fun disconnect() {
        pollJob?.cancel()
        scope.launch {
            bt.disconnect()
            withContext(Dispatchers.Main) {
                connected = false
                rpm = null
                speed = null
                coolant = null
                throttle = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bt.disconnect()
        scope.cancel()
    }
}
