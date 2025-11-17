package com.trtc.uikit.livekit.features.anchorboardcast.state

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow

class MediaState {
    val isAudioLocked = MutableStateFlow(false)
    val isVideoLocked = MutableStateFlow(false)
    val isPipModeEnabled = MutableStateFlow(false)
    var bigMuteBitmap: Bitmap? = null
    var smallMuteBitmap: Bitmap? = null
    var isCameraOccupied = false
}