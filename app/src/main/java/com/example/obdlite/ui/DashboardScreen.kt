package com.example.obdlite.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    connected: Boolean,
    rpm: Int?,
    speed: Int?,
    coolant: Int?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    // Warna dasar mirip LCD motor
    val bgColor = Color(0xFFF0F0F0) // Light gray/white LCD
    val textColor = Color(0xFF000000)
    val accentColor = Color(0xFF000000) // Jarum dan speed
    val redZoneColor = Color(0xFFE53935)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Background HP hitam
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Status
        Text(
            text = if (connected) "● TERHUBUNG" else "○ TERPUTUS",
            color = if (connected) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Panel LCD Utama
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f) // Rasio mirip layar LCD motor
                .background(bgColor, RoundedCornerShape(12.dp))
                .border(8.dp, Color(0xFF222222), RoundedCornerShape(12.dp))
        ) {
            // 1. RPM Gauge (Melengkung di atas)
            RpmGauge(
                rpm = rpm ?: 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(top = 16.dp),
                redZoneColor = redZoneColor,
                accentColor = accentColor
            )

            // 2. Kecepatan (Angka Besar di Tengah)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (speed != null) "$speed" else "0",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                Text(
                    text = "km/h",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            // 3. Suhu Air (Indikator kecil di kiri bawah)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            ) {
                Text("SUHU", fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = if (coolant != null) "$coolant°C" else "--°C",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if ((coolant ?: 0) > 90) redZoneColor else textColor
                )
            }
            
            // Indikator Bensin (Dummy visual aja)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                Text("FUEL", fontSize = 10.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Box(modifier = Modifier.width(6.dp).height(20.dp).background(Color.Green))
                    Box(modifier = Modifier.width(6.dp).height(15.dp).background(Color.Green))
                    Box(modifier = Modifier.width(6.dp).height(10.dp).background(Color.Gray))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Tombol Koneksi
        Button(
            onClick = if (connected) onDisconnect else onConnect,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (connected) Color(0xFFF44336) else Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (connected) "Putuskan Koneksi" else "Hubungkan Adaptor",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RpmGauge(
    rpm: Int,
    modifier: Modifier = Modifier,
    redZoneColor: Color,
    accentColor: Color
) {
    val maxRpm = 12000
    val currentRpm = rpm.coerceIn(0, maxRpm)
    // Sudut dari -180 derajat (kiri) ke 0 derajat (kanan)
    val startAngle = 180f
    val sweepAngle = 180f
    
    // Posisi jarum (dari 0 sampai 1)
    val needleRatio = currentRpm.toFloat() / maxRpm

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val radius = width / 2.2f
            val center = Offset(width / 2, height)
            
            // Gambar track background gauge abu-abu
            drawArc(
                color = Color.LightGray,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )

            // Gambar red zone (zona merah di RPM tinggi)
            drawArc(
                color = redZoneColor,
                startAngle = startAngle + (sweepAngle * 0.8f),
                sweepAngle = sweepAngle * 0.2f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )

            // Gambar jarum
            val angleRad = Math.toRadians((startAngle + (sweepAngle * needleRatio)).toDouble())
            val needleLength = radius - 20f
            val needleEnd = Offset(
                x = center.x + (needleLength * cos(angleRad)).toFloat(),
                y = center.y + (needleLength * sin(angleRad)).toFloat()
            )
            
            drawLine(
                color = accentColor,
                start = center,
                end = needleEnd,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
            
            // Titik tengah jarum
            drawCircle(color = accentColor, radius = 12f, center = center)
            drawCircle(color = Color.White, radius = 6f, center = center)
        }
        
        // Angka RPM
        Text(
            text = "${currentRpm / 1000}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
