package com.example.zenbreath.wear.data.sync

object SyncConstants {
    const val PATH_SESSION_ACTIVE = "/session/active"
    const val PATH_SESSION_COMPLETE = "/session/complete"
    const val PATH_SETTINGS_USER = "/settings/user"
    const val PATH_HEART_RATE_LIVE = "/sensor/heart_rate/live"
    
    const val KEY_SESSION_ACTIVE_STATUS = "active_status"
    const val KEY_SESSION_START_TIME = "start_time"
    const val KEY_SESSION_TYPE = "session_type"
    
    const val MESSAGE_START_SESSION = "/message/start_session"
    const val MESSAGE_STOP_SESSION = "/message/stop_session"
}
