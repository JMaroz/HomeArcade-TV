package com.homearcade.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "server_config")

class ServerRepository(private val context: Context) {

    companion object {
        private val HOST_KEY = stringPreferencesKey("server_host")
        private val PORT_KEY = stringPreferencesKey("server_port")
    }

    val serverConfig: Flow<ServerConfig> = context.dataStore.data.map { prefs ->
        ServerConfig(
            host = prefs[HOST_KEY] ?: "",
            port = prefs[PORT_KEY] ?: "9876"
        )
    }

    suspend fun saveConfig(config: ServerConfig) {
        context.dataStore.edit { prefs ->
            prefs[HOST_KEY] = config.host
            prefs[PORT_KEY] = config.port
        }
    }

    suspend fun getConfig(): ServerConfig = serverConfig.first()
}
