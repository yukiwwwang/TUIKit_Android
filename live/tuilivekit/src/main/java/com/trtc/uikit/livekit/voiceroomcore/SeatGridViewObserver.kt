package com.trtc.uikit.livekit.voiceroomcore

import android.view.View
import com.tencent.cloud.tuikit.engine.room.TUIRoomDefine

abstract class SeatGridViewObserver {
    open fun onSeatViewClicked(seatView: View, seatInfo: TUIRoomDefine.SeatInfo) = Unit
}
