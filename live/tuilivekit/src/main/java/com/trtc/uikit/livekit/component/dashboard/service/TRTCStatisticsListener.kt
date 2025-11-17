package com.trtc.uikit.livekit.component.dashboard.service

import com.tencent.trtc.TRTCStatistics

abstract class TRTCStatisticsListener {

    open fun onNetworkStatisticsChange(rtt: Int, upLoss: Int, downLoss: Int) {
    }

    open fun onLocalStatisticsChange(localArray: ArrayList<TRTCStatistics.TRTCLocalStatistics>) {
    }

    open fun onRemoteStatisticsChange(remoteArray: ArrayList<TRTCStatistics.TRTCRemoteStatistics>) {
    }
}