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
        val KEY_OVERLAY_TEXT_COLOR = intPreferencesKey("key_overlay_text_color")
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
        val KEY_NOTIFICATION_TEXT_SIZE = floatPreferencesKey("key_notification_text_size")
        val KEY_NOTIFICATION_UNIT_SIZE = floatPreferencesKey("key_notification_unit_size")

        val KEY_HIDE_FROM_RECENTS = booleanPreferencesKey("key_hide_from_recents")
        val KEY_OVERLAY_USE_DEFAULT_COLORS = booleanPreferencesKey("key_overlay_use_default_colors")
        val KEY_AUTO_START_SERVICE = booleanPreferencesKey("key_auto_start_service")
        val KEY_NOTIFICATION_THRESHOLD = longPreferencesKey("key_notification_threshold")
        val KEY_NOTIFICATION_LOW_TRAFFIC_MODE =
            intPreferencesKey("key_notification_low_traffic_mode")
        val KEY_NOTIFICATION_USE_CUSTOM_COLOR =
            booleanPreferencesKey("key_notification_use_custom_color")
        val KEY_NOTIFICATION_COLOR = intPreferencesKey("key_notification_color")
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

    val overlayTextColor: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_TEXT_COLOR]
                ?: 0xFFFFFFFF.toInt() // Default white
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
            preferences[KEY_NOTIFICATION_TEXT_UP] ?: "▲ "
        }

    val notificationTextDown: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_TEXT_DOWN] ?: "▼ "
        }

    val notificationOrderUpFirst: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_ORDER_UP_FIRST] ?: true // Default TRUE as requested
        }

    val notificationDisplayMode: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_DISPLAY_MODE] ?: 0 // 0: Total, 1: Up, 2: Down
        }

    val notificationTextSize: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_TEXT_SIZE] ?: 0.65f
        }

    val notificationUnitSize: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_UNIT_SIZE] ?: 0.35f
        }

    val notificationThreshold: Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_THRESHOLD] ?: 0L
        }

    val notificationLowTrafficMode: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_LOW_TRAFFIC_MODE] ?: 0 // 0: Static, 1: Dynamic
        }

    val notificationUseCustomColor: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_USE_CUSTOM_COLOR] ?: false
        }

    val notificationColor: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_COLOR] ?: 0
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

    suspend fun setOverlayTextColor(color: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_TEXT_COLOR] = color
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

    suspend fun setNotificationTextSize(size: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_TEXT_SIZE] = size
        }
    }

    suspend fun setNotificationUnitSize(size: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_UNIT_SIZE] = size
        }
    }

    suspend fun setNotificationThreshold(threshold: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_THRESHOLD] = threshold
        }
    }

    suspend fun setNotificationLowTrafficMode(mode: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_LOW_TRAFFIC_MODE] = mode
        }
    }

    suspend fun setNotificationUseCustomColor(useCustom: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_USE_CUSTOM_COLOR] = useCustom
        }
    }

    suspend fun setNotificationColor(color: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_COLOR] = color
        }
    }

    val isHideFromRecents: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_HIDE_FROM_RECENTS] ?: false
        }

    suspend fun setHideFromRecents(hide: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HIDE_FROM_RECENTS] = hide
        }
    }

    val isOverlayUseDefaultColors: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_OVERLAY_USE_DEFAULT_COLORS] ?: false
        }

    suspend fun setOverlayUseDefaultColors(useDefault: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_USE_DEFAULT_COLORS] = useDefault
        }
    }

    val isAutoStartServiceEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_AUTO_START_SERVICE] ?: false
        }

    suspend fun setAutoStartServiceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_START_SERVICE] = enabled
        }
    }
}
