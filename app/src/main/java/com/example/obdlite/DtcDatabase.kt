package com.example.obdlite

object DtcDatabase {
    // Kamus offline untuk menerjemahkan kode DTC ke Bahasa Indonesia
    private val dictionary = mapOf(
        // --- MASALAH ALIRAN UDARA (MAF/MAP) ---
        "P0100" to "Sirkuit Sensor Aliran Udara (MAF) Bermasalah",
        "P0101" to "Rentang/Kinerja Sensor Aliran Udara (MAF) Tidak Wajar",
        "P0102" to "Sinyal Input Sensor Aliran Udara (MAF) Terlalu Rendah",
        "P0103" to "Sinyal Input Sensor Aliran Udara (MAF) Terlalu Tinggi",
        
        // --- MASALAH SENSOR SUHU ---
        "P0110" to "Sirkuit Sensor Suhu Udara Masuk (IAT) Bermasalah",
        "P0113" to "Sinyal Input Sensor Suhu Udara Masuk (IAT) Terlalu Tinggi",
        "P0115" to "Sirkuit Sensor Suhu Air Pendingin (ECT) Bermasalah",
        "P0117" to "Sinyal Input Sensor Suhu Air Pendingin Terlalu Rendah",
        "P0118" to "Sinyal Input Sensor Suhu Air Pendingin Terlalu Tinggi",
        "P0125" to "Suhu Pendingin Tidak Cukup untuk Mode Closed Loop",
        
        // --- MASALAH SENSOR POSISI THROTTLE (GAS) ---
        "P0120" to "Sirkuit Sensor Posisi Throttle/Gas (TPS) Bermasalah",
        "P0121" to "Rentang/Kinerja Sensor Posisi Throttle Tidak Wajar",
        "P0122" to "Sinyal Input Sensor Posisi Throttle Terlalu Rendah",
        "P0123" to "Sinyal Input Sensor Posisi Throttle Terlalu Tinggi",
        
        // --- MASALAH SENSOR OKSIGEN (O2 SENSOR) ---
        "P0130" to "Sirkuit Sensor Oksigen (O2) Bermasalah (Bank 1 Sensor 1)",
        "P0131" to "Tegangan Sensor Oksigen (O2) Terlalu Rendah (Bank 1 Sensor 1)",
        "P0132" to "Tegangan Sensor Oksigen (O2) Terlalu Tinggi (Bank 1 Sensor 1)",
        "P0133" to "Respon Sensor Oksigen (O2) Lambat (Bank 1 Sensor 1)",
        "P0134" to "Tidak Ada Aktivitas di Sirkuit Sensor Oksigen (O2)",
        "P0135" to "Sirkuit Pemanas (Heater) Sensor Oksigen (O2) Bermasalah",
        "P0141" to "Sirkuit Pemanas (Heater) Sensor Oksigen (O2) Bermasalah (Bank 1 Sensor 2)",
        
        // --- CAMPURAN BAHAN BAKAR (LEAN/RICH) ---
        "P0171" to "Campuran Bahan Bakar Terlalu Miskin / Lean (Bank 1)",
        "P0172" to "Campuran Bahan Bakar Terlalu Kaya / Rich (Bank 1)",
        "P0174" to "Campuran Bahan Bakar Terlalu Miskin / Lean (Bank 2)",
        "P0175" to "Campuran Bahan Bakar Terlalu Kaya / Rich (Bank 2)",
        
        // --- MASALAH INJEKTOR & POMPA BENSIN ---
        "P0200" to "Sirkuit Injektor Bermasalah",
        "P0201" to "Sirkuit Injektor Bermasalah - Silinder 1",
        "P0202" to "Sirkuit Injektor Bermasalah - Silinder 2",
        "P0203" to "Sirkuit Injektor Bermasalah - Silinder 3",
        "P0204" to "Sirkuit Injektor Bermasalah - Silinder 4",
        "P0230" to "Sirkuit Utama Pompa Bahan Bakar Bermasalah",
        
        // --- MASALAH MISFIRE (MESIN GEGAR/NDUT-NDUTAN) ---
        "P0300" to "Terdeteksi Misfire Acak / Beberapa Silinder (Mesin Brebet)",
        "P0301" to "Terdeteksi Misfire pada Silinder 1",
        "P0302" to "Terdeteksi Misfire pada Silinder 2",
        "P0303" to "Terdeteksi Misfire pada Silinder 3",
        "P0304" to "Terdeteksi Misfire pada Silinder 4",
        
        // --- MASALAH SENSOR POSISI (CRANKSHAFT/CAMSHAFT) ---
        "P0325" to "Sirkuit Sensor Ketukan (Knock Sensor) Bermasalah",
        "P0335" to "Sirkuit Sensor Posisi Crankshaft (Poros Engkol) Bermasalah",
        "P0340" to "Sirkuit Sensor Posisi Camshaft (Poros Noken As) Bermasalah",
        
        // --- MASALAH EGR & KATALIS ---
        "P0400" to "Aliran Sistem EGR (Exhaust Gas Recirculation) Bermasalah",
        "P0401" to "Aliran Sistem EGR Terlalu Rendah (Tersumbat)",
        "P0402" to "Aliran Sistem EGR Terlalu Tinggi",
        "P0420" to "Efisiensi Katalis di Bawah Ambang Batas (Bank 1) - Katalik Rusak",
        "P0430" to "Efisiensi Katalis di Bawah Ambang Batas (Bank 2) - Katalik Rusak",
        
        // --- MASALAH EVAP (PENGUAPAN BENSIN) ---
        "P0440" to "Sistem Kontrol Emisi Penguapan (EVAP) Bermasalah",
        "P0442" to "Terdeteksi Kebocoran Kecil pada Sistem EVAP",
        "P0443" to "Sirkuit Katup Kontrol EVAP Bermasalah",
        "P0455" to "Terdeteksi Kebocoran Besar pada Sistem EVAP (Tutup Tangki Bensin?)",
        
        // --- MASALAH IDLE (PUTARAN STASIONER) & KECEPATAN ---
        "P0500" to "Sensor Kecepatan Kendaraan (VSS) Bermasalah",
        "P0505" to "Sistem Kontrol Idle (Stasioner) Bermasalah",
        "P0506" to "Putaran Idle Lebih Rendah dari Standar",
        "P0507" to "Putaran Idle Lebih Tinggi dari Standar",
        
        // --- MASALAH KELISTRIKAN & KOMPUTER (ECU) ---
        "P0560" to "Sistem Tegangan Bermasalah",
        "P0562" to "Tegangan Sistem Terlalu Rendah (Aki Lemah?)",
        "P0563" to "Tegangan Sistem Terlalu Tinggi (Alternator Overcharge?)",
        "P0600" to "Sirkuit Komunikasi Serial Bermasalah",
        "P0601" to "Error Checksum Memori Modul Kontrol (ECU Rusak)",
        
        // --- MASALAH TRANSMISI MATIC ---
        "P0700" to "Sistem Kontrol Transmisi Bermasalah",
        "P0705" to "Sirkuit Sensor Rentang Transmisi Bermasalah",
        "P0715" to "Sirkuit Sensor Kecepatan Input/Turbin Bermasalah",
        "P0720" to "Sirkuit Sensor Kecepatan Output Bermasalah",
        "P0730" to "Rasio Gigi Tidak Sesuai (Transmisi Selip)",
        "P0740" to "Sirkuit Kopling Torque Converter Bermasalah",
        "P0750" to "Solenoid Shift A Bermasalah",
        "P0755" to "Solenoid Shift B Bermasalah",
        
        // --- KODE SPESIFIK (PABRIKAN TERTENTU) ---
        "P1101" to "Sensor Aliran Udara (MAF) di Luar Rentang Uji Mandiri",
        "P1133" to "Sensor Oksigen (O2) Bank 1 Sensor 1 di Luar Rentang",
        "P1189" to "Sirkuit Saklar Tekanan Oli Mesin Bermasalah",
        "P1235" to "Kontrol Pompa Bahan Bakar di Luar Rentang",
        "P1604" to "Masalah Kemampuan Start (Mesin Susah Distarter)",
        
        // --- MASALAH THROTTLE ELEKTRONIK (E-THROTTLE) ---
        "P2100" to "Sirkuit Motor Aktuator Throttle Terbuka / Putus",
        "P2101" to "Rentang/Kinerja Motor Aktuator Throttle Bermasalah",
        "P2135" to "Korelasi Tegangan Sensor Posisi Throttle A/B Bermasalah",
        
        // --- MASALAH SISTEM PENDINGIN ---
        "P2181" to "Kinerja Sistem Pendingin Bermasalah (Thermostat?)",
        "P2270" to "Sinyal Sensor Oksigen Macet di Posisi Lean",
        "P2271" to "Sinyal Sensor Oksigen Macet di Posisi Rich",
        
        // --- KODE U & C (SISTEM MODUL & ABS) ---
        "U0001" to "Bus Komunikasi CAN Kecepatan Tinggi Bermasalah",
        "U0100" to "Kehilangan Komunikasi dengan Modul ECM/PCM",
        "U0121" to "Kehilangan Komunikasi dengan Modul ABS",
        "C0035" to "Sirkuit Sensor Kecepatan Roda Kiri Depan Bermasalah",
        "C0040" to "Sirkuit Sensor Kecepatan Roda Kanan Depan Bermasalah",
        "B0001" to "Kontrol Pengembangan (Airbag) Pengemudi Tahap 1 Bermasalah"
    )

    /**
     * Menerjemahkan kode DTC menjadi deskripsi Bahasa Indonesia.
     */
    fun getDescription(code: String): String {
        return dictionary[code] ?: "Kode error tidak dikenali (Silakan cari referensi kode ini)"
    }
}
