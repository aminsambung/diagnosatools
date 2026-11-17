package com.example.obdlite

class ObdService(private val bt: BluetoothManager) {

    /** Inisialisasi ELM327 */
    fun init(): Boolean {
        bt.send("ATZ")     // reset
        bt.send("ATE0")    // echo off
        bt.send("ATL0")    // linefeed off
        bt.send("ATSP0")   // auto protocol
        return true
    }

    /** PID 0C: Engine RPM */
    fun getRpm(): Int? {
        val raw = bt.send("010C")
        // respon: "41 0C 1A F8"
        val bytes = parseHexBytes(raw, prefix = "410C") ?: return null
        if (bytes.size < 2) return null
        val a = bytes[0]; val b = bytes[1]
        return ((a * 256) + b) / 4
    }
    
    /** PID 11: Throttle Position (0-100%) */
    fun getThrottle(): Int? {
        val raw = bt.send("0111")
        val bytes = parseHexBytes(raw, prefix = "4111") ?: return null
        val a = bytes.firstOrNull() ?: return null
        // Rumus: (A * 100) / 255
        return (a * 100) / 255
    }
    /** PID 0D: Vehicle Speed (km/h) */
    fun getSpeed(): Int? {
        val raw = bt.send("010D")
        val bytes = parseHexBytes(raw, prefix = "410D") ?: return null
        return bytes.firstOrNull()
    }

    /** PID 05: Coolant temp (°C) */
    fun getCoolantTemp(): Int? {
        val raw = bt.send("0105")
        val bytes = parseHexBytes(raw, prefix = "4105") ?: return null
        return bytes.firstOrNull()?.minus(40)
    }

    private fun parseHexBytes(raw: String, prefix: String): IntArray? {
        val clean = raw.replace(" ", "").replace("\r", "").replace("\n", "").uppercase()
        val idx = clean.indexOf(prefix)
        if (idx < 0) return null
        val data = clean.substring(idx + prefix.length)
        return data.chunked(2)
            .mapNotNull { it.toIntOrNull(16) }
            .toIntArray()
    }
} 
