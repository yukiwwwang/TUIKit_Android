package com.trtc.uikit.livekit.voiceroom

import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine
import java.io.Serializable

object VoiceRoomDefine {
    const val MAX_CONNECTED_VIEWERS_COUNT = 10

    class CreateRoomParams : Serializable {
        var roomName: String = ""
        var maxAnchorCount: Int = MAX_CONNECTED_VIEWERS_COUNT
        var seatMode: TUIRoomDefine.SeatMode = TUIRoomDefine.SeatMode.APPLY_TO_TAKE
    }
}