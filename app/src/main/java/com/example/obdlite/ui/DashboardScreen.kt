package com.example.obdlite.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    connected: Boolean,
    rpm: Int?,
    speed: Int?,
    coolant: Int?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("OBD Lite", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = Color.White)

        Spacer(Modifier.height(8.dp))
        Text(
            if (connected) "● Terhubung" else "○ Terputus",
            color = if (connected) Color(0xFF4CAF50) else Color(0xFFF44336)
        )

        Spacer(Modifier.height(24.dp))

        DataCard("RPM", rpm?.toString() ?: "--", "rpm")
        Spacer(Modifier.height(12.dp))
        DataCard("Kecepatan", speed?.toString() ?: "--", "km/h")
        Spacer(Modifier.height(12.dp))
        DataCard("Suhu Air", coolant?.toString() ?: "--", "°C")

        Spacer(Modifier.weight(1f))

        Button(
            onClick = if (connected) onDisconnect else onConnect,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (connected) Color(0xFFF44336)
                else Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (connected) "Putuskan" else "Hubungkan Adaptor",
                fontSize = 16.sp)
        }
    }
}

@Composable
private fun DataCard(label: String, value: String, unit: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFFAAAAAA), fontSize = 16.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 36.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text(unit, color = Color(0xFFAAAAAA), fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}
