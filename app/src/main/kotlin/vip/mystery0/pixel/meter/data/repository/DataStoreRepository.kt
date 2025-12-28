package vip.mystery0.pixel.meter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val DATA_STORE_NAME = "pixel_pulse_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {

    // Keys mapped from legacy SharedPreferences in NetworkRepository.kt
    companion object {
        val KEY_LIVE_UPDATE = booleanPreferencesKey("key_live_update")
        val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("key_notification_enabled")
        val KEY_OVERLAY_ENABLED = booleanPreferencesKey("key_overlay_enabled")
        val KEY_OVERLAY_LOCKED = booleanPreferencesKey("key_overlay_locked")
        val KEY_OVERLAY_X = intPreferencesKey("key_overlay_x")
        val KEY_OVERLAY_Y = intPreferencesKey("key_overlay_y")

        val KEY_SAMPLING_INTERVAL = longPreferencesKey("key_sampling_interval")
        val KEY_OVERLAY_BG_COLOR = intPreferencesKey("key_overlay_bg_color")
        val KEY_OVERLAY_CORNER_RADIUS = intPreferencesKey("key_overlay_corner_radius")
        val KEY_OVERLAY_TEXT_SIZE = floatPreferencesKey("key_overlay_text_size")
        val KEY_OVERLAY_TEXT_UP = stringPreferencesKey("key_overlay_text_up")
        val KEY_OVERLAY_TEXT_DOWN = stringPreferencesKey("key_overlay_text_down")
        val KEY_OVERLAY_ORDER_UP_FIRST = booleanPreferencesKey("key_overlay_order_up_first")
        val KEY_NOTIFICATION_TEXT_UP = stringPreferencesKey("key_notification_text_up")
        val KEY_NOTIFICATION_TEXT_DOWN = stringPreferencesKey("key_notification_text_down")
        val KEY_NOTIFICATION_ORDER_UP_FIRST =
            booleanPreferencesKey("key_notification_order_up_first")
        val KEY_NOTIFICATION_DISPLAY_MODE = intPreferencesKey("key_notification_display_mode")
    }

    val isLiveUpdateEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_LIVE_UPDATE] ?: false
        }

    val isNotificationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED]
                ?: true // Default TRUE as seen in NetworkRepository
        }

    val isOverlayEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_ENABLED] ?: false
        }

    val isOverlayLocked: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_LOCKED] ?: false
        }

    val overlayX: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_X] ?: 100
        }

    val overlayY: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_Y] ?: 200
        }

    val samplingInterval: Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[KEY_SAMPLING_INTERVAL] ?: 1500L
        }

    val overlayBgColor: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_BG_COLOR]
                ?: 0xCC000000.toInt() // Default semi-transparent black
        }

    val overlayCornerRadius: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_CORNER_RADIUS] ?: 8
        }

    val overlayTextSize: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_TEXT_SIZE] ?: 10f
        }

    val overlayTextUp: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_TEXT_UP] ?: "▲ "
        }

    val overlayTextDown: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_TEXT_DOWN] ?: "▼ "
        }

    val overlayOrderUpFirst: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_ORDER_UP_FIRST] ?: true // Default TRUE as requested
        }

    val notificationTextUp: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_TEXT_UP] ?: "TX "
        }

    val notificationTextDown: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_TEXT_DOWN] ?: "RX "
        }

    val notificationOrderUpFirst: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_ORDER_UP_FIRST] ?: true // Default TRUE as requested
        }

    val notificationDisplayMode: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_DISPLAY_MODE] ?: 0 // 0: Total, 1: Up, 2: Down
        }

    suspend fun setLiveUpdateEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_LIVE_UPDATE] = enabled
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_ENABLED] = enabled
        }
    }

    suspend fun setOverlayLocked(locked: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_LOCKED] = locked
        }
    }

    suspend fun saveOverlayPosition(x: Int, y: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_X] = x
            preferences[KEY_OVERLAY_Y] = y
        }
    }

    suspend fun setSamplingInterval(interval: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_SAMPLING_INTERVAL] = interval
        }
    }

    suspend fun setOverlayBgColor(color: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_BG_COLOR] = color
        }
    }

    suspend fun setOverlayCornerRadius(radius: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_CORNER_RADIUS] = radius
        }
    }

    suspend fun setOverlayTextSize(size: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_TEXT_SIZE] = size
        }
    }

    suspend fun setOverlayTextUp(text: String) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_TEXT_UP] = text
        }
    }

    suspend fun setOverlayTextDown(text: String) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_TEXT_DOWN] = text
        }
    }

    suspend fun setOverlayOrderUpFirst(upFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_ORDER_UP_FIRST] = upFirst
        }
    }

    suspend fun setNotificationTextUp(text: String) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_TEXT_UP] = text
        }
    }

    suspend fun setNotificationTextDown(text: String) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_TEXT_DOWN] = text
        }
    }

    suspend fun setNotificationOrderUpFirst(upFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ORDER_UP_FIRST] = upFirst
        }
    }

    suspend fun setNotificationDisplayMode(mode: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_DISPLAY_MODE] = mode
        }
    }
}
