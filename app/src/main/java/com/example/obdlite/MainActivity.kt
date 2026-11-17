package com.example.obdlite

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obdlite.ui.DashboardScreen
import com.example.obdlite.ui.DtcScreen
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    private lateinit var bt: BluetoothManager
    private lateinit var obd: ObdService

    // State Data
    private var connected by mutableStateOf(false)
    private var rpm by mutableStateOf<Int?>(null)
    private var speed by mutableStateOf<Int?>(null)
    private var coolant by mutableStateOf<Int?>(null)
    private var throttle by mutableStateOf<Int?>(null)
    
    // State DTC
    private var showDtcScreen by mutableStateOf(false)
    private var dtcList by mutableStateOf<List<DtcData>>(emptyList())
    private var isScanning by mutableStateOf(false)

    // State Bluetooth & Dialog
    private var showDeviceDialog by mutableStateOf(false)
    private var pairedDevices by mutableStateOf<List<BluetoothDevice>>(emptyList())
    private var showResetEcuDialog by mutableStateOf(false)

    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants -> if (grants.values.all { it }) loadPairedDevices() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() 

        bt = BluetoothManager(this)
        obd = ObdService(bt)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    color = Color(0xFF121212) 
                ) {
                    if (showDtcScreen) {
                        DtcScreen(
                            dtcList = dtcList,
                            isScanning = isScanning,
                            onScan = { scanDtcs() },
                            onClear = { clearDtcs() },
                            onBack = { showDtcScreen = false }
                        )
                    } else {
                        DashboardScreen(
                            connected = connected,
                            rpm = rpm, speed = speed, coolant = coolant, throttle = throttle,
                            onConnect = { requestPermissions() },
                            onDisconnect = { disconnect() },
                            onOpenDtc = { 
                                // Cukup buka layar, scan dilakukan dari dalam layar DTC
                                showDtcScreen = true 
                            },
                            onResetEcu = { showResetEcuDialog = true } 
                        )
                    }
                    
                    // Dialog Pilih Perangkat Bluetooth
                    if (showDeviceDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeviceDialog = false },
                            title = { Text("Pilih Adaptor OBD2", color = Color.White) },
                            text = {
                                if (pairedDevices.isEmpty()) {
                                    Text("Tidak ada perangkat yang di-pair. Silakan pair ELM327 di Pengaturan Bluetooth HP.", color = Color.Gray)
                                } else {
                                    LazyColumn {
                                        items(pairedDevices) { device ->
                                            val deviceName = device.name ?: "Unknown Device"
                                            Text(
                                                text = deviceName,
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        showDeviceDialog = false
                                                        connectToSpecificDevice(device)
                                                    }
                                                    .padding(vertical = 12.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showDeviceDialog = false }) {
                                    Text("Batal", color = Color.LightGray)
                                }
                            },
                            containerColor = Color(0xFF2C2C2C)
                        )
                    }

                    // Dialog Konfirmasi Reset ECU
                    if (showResetEcuDialog) {
                        AlertDialog(
                            onDismissRequest = { showResetEcuDialog = false },
                            title = { Text("Peringatan Reset ECU", color = Color.White) },
                            text = { 
                                Text("Tindakan ini akan menghapus memori belajar ECU (Idle & Bahan Bakar) dan mematikan lampu Check Engine. Mesin mungkin akan terasa sedikit aneh (RPM naik-turun) selama beberapa menit karena ECU belajar dari awal.\n\nLanjutkan?", color = Color.LightGray) 
                            },
                            confirmButton = {
                                TextButton(onClick = { 
                                    showResetEcuDialog = false 
                                    performEcuReset()
                                }) {
                                    Text("Ya, Reset", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showResetEcuDialog = false }) {
                                    Text("Batal", color = Color.White)
                                }
                            },
                            containerColor = Color(0xFF2C2C2C)
                        )
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val perms = if (Build.VERSION.SDK_INT >= 31) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permLauncher.launch(perms)
    }

    private fun loadPairedDevices() {
        pairedDevices = bt.getPairedDevices()
        showDeviceDialog = true
    }

    private fun connectToSpecificDevice(device: BluetoothDevice) {
        scope.launch {
            if (bt.connectToDevice(device)) {
                obd.init() 
                withContext(Dispatchers.Main) { connected = true }
                startPolling()
            } else {
                withContext(Dispatchers.Main) { connected = false }
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
                val t = obd.getThrottle()

                withContext(Dispatchers.Main) {
                    rpm = r; speed = s; coolant = c; throttle = t
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
                rpm = null; speed = null; coolant = null; throttle = null
            }
        }
    }

    private fun scanDtcs() {
        if (!connected) return
        scope.launch {
            withContext(Dispatchers.Main) { isScanning = true }
            val result = obd.readDtcs()
            withContext(Dispatchers.Main) {
                dtcList = result
                isScanning = false
            }
        }
    }

    private fun clearDtcs() {
        if (!connected) return
        scope.launch {
            val success = obd.clearDtcs()
            if (success) {
                withContext(Dispatchers.Main) {
                    dtcList = emptyList()
                }
            }
        }
    }

    private fun performEcuReset() {
        if (!connected) return
        scope.launch {
            pollJob?.cancel() // Hentikan polling sementara
            obd.clearDtcs()   // Hapus error
            bt.send("ATZ")    // Restart adaptor ELM327
            startPolling()    // Lanjutkan polling
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bt.disconnect()
        scope.cancel()
    }
}
