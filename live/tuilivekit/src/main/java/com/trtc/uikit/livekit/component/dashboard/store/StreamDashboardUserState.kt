package com.trtc.uikit.livekit.component.dashboard.store

data class StreamDashboardUserState(
    var userId: String = "",
    var isLocal: Boolean = false,
    var videoResolution: String = "",
    var videoFrameRate: Int = 0,
    var videoBitrate: Int = 0,
    var audioSampleRate: Int = 0,
    var audioBitrate: Int = 0
)