package com.homearcade.tv.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.ServerConfig
import com.homearcade.tv.data.ServerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

sealed class ConnectionStatus {
    data object Idle : ConnectionStatus()
    data object Testing : ConnectionStatus()
    data object Success : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

data class SetupUiState(
    val host: String = "",
    val port: String = "9876",
    val connectionStatus: ConnectionStatus = ConnectionStatus.Idle,
    val isLoaded: Boolean = false
)

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServerRepository(application)

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        loadSavedConfig()
    }

    private fun loadSavedConfig() {
        viewModelScope.launch {
            val config = repository.getConfig()
            _uiState.update {
                it.copy(
                    host = config.host,
                    port = config.port.ifBlank { "9876" },
                    isLoaded = true
                )
            }
        }
    }

    fun updateHost(host: String) {
        _uiState.update { it.copy(host = host, connectionStatus = ConnectionStatus.Idle) }
    }

    fun updatePort(port: String) {
        _uiState.update { it.copy(port = port, connectionStatus = ConnectionStatus.Idle) }
    }

    fun testConnection() {
        val state = _uiState.value
        if (state.host.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.Testing) }
            val result = testConnection(state.host, state.port)
            _uiState.update { it.copy(connectionStatus = result) }
        }
    }

    fun saveAndLaunch(onLaunch: (host: String, port: String) -> Unit) {
        val state = _uiState.value
        if (state.host.isBlank()) return

        viewModelScope.launch {
            repository.saveConfig(ServerConfig(state.host, state.port))
            onLaunch(state.host, state.port)
        }
    }

    fun getSavedConfig(): ServerConfig? {
        return runCatching {
            kotlinx.coroutines.runBlocking { repository.getConfig() }
        }.getOrNull()
    }

    companion object {
        suspend fun testConnection(host: String, port: String): ConnectionStatus = withContext(Dispatchers.IO) {
            try {
                val url = URL("http://$host:$port/api/health")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val responseCode = connection.responseCode
                connection.disconnect()
                if (responseCode == 200) ConnectionStatus.Success
                else ConnectionStatus.Error("Server returned HTTP $responseCode")
            } catch (e: UnknownHostException) {
                ConnectionStatus.Error("Server not found. Check the address.")
            } catch (e: ConnectException) {
                ConnectionStatus.Error("Connection refused. Check port and server status.")
            } catch (e: Exception) {
                ConnectionStatus.Error(e.message ?: "Connection failed")
            }
        }
    }
}
