package com.trtc.uikit.livekit.voiceroom.view.seatmanager

data class ListMenuInfo(
    val text: String,
    val listener: OnClickListener?
) {
    interface OnClickListener {
        fun onClick()
    }
}