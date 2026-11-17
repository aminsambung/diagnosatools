package com.example.obdlite

class ObdService(private val bt: BluetoothManager) {

    fun init(): Boolean {
        bt.send("ATZ")
        bt.send("ATE0")
        bt.send("ATL0")
        bt.send("ATSP0")
        return true
    }

    fun getRpm(): Int? {
        val raw = bt.send("010C")
        val bytes = parseHexBytes(raw, prefix = "410C") ?: return null
        if (bytes.size < 2) return null
        val a = bytes[0]; val b = bytes[1]
        return ((a * 256) + b) / 4
    }

    fun getSpeed(): Int? {
        val raw = bt.send("010D")
        val bytes = parseHexBytes(raw, prefix = "410D") ?: return null
        return bytes.firstOrNull()
    }

    fun getCoolantTemp(): Int? {
        val raw = bt.send("0105")
        val bytes = parseHexBytes(raw, prefix = "4105") ?: return null
        return bytes.firstOrNull()?.minus(40)
    }

    fun getThrottle(): Int? {
        val raw = bt.send("0111")
        val bytes = parseHexBytes(raw, prefix = "4111") ?: return null
        val a = bytes.firstOrNull() ?: return null
        return (a * 100) / 255
    }

    /** Membaca DTC (Mode 03) */
    fun readDtcs(): List<String> {
        val raw = bt.send("03")
        val clean = raw.replace(" ", "").replace("\r", "").replace("\n", "").uppercase()
        val idx = clean.indexOf("43")
        if (idx < 0 || clean.length < idx + 4) return emptyList()

        val data = clean.substring(idx + 2)
        val dtcs = mutableListOf<String>()
        
        for (i in 0 until data.length - 3 step 4) {
            val code = data.substring(i, i + 4)
            if (code != "0000") {
                dtcs.add(parseDtcCode(code))
            }
        }
        return dtcs
    }

    /** Menghapus DTC (Mode 04) */
    fun clearDtcs(): Boolean {
        val raw = bt.send("04")
        return raw.contains("44")
    }

    private fun parseDtcCode(hex: String): String {
        val firstChar = when (hex[0]) {
            '0' -> 'P'; '1' -> 'C'; '2' -> 'B'; '3' -> 'U'
            else -> 'P'
        }
        val rest = hex.substring(1)
        return "$firstChar$rest"
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
