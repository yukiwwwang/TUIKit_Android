package com.trtc.uikit.livekit.features.endstatistics.state

import androidx.lifecycle.MutableLiveData

class EndStatisticsState {
    val roomId = MutableLiveData("")
    val ownerName = MutableLiveData("")
    val ownerAvatarUrl = MutableLiveData("")
    val liveDurationMS = MutableLiveData(0L)
    val maxViewersCount = MutableLiveData(0L)
    val messageCount = MutableLiveData(0L)
    val likeCount = MutableLiveData(0L)
    val giftIncome = MutableLiveData(0L)
    val giftSenderCount = MutableLiveData(0L)

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