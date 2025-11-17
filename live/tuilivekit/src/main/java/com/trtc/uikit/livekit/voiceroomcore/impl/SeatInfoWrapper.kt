package com.trtc.uikit.livekit.voiceroomcore.impl;

import io.trtc.tuikit.atomicxcore.api.live.SeatInfo

data class SeatInfoWrapper(
    var seatInfo: SeatInfo? = null,
    var rowIndex: Int = 0,
    var columnIndex: Int = 0
)