package com.trtc.uikit.livekit.common

import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import io.trtc.tuikit.atomicxcore.api.live.SeatInfo
import io.trtc.tuikit.atomicxcore.api.live.TakeSeatMode


fun seatModeFromEngineSeatMode(seatMode: TUIRoomDefine.SeatMode): TakeSeatMode {
    return when (seatMode) {
        TUIRoomDefine.SeatMode.FREE_TO_TAKE -> TakeSeatMode.FREE
        TUIRoomDefine.SeatMode.APPLY_TO_TAKE -> TakeSeatMode.APPLY
    }
}

fun convertToSeatInfo(audienceInfo: SeatInfo): TUIRoomDefine.SeatInfo {
    return TUIRoomDefine.SeatInfo().apply {
        index = audienceInfo.index
        userId = audienceInfo.userInfo.userID
        userName = audienceInfo.userInfo.userName
        nameCard = audienceInfo.userInfo.userName
        avatarUrl = audienceInfo.userInfo.avatarURL
        isLocked = audienceInfo.isLocked
        isVideoLocked = audienceInfo.userInfo.allowOpenCamera
        isAudioLocked = audienceInfo.userInfo.allowOpenMicrophone
    }
}