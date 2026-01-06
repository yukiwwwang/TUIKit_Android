package com.trtc.uikit.livekit.features.endstatistics.store

import kotlinx.coroutines.flow.MutableStateFlow

class EndStatisticsState {
    val roomId = MutableStateFlow("")
    val ownerName = MutableStateFlow("")
    val ownerAvatarUrl = MutableStateFlow("")
    val liveDurationMS = MutableStateFlow(0L)
    val maxViewersCount = MutableStateFlow(0L)
    val messageCount = MutableStateFlow(0L)
    val likeCount = MutableStateFlow(0L)
    val giftIncome = MutableStateFlow(0L)
    val giftSenderCount = MutableStateFlow(0L)

    override fun toString(): String {
        return "EndStatisticsState{" +
                "roomId=${roomId.value}, " +
                "ownerName=${ownerName.value}, " +
                "ownerAvatarUrl=${ownerAvatarUrl.value}, " +
                "liveDurationMS=${liveDurationMS.value}, " +
                "maxViewersCount=${maxViewersCount.value}, " +
                "messageCount=${messageCount.value}, " +
                "likeCount=${likeCount.value}, " +
                "giftIncome=${giftIncome.value}, " +
                "giftSenderCount=${giftSenderCount.value}" +
                "}"
    }
}