package com.example.obdlite

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothManager(private val context: Context) {

    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    /** Mengambil daftar perangkat Bluetooth yang sudah di-pair di HP */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasPermission()) return emptyList()
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        
        return adapter.bondedDevices?.toList() ?: emptyList()
    }

    /** Menghubungkan ke perangkat spesifik yang dipilih oleh pengguna */
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice): Boolean {
        if (!hasPermission()) return false
        
        try {
            // Putuskan koneksi lama jika ada (biar bersih)
            disconnect()
            
            socket = device.createRfcommSocketToServiceRecord(sppUuid)
            BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()
            socket?.connect()
            input = socket?.inputStream
            output = socket?.outputStream
            return true
        } catch (e: Exception) {
            disconnect()
            return false
        }
    }

    fun send(cmd: String): String {
        return try {
            output?.write("$cmd\r".toByteArray())
            output?.flush()
            Thread.sleep(200)
            readResponse()
        } catch (e: Exception) {
            ""
        }
    }

    private fun readResponse(): String {
        val buffer = ByteArray(1024)
        val sb = StringBuilder()
        val timeout = System.currentTimeMillis() + 1000
        while (System.currentTimeMillis() < timeout) {
            if (input?.available() ?: 0 > 0) {
                val n = input!!.read(buffer)
                sb.append(String(buffer, 0, n))
                if (sb.contains(">")) break
            }
        }
        return sb.toString().replace(">", "").trim()
    }

    fun disconnect() {
        try { socket?.close() } catch (_: Exception) {}
        socket = null; input = null; output = null
    }

    private fun hasPermission(): Boolean {
        val perm = if (android.os.Build.VERSION.SDK_INT >= 31)
            Manifest.permission.BLUETOOTH_CONNECT
        else Manifest.permission.BLUETOOTH
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }
}
