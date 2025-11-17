package com.trtc.uikit.livekit.features.anchorboardcast

class AnchorBoardcastState {
    var viewCount: Long = 0
    var duration: Long = 0
    var messageCount: Long = 0
    var giftIncome: Long = 0
    var giftSenderCount: Long = 0
    var likeCount: Long = 0
}

interface AnchorViewListener {
    fun onEndLiving(state: AnchorBoardcastState)
    fun onClickFloatWindow()
}

enum class RoomBehavior {
    CREATE_ROOM,
    ENTER_ROOM
}