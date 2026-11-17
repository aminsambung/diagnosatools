package com.example.obdlite.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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
    val bgColor = Color(0xFFF0F0F0)
    val textColor = Color(0xFF000000)
    val accentColor = Color(0xFF000000)
    val redZoneColor = Color(0xFFE53935)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Beri jarak atas agar tidak tertutup status bar (karena enableEdgeToEdge)
        Spacer(modifier = Modifier.height(24.dp)) 

        Text(
            text = if (connected) "● TERHUBUNG" else "○ TERPUTUS",
            color = if (connected) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Panel LCD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f) // Sedikit lebih kotak
                .background(bgColor, RoundedCornerShape(16.dp))
                .border(10.dp, Color(0xFF222222), RoundedCornerShape(16.dp))
        ) {
            // 1. RPM Gauge (Garis melengkung di atas)
            RpmGauge(
                rpm = rpm ?: 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f) // Ambil 65% area atas
                    .padding(top = 10.dp),
                redZoneColor = redZoneColor,
                accentColor = accentColor
            )

            // 2. Kecepatan (Angka Besar)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 30.dp), // Geser ke bawah agar tidak tertutup jarum
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (speed != null) "$speed" else "0",
                    fontSize = 80.sp,
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

            // 3. Suhu Air (Kiri bawah)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            ) {
                Text("SUHU", fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = if (coolant != null) "$coolant°C" else "--°C",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if ((coolant ?: 0) > 90) redZoneColor else textColor
                )
            }
            
            // 4. Bensin (Kanan bawah)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                Text("FUEL", fontSize = 10.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Box(modifier = Modifier.width(8.dp).height(24.dp).background(Color(0xFF4CAF50)))
                    Box(modifier = Modifier.width(8.dp).height(18.dp).background(Color(0xFF4CAF50)))
                    Box(modifier = Modifier.width(8.dp).height(12.dp).background(Color.LightGray))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
    
    // Animasi jarum agar bergerak mulus
    val targetRatio = currentRpm.toFloat() / maxRpm
    val needleRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 300),
        label = "rpmNeedle"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Titik tengah lingkaran diletakkan di bawah (supaya melengkung ke atas)
            val center = Offset(width / 2, height)
            val radius = width / 2.3f 

            val startAngle = 180f
            val sweepAngle = 180f

            // Gambar garis background (abu-abu)
            drawArc(
                color = Color.LightGray,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )

            // Gambar zona merah (redline 80% - 100%)
            drawArc(
                color = redZoneColor,
                startAngle = startAngle + (sweepAngle * 0.8f),
                sweepAngle = sweepAngle * 0.2f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )

            // Gambar Jarum (mengikuti sudut)
            val angleDeg = startAngle + (sweepAngle * needleRatio)
            val angleRad = Math.toRadians(angleDeg.toDouble())
            
            // Jarum ditarik dari luar ke dalam (tidak menutupi angka speed)
            val outerRadius = radius - 12f // Ujung jarum
            val innerRadius = radius * 0.4f // Pangkal jarum (tidak sampai tengah)
            
            val startPoint = Offset(
                x = center.x + (innerRadius * cos(angleRad)).toFloat(),
                y = center.y + (innerRadius * sin(angleRad)).toFloat()
            )
            val endPoint = Offset(
                x = center.x + (outerRadius * cos(angleRad)).toFloat(),
                y = center.y + (outerRadius * sin(angleRad)).toFloat()
            )

            drawLine(
                color = accentColor,
                start = startPoint,
                end = endPoint,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }
    }
}
