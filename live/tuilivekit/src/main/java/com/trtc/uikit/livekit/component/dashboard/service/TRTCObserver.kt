package com.trtc.uikit.livekit.component.dashboard.service

import com.tencent.trtc.TRTCCloudListener
import com.tencent.trtc.TRTCStatistics

class TRTCObserver : TRTCCloudListener() {
    private var mTRTCStatisticsListener: TRTCStatisticsListener? = null

    fun setListener(listener: TRTCStatisticsListener?) {
        mTRTCStatisticsListener = listener
    }

    override fun onStatistics(statistics: TRTCStatistics) {
        mTRTCStatisticsListener?.let {
            it.onNetworkStatisticsChange(statistics.rtt, statistics.upLoss, statistics.downLoss)
            it.onLocalStatisticsChange(statistics.localArray)
            it.onRemoteStatisticsChange(statistics.remoteArray)
        }
    }
}