package com.app.privacyscreendisplay.core.ads.model

/**
 * State machine representation for Ad Lifecycle tracking.
 */
enum class AdState {
    IDLE,
    INITIALIZING,
    LOADING,
    LOADED,
    SHOWING,
    DISMISSED,
    CONSUMED,
    RETRYING,
    EXPIRED,
    DESTROYED
}
