package com.homearcade.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun SettingsScreen(
    host: String,
    port: String,
    onBack: () -> Unit = {},
    onSave: (host: String, port: String) -> Unit = {}
) {
    var editHost by remember { mutableStateOf(host) }
    var editPort by remember { mutableStateOf(port) }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(32.dp))

        Surface(
            modifier = Modifier.widthIn(max = 520.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IpInputField(
                    value = editHost,
                    onValueChange = { editHost = it },
                    label = "Server Address",
                    placeholder = "192.168.1.50",
                    modifier = Modifier.widthIn(max = 400.dp)
                )
                Spacer(Modifier.height(12.dp))

                IpInputField(
                    value = editPort,
                    onValueChange = { editPort = it },
                    label = "Port",
                    placeholder = "9876",
                    modifier = Modifier.widthIn(max = 200.dp),
                    isNumeric = true
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Changes take effect after reconnecting.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = onBack) {
                        Text("Back", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = { onSave(editHost, editPort) },
                        enabled = editHost.isNotBlank()
                    ) {
                        Text("Save & Reconnect", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text = "HomeArcade TV v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
