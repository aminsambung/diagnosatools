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
    throttle: Int?, // Parameter baru
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
        Spacer(modifier = Modifier.height(24.dp)) 

        Text(
            text = if (connected) "● TERHUBUNG" else "○ TERPUTUS",
            color = if (connected) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f) 
                .background(bgColor, RoundedCornerShape(16.dp))
                .border(10.dp, Color(0xFF222222), RoundedCornerShape(16.dp))
        ) {
            RpmGauge(
                rpm = rpm ?: 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f) 
                    .padding(top = 10.dp),
                redZoneColor = redZoneColor,
                accentColor = accentColor
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 30.dp), 
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

            // Indikator Bawah (Suhu & Throttle)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // SUHU
                Column(horizontalAlignment = Alignment.Start) {
                    Text("SUHU", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = if (coolant != null) "$coolant°C" else "--°C",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if ((coolant ?: 0) > 90) redZoneColor else textColor
                    )
                }

                // THROTTLE (Bukaan Gas)
                Column(horizontalAlignment = Alignment.End) {
                    Text("THROTTLE", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = if (throttle != null) "$throttle%" else "--%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
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
            val center = Offset(width / 2, height)
            val radius = width / 2.3f 

            val startAngle = 180f
            val sweepAngle = 180f

            drawArc(
                color = Color.LightGray,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )

            drawArc(
                color = redZoneColor,
                startAngle = startAngle + (sweepAngle * 0.8f),
                sweepAngle = sweepAngle * 0.2f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )

            val angleDeg = startAngle + (sweepAngle * needleRatio)
            val angleRad = Math.toRadians(angleDeg.toDouble())
            
            val outerRadius = radius - 12f 
            val innerRadius = radius * 0.4f 
            
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
