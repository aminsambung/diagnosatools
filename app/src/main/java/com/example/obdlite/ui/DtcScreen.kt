package com.example.obdlite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obdlite.DtcData

@Composable
fun DtcScreen(
    dtcList: List<DtcData>,
    isScanning: Boolean,
    onScan: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit
) {
    // State untuk mode tampilan: MENU, HASIL, atau KONFIRMASI_HAPUS
    var currentView by remember { mutableStateOf("MENU") } 

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            text = "Diagnostik Mesin",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )

        when (currentView) {
            "MENU" -> {
                Text("Pilih Tindakan:", color = Color.LightGray, modifier = Modifier.padding(bottom = 16.dp))
                
                // Tombol Tampilkan Error
                Button(
                    onClick = { 
                        onScan()
                        currentView = "HASIL" 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tampilkan Error (Show logged faults)", color = Color(0xFF64B5F6), fontSize = 16.sp)
                }

                // Tombol Hapus Error
                Button(
                    onClick = { currentView = "KONFIRMASI_HAPUS" },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hapus Error (Clear logged faults)", color = Color(0xFF64B5F6), fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kembali ke Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            "HASIL" -> {
                if (isScanning) {
                    CircularProgressIndicator(color = Color(0xFF2196F3), modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Memindai mobil...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
                } else {
                    if (dtcList.isEmpty()) {
                        Text(
                            text = "Tidak ada kode error. Mesin dalam kondisi baik.",
                            color = Color(0xFF4CAF50),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(dtcList) { dtc ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(dtc.code, color = Color(0xFFFF5252), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(dtc.description, color = Color.LightGray, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { currentView = "MENU" },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Kembali ke Menu", color = Color.White)
                }
            }

            "KONFIRMASI_HAPUS" -> {
                Text(
                    text = "Peringatan!",
                    color = Color(0xFFFF5252),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Tindakan ini akan menghapus semua riwayat error di komputer mobil (ECU) dan mematikan lampu Check Engine. Pastikan kamu sudah mencatat kode errornya.\n\nLanjutkan?",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { currentView = "MENU" },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = { 
                            onClear()
                            currentView = "MENU" // Kembali ke menu setelah hapus
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Ya, Hapus!")
                    }
                }
            }
        }
    }
}
