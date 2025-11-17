package com.trtc.uikit.livekit.features.endstatistics

object EndStatisticsDefine {

    data class AnchorEndStatisticsInfo(
        var roomId: String = "",
        var liveDurationMS: Long = 0L,
        var maxViewersCount: Long = 0L,
        var messageCount: Long = 0L,
        var likeCount: Long = 0L,
        var giftIncome: Long = 0L,
        var giftSenderCount: Long = 0L
    )

    interface AnchorEndStatisticsViewListener {
        fun onCloseButtonClick()
    }

    interface AudienceEndStatisticsViewListener {
        fun onCloseButtonClick()
    }
}