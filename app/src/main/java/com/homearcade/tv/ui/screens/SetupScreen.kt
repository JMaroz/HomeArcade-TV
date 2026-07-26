package com.homearcade.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.homearcade.tv.viewmodel.ConnectionStatus

@Composable
fun SetupScreen(
    host: String,
    port: String,
    connectionStatus: ConnectionStatus,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onLaunch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HomeArcade",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "TV CLIENT",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 6.sp
        )
        Spacer(Modifier.height(56.dp))

        Surface(
            modifier = Modifier.widthIn(max = 520.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Server Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(24.dp))

                IpInputField(
                    value = host,
                    onValueChange = onHostChange,
                    label = "Address",
                    placeholder = "192.168.1.50",
                    modifier = Modifier.widthIn(max = 400.dp)
                )
                Spacer(Modifier.height(12.dp))

                IpInputField(
                    value = port,
                    onValueChange = onPortChange,
                    label = "Port",
                    placeholder = "9876",
                    modifier = Modifier.widthIn(max = 200.dp),
                    isNumeric = true
                )
                Spacer(Modifier.height(24.dp))

                when (connectionStatus) {
                    is ConnectionStatus.Testing -> {
                        Text(
                            text = "Testing connection...",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is ConnectionStatus.Success -> {
                        Text(
                            text = "Connected",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is ConnectionStatus.Error -> {
                        Text(
                            text = connectionStatus.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {}
                }
                if (connectionStatus !is ConnectionStatus.Idle) {
                    Spacer(Modifier.height(16.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onTestConnection,
                        enabled = host.isNotBlank()
                    ) {
                        Text("Test Connection", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = onLaunch,
                        enabled = host.isNotBlank()
                    ) {
                        Text("Launch", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
